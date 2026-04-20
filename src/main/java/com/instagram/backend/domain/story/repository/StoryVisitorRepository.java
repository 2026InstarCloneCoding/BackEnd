package com.instagram.backend.domain.story.repository;

import com.instagram.backend.domain.story.entity.StoryVisitor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryVisitorRepository extends JpaRepository<StoryVisitor, Long> {

    boolean existsByStoryIdAndMemberId(Long storyId, Long memberId);

    List<StoryVisitor> findByStoryIdOrderByCreatedAtDesc(Long storyId);

    void deleteAllByStoryId(Long storyId);
}
