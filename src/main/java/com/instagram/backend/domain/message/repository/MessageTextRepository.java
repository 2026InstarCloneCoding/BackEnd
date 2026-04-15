package com.instagram.backend.domain.message.repository;

import com.instagram.backend.domain.message.entity.MessageText;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessageTextRepository extends JpaRepository<MessageText, Long> {

    /*
      여러 메시지의 텍스트 본문을 한 번에 조회 (IN 배치 조회)

      목적: 채팅방 목록 화면의 "last_message.preview" 필드 조립용
        — 각 채팅방의 최신 메시지가 TEXT 타입이면 본문을 미리보기로 표시
        — 여러 room의 TEXT 타입 메시지를 한 번의 쿼리로 묶어서 조회 (N+1 회피)

      SELECT * FROM message_texts WHERE message_id IN (?, ?, ...)
    */
    List<MessageText> findByMessageIdIn(Collection<Long> messageIds);
}
