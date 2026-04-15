package com.instagram.backend.domain.message.repository;

import com.instagram.backend.domain.message.entity.MessageStory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MessageStoryRepository extends JpaRepository<MessageStory, Long> {

    /*
      여러 메시지의 공유 스토리 참조를 한 번에 조회 (IN 배치 조회)
      목적: 메시지 목록 조회 시 STORY 타입 메시지의 story_id를 한 번에 획득 (N+1 회피)
    */
    List<MessageStory> findByMessageIdIn(Collection<Long> messageIds);
}
