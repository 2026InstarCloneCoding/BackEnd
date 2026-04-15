package com.instagram.backend.domain.message.repository;

import com.instagram.backend.domain.message.entity.MessageImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessageImageRepository extends JpaRepository<MessageImage, Long> {

    /*
      여러 메시지의 이미지 본문을 한 번에 조회 (IN 배치 조회)

      목적: 메시지 목록 조회 시 IMAGE 타입 메시지의 부가 정보를 한 번에 가져오기 위함
        — 각 메시지마다 따로 조회하면 N+1 발생
        — SELECT * FROM message_images WHERE message_id IN (?, ?, ...)
    */
    List<MessageImage> findByMessageIdIn(Collection<Long> messageIds);
}
