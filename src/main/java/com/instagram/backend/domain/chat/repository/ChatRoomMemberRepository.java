package com.instagram.backend.domain.chat.repository;

import com.instagram.backend.domain.chat.entity.ChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    // 내가 속한 채팅방 목록 조회
    // SELECT * FROM chat_room_members WHERE member_id = ?
    // → 내가 참여 중인 모든 chat_room_id를 얻기 위한 첫 단계
    List<ChatRoomMember> findByMemberId(Long memberId);

    /*
      특정 유저가 특정 채팅방의 참여자인지 확인

      목적: 메시지 전송/조회 API의 참여자 권한 검증
        — SELECT 1 FROM chat_room_members WHERE chat_room_id = ? AND member_id = ? LIMIT 1
        — Spring Data JPA의 existsBy... 메서드는 존재 여부만 반환 (count 또는 limit 1)
        — 전체 row를 JVM으로 끌어오는 findByChatRoomIdIn() + stream().anyMatch() 패턴의 대체

      사용처:
        — MessageService.sendMessage / getMessages 의 권한 검증
    */
    boolean existsByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);

    // 여러 채팅방의 참여자 목록을 IN 절 배치 조회 (N+1 회피)
    // SELECT * FROM chat_room_members WHERE chat_room_id IN (?, ?, ...)
    // → 내가 속한 room들의 "다른 참여자" 정보를 한 번에 가져올 때 사용
    List<ChatRoomMember> findByChatRoomIdIn(Collection<Long> chatRoomIds);

    /*
      1:1 DM 채팅방 중복 체크용 쿼리

      목적: "나(myId)와 상대방(otherId) 단 둘만 참여한 DM 타입 채팅방이 이미 있는가?"
        — 그룹 채팅방(chat_room_type='GROUP')은 같은 두 사람이 있어도 상관없음
        — 소프트 삭제된 채팅방(is_deleted=true)은 제외

      동작 방식:
        1) 내가 속한 chat_room_id 목록 (서브쿼리 1)
        2) 상대가 속한 chat_room_id 목록 (서브쿼리 2)
        3) 그 방의 전체 참여자 수가 정확히 2명인지 확인 (서브쿼리 3 — 방어적 검증)
        4) 위 조건을 모두 만족하는 DM 타입 + 삭제되지 않은 채팅방 반환

      왜 인원수 검증(서브쿼리 3)이 필요한가?
        — createChatRoom 로직상 DM 타입 방은 항상 2명만 가지지만,
          데이터 무결성 깨진 경우(수동 INSERT, 향후 멤버 추가 기능 등)를 대비한 방어 코드
        — 만약 3명이 있는 DM 타입 방이 존재하면 위 두 IN 조건만으론 그것까지 매칭됨

      왜 List<Long>로 반환하나?
        — 정상 상황에선 0개 또는 1개만 나옴
        — 방어적으로 List로 받아서 서비스 레이어에서 isEmpty()만 체크
        — 여러 개가 나올 가능성은 거의 없지만, 데이터 무결성 깨진 경우를 대비해 에러 없이 처리 가능
    */
    @Query("""
        SELECT cr.chatRoomId
        FROM ChatRoom cr
        WHERE cr.chatRoomType = 'DM'
          AND cr.isDeleted = false
          AND cr.chatRoomId IN (
              SELECT crm1.chatRoomId FROM ChatRoomMember crm1 WHERE crm1.memberId = :myId
          )
          AND cr.chatRoomId IN (
              SELECT crm2.chatRoomId FROM ChatRoomMember crm2 WHERE crm2.memberId = :otherId
          )
          AND (
              SELECT COUNT(crm3.memberId) FROM ChatRoomMember crm3
              WHERE crm3.chatRoomId = cr.chatRoomId
          ) = 2
        """)
    List<Long> findDmRoomIdBetween(@Param("myId") Long myId, @Param("otherId") Long otherId);

    /*
      특정 채팅방에서 특정 유저의 ChatRoomMember 엔티티 조회

      목적: 읽음 처리 API에서 lastReadMessageId 갱신을 위해 엔티티가 필요
        — existsBy...는 존재 여부만 반환하므로 엔티티를 수정할 수 없음
        — 이 메서드로 엔티티를 가져와서 updateLastReadMessageId()를 호출한 뒤 dirty checking으로 저장
    */
    Optional<ChatRoomMember> findByChatRoomIdAndMemberId(Long chatRoomId, Long memberId);
}
