package com.instagram.backend.domain.story.repository;

import com.instagram.backend.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findByStoryIdAndIsDeletedFalseAndExpiresAtAfter(Long storyId, LocalDateTime now);

    List<Story> findByMemberIdInAndIsDeletedFalseAndExpiresAtAfterOrderByCreatedAtDesc(List<Long> memberIds, LocalDateTime now);
}
