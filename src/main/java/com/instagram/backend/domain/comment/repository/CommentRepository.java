package com.instagram.backend.domain.comment.repository;

import com.instagram.backend.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    //삭제안된 포스트 세기
    int countByPostIdAndIsDeletedFalse(Long postId);
    //삭제안된 댓글 가져오기
    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);
    //가장 위 댓글(페이지네이션 없음) 처음 들어왔을 때
    List<Comment>findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseOrderByCommentIdAsc(Long postId, Pageable pageable);
    //가장 위 댓글(페이지네이션 있음) 다음거 볼 때
    List<Comment>findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseAndCommentIdGreaterThanOrderByCommentIdAsc( Long postId, Long cursor, Pageable pageable);

    // 대댓글 조회
    @Query("""
            SELECT c FROM Comment c WHERE c.parentCommentId IN :parentIds AND c.isDeleted = false ORDER BY c.commentId ASC
""")
    List<Comment> findRepliesByParentCommentIds(@Param("parentIds") List<Long> parentIds);

    // 좋아요 카운트 원자적 증가 (동시성 안전)
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.commentId = :commentId AND c.isDeleted = false")
    int incrementLikeCountAndIsDeletedFalse(@Param("commentId") Long commentId);

    // 좋아요 카운트 원자적 감소 (0 미만 방어 포함)
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.commentId = :commentId AND c.isDeleted = false AND c.likeCount > 0")
    int decrementLikeCountAndIsDeletedFalse(@Param("commentId") Long commentId);

    // 피드용 — 여러 게시물의 댓글 수 일괄 집계
    @Query("""
        SELECT c.postId AS postId, COUNT(c) AS count
        FROM Comment c
        WHERE c.postId IN :postIds AND c.isDeleted = false
        GROUP BY c.postId
    """)
    List<PostCommentCount> countByPostIdInAndIsDeletedFalse(@Param("postIds") Collection<Long> postIds);
}
