package com.instagram.backend.domain.chat.service;

import com.instagram.backend.domain.chat.dto.ChatRoomCreateResponse;
import com.instagram.backend.domain.chat.dto.ChatRoomListResponse;
import com.instagram.backend.domain.chat.entity.ChatRoom;
import com.instagram.backend.domain.chat.entity.ChatRoomMember;
import com.instagram.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.instagram.backend.domain.chat.repository.ChatRoomRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.message.entity.Message;
import com.instagram.backend.domain.message.entity.MessageText;
import com.instagram.backend.domain.message.repository.MessageRepository;
import com.instagram.backend.domain.message.repository.MessageTextRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/*
  ChatService — 채팅방 도메인 비즈니스 로직

  담당 기능 (이슈 #36):
    1) createChatRoom — POST /api/chats
    2) getMyChatRooms — GET /api/chats

  @Transactional(readOnly = true) — 클래스 레벨
    — 기본적으로 읽기 전용 (조회 성능 최적화)
    — 쓰기 메서드에는 @Transactional을 개별로 덮어씀
*/
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ChatService {

    // 메시지 타입 상수 — 매직 스트링 방지
    private static final String CHAT_ROOM_TYPE_DM = "DM";
    private static final String CHAT_ROOM_TYPE_GROUP = "GROUP";
    private static final String MESSAGE_TYPE_TEXT = "TEXT";
    // 미리보기 본문 최대 길이 (긴 텍스트 잘라서 전송 → 네트워크 절약)
    private static final int PREVIEW_MAX_LENGTH = 50;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final MessageRepository messageRepository;
    private final MessageTextRepository messageTextRepository;

    /*
      채팅방 생성 — POST /api/chats

      처리 순서:
        1) 요청의 member_ids를 정제 (나 자신 제거, 중복 제거)
        2) 나를 포함한 "전체 참여자" 목록 구성
        3) 전체 참여자가 DB에 실제로 존재하는지 검증 (1명이라도 없으면 404)
        4) 참여자 수로 채팅방 타입 판별 (2명=DM, 3명 이상=GROUP)
        5) DM이면 1:1 중복 체크 (같은 두 사람의 DM이 이미 있으면 409)
        6) ChatRoom 저장 (chat_room_name=null)
        7) ChatRoomMember를 전체 참여자 수만큼 INSERT
        8) 생성된 chat_room_id 반환

      BLOCKED_MEMBER 검증은 MVP 이후로 미룸 (block 도메인 미구현)

      @Transactional — 쓰기 작업이므로 override
    */
    @Transactional
    public ChatRoomCreateResponse createChatRoom(Long myMemberId, List<Long> requestMemberIds) {
        // 1. 요청의 member_ids에서 나 자신 제거 + 중복 제거
        //    — 프론트가 실수로 내 ID를 포함시킬 수 있으므로 방어적으로 처리
        //    — LinkedHashSet을 사용해 중복 제거하면서 입력 순서 유지
        Set<Long> otherIds = new LinkedHashSet<>(requestMemberIds);
        otherIds.remove(myMemberId);

        // 정제 후 상대가 0명이면 "참여자 없음" 에러 (자기 자신과의 채팅방은 불가)
        if (otherIds.isEmpty()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }

        // 2. 나를 포함한 전체 참여자 목록 (나 + 상대들)
        Set<Long> allParticipantIds = new LinkedHashSet<>();
        allParticipantIds.add(myMemberId);
        allParticipantIds.addAll(otherIds);

        // 3. 전체 참여자가 DB에 실제로 존재하는지 검증
        //    — IN 배치 조회 한 번으로 N+1 회피
        //    — 조회 결과 수 != 요청 수 이면 존재하지 않는 멤버가 포함된 것
        List<Member> existingMembers = memberRepository.findByMemberIdInAndIsDeletedFalse(allParticipantIds);
        if (existingMembers.size() != allParticipantIds.size()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 4. 참여자 수로 채팅방 타입 판별
        //    — 2명(나+상대 1명) = DM
        //    — 3명 이상(나+상대 2명 이상) = GROUP
        String chatRoomType = (allParticipantIds.size() == 2) ? CHAT_ROOM_TYPE_DM : CHAT_ROOM_TYPE_GROUP;

        // 5. DM이면 1:1 중복 체크
        //    — 그룹 채팅방은 같은 사람들이 있어도 여러 개 허용
        if (CHAT_ROOM_TYPE_DM.equals(chatRoomType)) {
            Long otherId = otherIds.iterator().next(); // DM이므로 상대는 정확히 1명
            List<Long> existingDmRoomIds = chatRoomMemberRepository.findDmRoomIdBetween(myMemberId, otherId);
            if (!existingDmRoomIds.isEmpty()) {
                throw new BusinessException(ErrorCode.ROOM_ALREADY_EXISTS);
            }
        }

        // 6. ChatRoom 저장 (chat_room_name은 null로 두기)
        //    — 1:1은 프론트가 상대방 username으로 렌더링하므로 이름 불필요
        //    — 그룹은 향후 이름 설정 API에서 값 채울 예정
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomType(chatRoomType)
                .chatRoomName(null)
                .build();
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);

        // 7. 전체 참여자를 chat_room_members에 INSERT
        //    — joinAt은 현재 시각으로 기록 (누가 언제 이 방에 들어왔는지 추적)
        //    — last_read_message_id는 null로 시작 (아직 읽은 메시지 없음)
        LocalDateTime now = LocalDateTime.now();
        List<ChatRoomMember> membersToSave = new ArrayList<>();
        for (Long participantId : allParticipantIds) {
            membersToSave.add(ChatRoomMember.builder()
                    .chatRoomId(savedRoom.getChatRoomId())
                    .memberId(participantId)
                    .joinAt(now)
                    .build());
        }
        chatRoomMemberRepository.saveAll(membersToSave);

        // 8. 생성된 chat_room_id 반환
        return ChatRoomCreateResponse.of(savedRoom.getChatRoomId());
    }

    /*
      내 채팅방 목록 조회 — GET /api/chats

      처리 순서 (4단계 배치 조회로 N+1 최소화):
        1) chat_room_members에서 내가 속한 room 목록 획득
        2) chat_rooms IN 배치 조회로 채팅방 정보 일괄 획득
        3) chat_room_members IN 배치 조회로 모든 참여자 정보 획득 (나 제외)
           + members IN 배치 조회로 username/imageUrl 획득
        4) 각 방의 최신 메시지 1건 개별 조회 (N+1 허용 범위)
           + TEXT 타입 메시지 IDs로 message_texts IN 배치 조회
        5) 각 방의 unread_count 개별 COUNT (N+1 허용 범위)
        6) 응답 DTO 조립 + 정렬 (last_message.sent_at DESC, null이면 created_at DESC)

      페이지네이션:
        — MVP는 전체 반환 (PDF 명세에 커서 파라미터 없음)
        — 향후 채팅방 수 증가 시 CursorPageResponse 기반 추가 가능
    */
    public List<ChatRoomListResponse> getMyChatRooms(Long myMemberId) {
        // 1. 내가 속한 chat_room_id 목록 획득
        //    — 나의 ChatRoomMember 레코드들에서 chat_room_id와 last_read_message_id를 함께 얻음
        List<ChatRoomMember> myMemberships = chatRoomMemberRepository.findByMemberId(myMemberId);
        if (myMemberships.isEmpty()) {
            return new ArrayList<>(); // 참여한 방 없음 → 빈 목록 즉시 반환
        }

        // memberId → lastReadMessageId 매핑 (unread_count 계산용)
        // chat_room_id 기준으로 내 last_read_message_id를 빠르게 조회하기 위해 Map으로 변환
        Map<Long, Long> myLastReadByRoomId = new HashMap<>();
        List<Long> myRoomIds = new ArrayList<>();
        for (ChatRoomMember membership : myMemberships) {
            myRoomIds.add(membership.getChatRoomId());
            myLastReadByRoomId.put(
                    membership.getChatRoomId(),
                    membership.getLastReadMessageId() // null 가능 — 이후 COUNT 시 0으로 변환
            );
        }

        // 2. chat_rooms IN 조회 (소프트 삭제된 방 자동 제외)
        List<ChatRoom> chatRooms = chatRoomRepository.findByChatRoomIdInAndIsDeletedFalse(myRoomIds);
        if (chatRooms.isEmpty()) {
            return new ArrayList<>(); // 내 모든 방이 삭제된 상태 → 빈 목록
        }

        // 살아있는 room_id만 재구성 (소프트 삭제된 방은 이후 처리에서 제외)
        List<Long> aliveRoomIds = chatRooms.stream().map(ChatRoom::getChatRoomId).toList();
        Map<Long, ChatRoom> roomById = new HashMap<>();
        for (ChatRoom room : chatRooms) {
            roomById.put(room.getChatRoomId(), room);
        }

        // 3. 모든 방의 참여자 목록 IN 조회 (나 포함, 이후 필터링)
        //    — 내 방에 속한 모든 chat_room_member row를 한 번에 가져와서 room별로 그룹화
        List<ChatRoomMember> allMemberships = chatRoomMemberRepository.findByChatRoomIdIn(aliveRoomIds);

        // room_id → [other member_ids] 매핑 (나 제외)
        Map<Long, List<Long>> otherMemberIdsByRoomId = new HashMap<>();
        Set<Long> allOtherMemberIds = new HashSet<>(); // 모든 "다른 참여자" ID를 한 번에 모아 IN 조회용
        for (ChatRoomMember membership : allMemberships) {
            if (membership.getMemberId().equals(myMemberId)) {
                continue; // 나 자신 제외
            }
            otherMemberIdsByRoomId
                    .computeIfAbsent(membership.getChatRoomId(), k -> new ArrayList<>())
                    .add(membership.getMemberId());
            allOtherMemberIds.add(membership.getMemberId());
        }

        // members IN 조회로 username/imageUrl 한 번에 획득 (N+1 회피)
        Map<Long, Member> memberById = new HashMap<>();
        if (!allOtherMemberIds.isEmpty()) {
            List<Member> others = memberRepository.findByMemberIdInAndIsDeletedFalse(allOtherMemberIds);
            for (Member m : others) {
                memberById.put(m.getMemberId(), m);
            }
        }

        // 4. 각 방의 최신 메시지 1건 조회 (방마다 1쿼리 — N+1 허용)
        //    — 향후 서브쿼리/윈도우 함수로 최적화 가능하지만 MVP에선 단순성 우선
        Map<Long, Message> lastMessageByRoomId = new HashMap<>();
        List<Long> textMessageIds = new ArrayList<>(); // TEXT 타입 메시지 ID만 따로 수집
        for (Long roomId : aliveRoomIds) {
            Optional<Message> latest = messageRepository.findTopByChatRoomIdAndIsDeletedFalseOrderByMessageIdDesc(roomId);
            latest.ifPresent(msg -> {
                lastMessageByRoomId.put(roomId, msg);
                if (MESSAGE_TYPE_TEXT.equals(msg.getMessageType())) {
                    textMessageIds.add(msg.getMessageId());
                }
            });
        }

        // TEXT 타입 메시지들의 본문을 IN 배치 조회 (preview용)
        //    — TEXT가 아닌 메시지는 preview=null로 응답
        Map<Long, String> previewByMessageId = new HashMap<>();
        if (!textMessageIds.isEmpty()) {
            List<MessageText> texts = messageTextRepository.findByMessageIdIn(textMessageIds);
            for (MessageText text : texts) {
                previewByMessageId.put(text.getMessageId(), truncatePreview(text.getMessageTextContents()));
            }
        }

        // 5. 각 방의 unread_count 계산 (방마다 1쿼리 — N+1 허용)
        //    — last_read_message_id > 해당 값인 메시지 수를 COUNT
        //    — null이면 0으로 변환해서 "모든 메시지가 안 읽은 상태"로 계산
        Map<Long, Long> unreadCountByRoomId = new HashMap<>();
        for (Long roomId : aliveRoomIds) {
            Long lastRead = myLastReadByRoomId.get(roomId);
            long cursor = (lastRead == null) ? 0L : lastRead;
            long unread = messageRepository.countByChatRoomIdAndMessageIdGreaterThanAndIsDeletedFalse(roomId, cursor);
            unreadCountByRoomId.put(roomId, unread);
        }

        // 6. 응답 DTO 조립
        List<ChatRoomListResponse> responses = new ArrayList<>();
        for (Long roomId : aliveRoomIds) {
            ChatRoom room = roomById.get(roomId);

            // other_members 조립 (나 제외 참여자 → OtherMember DTO 변환)
            List<ChatRoomListResponse.OtherMember> otherMembers = new ArrayList<>();
            List<Long> otherIds = otherMemberIdsByRoomId.getOrDefault(roomId, new ArrayList<>());
            for (Long otherId : otherIds) {
                Member other = memberById.get(otherId);
                if (other == null) {
                    continue; // 삭제된 멤버는 건너뜀
                }
                otherMembers.add(ChatRoomListResponse.OtherMember.builder()
                        .memberId(other.getMemberId())
                        .memberUsername(other.getMemberUsername())
                        .profileImageUrl(other.getImageUrl())
                        .build());
            }

            // last_message 조립 (없으면 null)
            Message latest = lastMessageByRoomId.get(roomId);
            ChatRoomListResponse.LastMessage lastMessage = null;
            if (latest != null) {
                lastMessage = ChatRoomListResponse.LastMessage.builder()
                        .messageId(latest.getMessageId())
                        .messageType(latest.getMessageType())
                        .preview(previewByMessageId.get(latest.getMessageId())) // TEXT 아니면 null
                        .senderId(latest.getMemberId())
                        .sentAt(latest.getCreatedAt())
                        .build();
            }

            responses.add(ChatRoomListResponse.builder()
                    .chatRoomId(room.getChatRoomId())
                    .chatRoomType(room.getChatRoomType())
                    .chatRoomName(room.getChatRoomName())
                    .otherMembers(otherMembers)
                    .lastMessage(lastMessage)
                    .unreadCount(unreadCountByRoomId.getOrDefault(roomId, 0L))
                    .build());
        }

        // 7. 정렬 — 최근 활동 순 (last_message.sent_at DESC, null이면 room.created_at DESC)
        //    — latest 조회 시 roomById와 lastMessageByRoomId를 함께 이용
        responses.sort((a, b) -> {
            LocalDateTime aTime = (a.getLastMessage() != null)
                    ? a.getLastMessage().getSentAt()
                    : roomById.get(a.getChatRoomId()).getCreatedAt();
            LocalDateTime bTime = (b.getLastMessage() != null)
                    ? b.getLastMessage().getSentAt()
                    : roomById.get(b.getChatRoomId()).getCreatedAt();
            return bTime.compareTo(aTime); // DESC
        });

        return responses;
    }

    /*
      preview 본문 자르기 — 50자 초과 시 잘라서 반환
      — 네트워크 비용 절약 + 화면에 어차피 긴 텍스트 표시 안 함
      — 프론트에서 추가로 ellipsis("...") 처리 가능
    */
    private String truncatePreview(String content) {
        if (content == null) return null;
        if (content.length() <= PREVIEW_MAX_LENGTH) return content;
        return content.substring(0, PREVIEW_MAX_LENGTH);
    }
}
