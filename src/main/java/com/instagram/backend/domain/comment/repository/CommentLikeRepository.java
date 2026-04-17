package com.instagram.backend.domain.comment.repository;

import com.instagram.backend.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    //좋아요 이미 했는 지 확인하기 위해서 사용
    boolean existsByMemberIdAndCommentId(Long memberId, Long commentId);
    // 좋아요 취소 시 삭제할 부분 찾기
    Optional<CommentLike> findByMemberIdAndCommentId(Long memberId, Long commentId);
}
