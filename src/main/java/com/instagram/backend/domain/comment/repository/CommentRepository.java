package com.instagram.backend.domain.comment.repository;

import com.instagram.backend.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    int countByPostIdAndIsDeletedFalse(Long postId);
}
