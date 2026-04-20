package com.instagram.backend.domain.message.service;

import com.instagram.backend.domain.chat.repository.ChatRoomMemberRepository;
import com.instagram.backend.domain.chat.repository.ChatRoomRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.message.dto.request.MessageSendRequest;
import com.instagram.backend.domain.message.dto.response.MessageResponse;
import com.instagram.backend.domain.message.entity.Message;
import com.instagram.backend.domain.message.entity.MessageImage;
import com.instagram.backend.domain.message.entity.MessagePost;
import com.instagram.backend.domain.message.entity.MessageStory;
import com.instagram.backend.domain.message.entity.MessageText;
import com.instagram.backend.domain.message.entity.MessageType;
import com.instagram.backend.domain.message.repository.MessageImageRepository;
import com.instagram.backend.domain.message.repository.MessagePostRepository;
import com.instagram.backend.domain.message.repository.MessageRepository;
import com.instagram.backend.domain.message.repository.MessageStoryRepository;
import com.instagram.backend.domain.message.repository.MessageTextRepository;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.domain.story.entity.Story;
import com.instagram.backend.domain.story.repository.StoryRepository;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  MessageService — 채팅방 메시지 도메인 비즈니스 로직

  담당 기능 (이슈 A단계):
    1) sendMessage    — POST /api/chats/{roomId}/messages
    2) getMessages    — GET  /api/chats/{roomId}/messages (커서 페이지네이션)

  설계 원칙:
    — 매직 스트링 대신 MessageType enum 사용 (ChatService와 상수 공유)
    — 방 존재 + 참여자 권한 검증은 private helper로 통합 (sendMessage/getMessages 중복 제거)
    — N+1 회피: 목록 조회에서 서브 테이블(Text/Image/Post/Story) + Member를 IN 배치 조회
    — 소프트 삭제 패턴: 탈퇴/삭제된 리소스는 자동 제외

  @Transactional(readOnly = true) 클래스 레벨:
    — 기본은 읽기 전용 (조회 성능 최적화)
    — 쓰기 메서드는 @Transactional 로 덮어씀
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    // 목록 조회 limit 기본값/최대값 (명세서: 기본 30)
    // 상한 100은 서버 정책 — 명세 원본에 없으므로 향후 API 문서 업데이트 필요
    private static final int DEFAULT_LIMIT = 30;
    private static final int MAX_LIMIT = 100;

    private final MessageRepository messageRepository;
    private final MessageTextRepository messageTextRepository;
    private final MessageImageRepository messageImageRepository;
    private final MessagePostRepository messagePostRepository;
    private final MessageStoryRepository messageStoryRepository;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final StoryRepository storyRepository;

    /*
      메시지 전송 — POST /api/chats/{roomId}/messages

      처리 순서:
        1) 방 존재 + 참여자 검증 (공통 helper)
        2) dtype 검증 (400 MISSING_REQUIRED_FIELD / INVALID_DTYPE)
        3) dtype별 필수 필드 검증 (400 MISSING_MESSAGE_CONTENT)
        4) 공유 대상 존재 검증 (POST/STORY일 때 404)
        5) Message 공통 레코드 저장
        6) dtype에 대응하는 서브 테이블 레코드 저장
        7) 작성자 정보(소프트 삭제 제외) 조회 후 응답 DTO 반환

      트랜잭션 원자성:
        — @Transactional 덮어쓰기로 공통 Message + 서브 테이블 저장이 한 트랜잭션에서 처리
        — 중간에 RuntimeException이 터지면 둘 다 롤백 (고아 row 방지)

      TODO(#블록도메인 이슈): POST/STORY 공유 시 BLOCKED_MEMBER 검증 추가
    */
    @Transactional
    public MessageResponse sendMessage(Long myMemberId, Long roomId, MessageSendRequest request) {
        // 1) 방 존재 + 참여자 검증
        validateRoomAndParticipant(roomId, myMemberId);

        // 2) dtype 존재/유효성 검증
        String rawDtype = request.getDtype();
        if (rawDtype == null || rawDtype.isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        if (!MessageType.isValid(rawDtype)) {
            throw new BusinessException(ErrorCode.INVALID_DTYPE);
        }
        MessageType dtype = MessageType.valueOf(rawDtype);

        // 3) dtype별 필수 필드 검증 + 4) 공유 대상 존재 검증
        //    저장 실패 시 공통 Message까지 롤백되도록 한 트랜잭션 내에서 처리
        Post sharedPost = null;
        Story sharedStory = null;
        switch (dtype) {
            case TEXT -> {
                if (request.getMessageTextContent() == null || request.getMessageTextContent().isBlank()) {
                    throw new BusinessException(ErrorCode.MISSING_MESSAGE_CONTENT);
                }
            }
            case IMAGE -> {
                if (request.getMessageImageUrl() == null || request.getMessageImageUrl().isBlank()) {
                    throw new BusinessException(ErrorCode.MISSING_MESSAGE_CONTENT);
                }
            }
            case POST -> {
                if (request.getPostId() == null) {
                    throw new BusinessException(ErrorCode.MISSING_MESSAGE_CONTENT);
                }
                sharedPost = postRepository.findByPostIdAndIsDeletedFalse(request.getPostId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
            }
            case STORY -> {
                if (request.getStoryVisitorId() == null) {
                    throw new BusinessException(ErrorCode.MISSING_MESSAGE_CONTENT);
                }
                // 명세서 상 "story_visitor_id"라는 이름을 쓰지만 실제 DB 참조는 story_id
                sharedStory = storyRepository.findByStoryIdAndIsDeletedFalseAndExpiresAtAfter(
                                request.getStoryVisitorId(), LocalDateTime.now())
                        .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));
            }
            // default는 isValid 검증 이후라 도달 불가. 향후 enum 확장 시 안전장치
            default -> throw new BusinessException(ErrorCode.INVALID_DTYPE);
        }

        // 5) 공통 Message 저장 — 먼저 messageId를 확보해야 서브 테이블 FK에 넣을 수 있음
        Message message = Message.builder()
                .memberId(myMemberId)
                .chatRoomId(roomId)
                .messageType(dtype.name())
                .build();
        Message savedMessage = messageRepository.save(message);

        // 6) dtype별 서브 테이블 저장
        switch (dtype) {
            case TEXT -> messageTextRepository.save(MessageText.builder()
                    .messageId(savedMessage.getMessageId())
                    .messageTextContents(request.getMessageTextContent())
                    .build());
            case IMAGE -> messageImageRepository.save(MessageImage.builder()
                    .messageId(savedMessage.getMessageId())
                    .imageUrl(request.getMessageImageUrl())
                    .imageType(request.getMessageImageType())
                    .imageName(request.getMessageImageName())
                    .imageUuid(request.getMessageImageUuid())
                    .build());
            case POST -> messagePostRepository.save(MessagePost.builder()
                    .messageId(savedMessage.getMessageId())
                    .postId(sharedPost.getPostId())
                    .build());
            case STORY -> messageStoryRepository.save(MessageStory.builder()
                    .messageId(savedMessage.getMessageId())
                    .storyId(sharedStory.getStoryId())
                    .build());
            default -> throw new BusinessException(ErrorCode.INVALID_DTYPE);
        }

        // 7) 작성자 정보 조회 (소프트 삭제 제외)
        //    — findById(Long)은 탈퇴 계정까지 노출하므로 프로젝트 전반의 isDeletedFalse 패턴 사용
        Member sender = memberRepository.findByMemberIdAndIsDeletedFalse(myMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        return buildSendResponse(savedMessage, sender, dtype, request, sharedPost, sharedStory);
    }

    /*
      메시지 목록 조회 — GET /api/chats/{roomId}/messages

      커서 페이지네이션 규칙:
        — cursor가 null이면 가장 최신 limit개
        — cursor가 있으면 messageId < cursor 조건으로 더 오래된 limit개
        — 정렬은 messageId DESC (최신이 앞)
        — next_cursor는 이 페이지에서 가장 오래된 messageId (다음 요청에 그대로 넣음)
        — has_next 판단: "+1 트릭" — size+1 개 가져온 뒤 1개 더 있으면 있음

      N+1 회피:
        — 메시지 목록 확보 후 한 번씩만 IN 배치 조회
          (message_texts, message_images, message_posts, message_stories)
        — 작성자 정보도 IN 배치 조회

      탈퇴한 sender 정책:
        — findByMemberIdInAndIsDeletedFalse는 탈퇴 계정을 결과에 포함하지 않음
        — 이 경우 응답의 sender_username/profile_image_url이 null로 내려감
        — 프론트는 null일 때 "(알 수 없음)" 등으로 렌더링 (정책 주석)
    */
    public CursorPageResponse<MessageResponse> getMessages(Long myMemberId, Long roomId, String cursor, Integer limit) {
        // 1) 방 존재 + 참여자 검증
        validateRoomAndParticipant(roomId, myMemberId);

        // 2) limit 정규화 + 검증
        int size = (limit == null) ? DEFAULT_LIMIT : limit;
        if (size < 1 || size > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }

        // 3) cursor 파싱 — 숫자 형식/양수 검증
        Long cursorId = parseCursor(cursor);

        // 4) 메시지 조회 (+1 트릭으로 hasNext 판단)
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Message> fetched = (cursorId == null)
                ? messageRepository.findByChatRoomIdAndIsDeletedFalseOrderByMessageIdDesc(roomId, pageable)
                : messageRepository.findByChatRoomIdAndIsDeletedFalseAndMessageIdLessThanOrderByMessageIdDesc(roomId, cursorId, pageable);

        boolean hasNext = fetched.size() > size;
        List<Message> messages = hasNext ? fetched.subList(0, size) : fetched;

        if (messages.isEmpty()) {
            // 빈 페이지는 next_cursor/hasNext 모두 없음 — 명시적 분기로 가독성 확보
            return CursorPageResponse.of(Collections.emptyList(), null, false);
        }

        // 5) 각 서브 테이블 IN 배치 조회
        List<Long> textIds = new ArrayList<>();
        List<Long> imageIds = new ArrayList<>();
        List<Long> postIds = new ArrayList<>();
        List<Long> storyIds = new ArrayList<>();
        Set<Long> senderIds = new HashSet<>();

        for (Message m : messages) {
            senderIds.add(m.getMemberId());
            String typeStr = m.getMessageType();
            if (!MessageType.isValid(typeStr)) {
                continue; // 데이터 무결성 깨진 row는 건너뛰기
            }
            switch (MessageType.valueOf(typeStr)) {
                case TEXT -> textIds.add(m.getMessageId());
                case IMAGE -> imageIds.add(m.getMessageId());
                case POST -> postIds.add(m.getMessageId());
                case STORY -> storyIds.add(m.getMessageId());
            }
        }

        Map<Long, MessageText> textMap = new HashMap<>();
        Map<Long, MessageImage> imageMap = new HashMap<>();
        Map<Long, MessagePost> postMap = new HashMap<>();
        Map<Long, MessageStory> storyMap = new HashMap<>();

        if (!textIds.isEmpty()) {
            for (MessageText t : messageTextRepository.findByMessageIdIn(textIds)) {
                textMap.put(t.getMessageId(), t);
            }
        }
        if (!imageIds.isEmpty()) {
            for (MessageImage i : messageImageRepository.findByMessageIdIn(imageIds)) {
                imageMap.put(i.getMessageId(), i);
            }
        }
        if (!postIds.isEmpty()) {
            for (MessagePost p : messagePostRepository.findByMessageIdIn(postIds)) {
                postMap.put(p.getMessageId(), p);
            }
        }
        if (!storyIds.isEmpty()) {
            for (MessageStory s : messageStoryRepository.findByMessageIdIn(storyIds)) {
                storyMap.put(s.getMessageId(), s);
            }
        }

        // 작성자 일괄 조회 (N+1 회피 + 소프트 삭제 제외)
        Map<Long, Member> senderMap = new HashMap<>();
        for (Member m : memberRepository.findByMemberIdInAndIsDeletedFalse(senderIds)) {
            senderMap.put(m.getMemberId(), m);
        }

        // 6) 응답 조립
        List<MessageResponse> content = new ArrayList<>();
        for (Message m : messages) {
            content.add(buildListItem(m, senderMap, textMap, imageMap, postMap, storyMap));
        }

        // 7) next_cursor = 이번 페이지의 가장 오래된(= 마지막 원소) messageId
        //    messages는 DESC 정렬이므로 마지막이 가장 작은 값
        String nextCursor = hasNext
                ? String.valueOf(messages.get(messages.size() - 1).getMessageId())
                : null;

        return CursorPageResponse.of(content, nextCursor, hasNext);
    }

    // ===== private helpers =====

    /*
      방 존재 여부 + 참여자 권한 검증 (공통 helper)

      sendMessage / getMessages 모두 동일한 검증이 필요하므로 추출.
      existsByChatRoomIdAndMemberId는 count(*) LIMIT 1 쿼리로 번역되어
      전체 row를 JVM에 끌어오는 것보다 훨씬 효율적.
    */
    private void validateRoomAndParticipant(Long roomId, Long memberId) {
        chatRoomRepository.findByChatRoomIdAndIsDeletedFalse(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));
        if (!chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, memberId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_FORBIDDEN);
        }
    }

    /*
      cursor 문자열 파싱

      - null/공백 → null (초기 조회)
      - 숫자 형식 아님 → INVALID_CURSOR
      - 0 이하 → INVALID_CURSOR (messageId는 1부터 시작)
    */
    private Long parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(cursor);
            if (parsed <= 0) {
                throw new BusinessException(ErrorCode.INVALID_CURSOR);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorCode.INVALID_CURSOR);
        }
    }

    /*
      메시지 전송 응답 DTO 조립
      전송 API에서 방금 저장한 값들을 그대로 DTO에 매핑한다.
    */
    private MessageResponse buildSendResponse(Message savedMessage,
                                              Member sender,
                                              MessageType dtype,
                                              MessageSendRequest request,
                                              Post sharedPost,
                                              Story sharedStory) {
        return MessageResponse.builder()
                .messageId(savedMessage.getMessageId())
                .chatRoomId(savedMessage.getChatRoomId())
                .senderId(sender.getMemberId())
                .senderUsername(sender.getMemberUsername())
                .senderProfileImageUrl(sender.getImageUrl())
                .messageType(savedMessage.getMessageType())
                .createdAt(savedMessage.getCreatedAt())
                .messageTextContent(dtype == MessageType.TEXT ? request.getMessageTextContent() : null)
                .messageImageUrl(dtype == MessageType.IMAGE ? request.getMessageImageUrl() : null)
                .messageImageType(dtype == MessageType.IMAGE ? request.getMessageImageType() : null)
                .messageImageName(dtype == MessageType.IMAGE ? request.getMessageImageName() : null)
                .messageImageUuid(dtype == MessageType.IMAGE ? request.getMessageImageUuid() : null)
                .postId(dtype == MessageType.POST ? sharedPost.getPostId() : null)
                .storyVisitorId(dtype == MessageType.STORY ? sharedStory.getStoryId() : null)
                .build();
    }

    /*
      메시지 목록 응답 DTO 조립
      각 메시지에 대해 작성자 정보와 서브 테이블 정보를 조립한다.
      탈퇴 계정(senderMap에 없음)은 username/imageUrl을 null로 내린다.
    */
    private MessageResponse buildListItem(Message m,
                                          Map<Long, Member> senderMap,
                                          Map<Long, MessageText> textMap,
                                          Map<Long, MessageImage> imageMap,
                                          Map<Long, MessagePost> postMap,
                                          Map<Long, MessageStory> storyMap) {
        Member sender = senderMap.get(m.getMemberId());
        MessageResponse.MessageResponseBuilder b = MessageResponse.builder()
                .messageId(m.getMessageId())
                .chatRoomId(m.getChatRoomId())
                .senderId(m.getMemberId())
                .senderUsername(sender != null ? sender.getMemberUsername() : null)
                .senderProfileImageUrl(sender != null ? sender.getImageUrl() : null)
                .messageType(m.getMessageType())
                .createdAt(m.getCreatedAt());

        if (!MessageType.isValid(m.getMessageType())) {
            return b.build(); // 데이터 무결성 깨진 row — 공통 필드만
        }
        switch (MessageType.valueOf(m.getMessageType())) {
            case TEXT -> {
                MessageText t = textMap.get(m.getMessageId());
                if (t != null) b.messageTextContent(t.getMessageTextContents());
            }
            case IMAGE -> {
                MessageImage i = imageMap.get(m.getMessageId());
                if (i != null) {
                    b.messageImageUrl(i.getImageUrl())
                     .messageImageType(i.getImageType())
                     .messageImageName(i.getImageName())
                     .messageImageUuid(i.getImageUuid());
                }
            }
            case POST -> {
                MessagePost p = postMap.get(m.getMessageId());
                if (p != null) b.postId(p.getPostId());
            }
            case STORY -> {
                MessageStory s = storyMap.get(m.getMessageId());
                if (s != null) b.storyVisitorId(s.getStoryId());
            }
            default -> {
                // 도달 불가 — 위 isValid 검사로 걸러짐
            }
        }
        return b.build();
    }
}
