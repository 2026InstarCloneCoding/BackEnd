package com.instagram.backend.domain.story.repository;

import com.instagram.backend.domain.story.entity.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

    @Query("SELECT s FROM Story s WHERE s.storyId = :storyId AND s.isDeleted = false AND s.expiresAt > :now")
    Optional<Story> findActiveStory(@Param("storyId") Long storyId, @Param("now") LocalDateTime now);

    @Query("SELECT s FROM Story s WHERE s.memberId IN :memberIds AND s.isDeleted = false AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByMembers(@Param("memberIds") List<Long> memberIds, @Param("now") LocalDateTime now);
}
