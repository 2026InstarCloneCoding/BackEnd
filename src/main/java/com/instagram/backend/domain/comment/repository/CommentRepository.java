package com.instagram.backend.domain.comment.repository;

import com.instagram.backend.domain.comment.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    //삭제안된 포스트 세기
    int countByPostIdAndIsDeletedFalse(Long postId);
    //삭제안된 댓글 가져오기
    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long postId);
    //가장 위 댓글(페이지네이션 없음)
    List<Comment>findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseOrderByCommentIdAsc(Long postId, Pageable pageable);
    //가장 위 댓글(페이지네이션 있음)
    List<Comment>findByPostIdAndParentCommentIdIsNullAndIsDeletedFalseAndCommentIdGreaterThanOrderByCommentIdAsc( Long postId, Long cursor, Pageable pageable);

    // 대댓글 조회
    @Query("""
            SELECT c FROM Comment c WHERE c.parentCommentId IN :parentIds AND c.idDeleted = false ORDER BY c.commentId ASC
""")
    List<Comment> findRepliesByParentCommentIds(@Param("parentIds") List<Long> parentIds);
}
