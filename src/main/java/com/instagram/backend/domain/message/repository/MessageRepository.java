package com.instagram.backend.domain.message.repository;

import com.instagram.backend.domain.message.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    /*
      특정 채팅방의 최신 메시지 1건 조회

      목적: 채팅방 목록 화면의 "마지막 메시지 미리보기" 표시용
        — 각 채팅방마다 가장 최근에 전송된 메시지 하나가 필요
        — message_id DESC 정렬로 AUTO_INCREMENT PK 기준 최신 1건 추출
        — findTop : Spring Data JPA가 LIMIT 1로 번역

      주의 (N+1 가능 지점):
        — 채팅방이 N개면 이 메서드가 N번 호출됨 (MVP 허용 범위)
        — 향후 최적화: 서브쿼리 또는 윈도우 함수로 한 번에 처리 가능
    */
    Optional<Message> findTopByChatRoomIdAndIsDeletedFalseOrderByMessageIdDesc(Long chatRoomId);

    /*
      특정 채팅방에서 "안 읽은 메시지 수" 카운트

      목적: unread_count 응답 필드 계산용
        — chat_room_members.last_read_message_id 기준
        — 해당 메시지 이후의 메시지 = 아직 안 읽은 메시지
        — 자기 자신이 보낸 메시지도 포함되지만 실무상 큰 문제 아님
          (보통 lastReadMessageId를 방 진입 시 최신값으로 갱신하므로 0이 됨)

      lastReadMessageId가 null일 때 처리:
        — 서비스 레이어에서 null이면 0L로 변환해서 호출 (0보다 큰 모든 메시지 = 전체 카운트)
    */
    long countByChatRoomIdAndMessageIdGreaterThanAndIsDeletedFalse(Long chatRoomId, Long lastReadMessageId);

    /*
      채팅방 메시지 목록 — 초기 조회 (cursor 없음)

      목적: GET /api/chats/{roomId}/messages 의 첫 페이지
        — messageId DESC 정렬 → 가장 최신 메시지가 앞
        — 프론트가 아래에서 위로 스크롤하면서 이전 메시지는 cursor로 추가 조회

      Pageable:
        — PageRequest.of(0, limit + 1) 로 "+1 트릭" 적용
        — size보다 1개 더 가져와서 hasNext 판단 후 잘라냄
    */
    List<Message> findByChatRoomIdAndIsDeletedFalseOrderByMessageIdDesc(Long chatRoomId, Pageable pageable);

    /*
      채팅방 메시지 목록 — 커서 조회 (cursor 이전의 더 오래된 메시지)

      동작: messageId < cursor 인 메시지들을 DESC로 조회
        — cursor 값은 이전 응답의 next_cursor (그 페이지에서 가장 오래된 messageId)
        — 반환되는 값들은 cursor보다 작은(= 더 과거의) 메시지들
    */
    List<Message> findByChatRoomIdAndIsDeletedFalseAndMessageIdLessThanOrderByMessageIdDesc(
            Long chatRoomId, Long cursor, Pageable pageable);
}
