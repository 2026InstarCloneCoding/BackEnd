package com.instagram.backend.domain.message.repository;

import com.instagram.backend.domain.message.entity.MessagePost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessagePostRepository extends JpaRepository<MessagePost, Long> {

    /*
      여러 메시지의 공유 게시물 참조를 한 번에 조회 (IN 배치 조회)
      목적: 메시지 목록 조회 시 POST 타입 메시지의 post_id를 한 번에 획득 (N+1 회피)
    */
    List<MessagePost> findByMessageIdIn(Collection<Long> messageIds);
}
