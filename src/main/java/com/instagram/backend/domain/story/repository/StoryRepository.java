package com.instagram.backend.domain.story.repository;

import com.instagram.backend.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    Optional<Story> findByStoryIdAndIsDeletedFalse(Long storyId);

    List<Story> findByMemberIdInAndIsDeletedFalseOrderByCreatedAtDesc(List<Long> memberIds);
}
