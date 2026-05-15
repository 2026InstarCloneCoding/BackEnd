package com.instagram.backend.domain.post.service;

import com.instagram.backend.domain.alarm.entity.Alarm;
import com.instagram.backend.domain.alarm.service.AlarmService;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostLike;
import com.instagram.backend.domain.post.repository.PostLikeRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)// 나중에 조회 기능이 올 수 있어서 미리 깔아 놓기(실무에서 정석으로 사용)
public class PostLikeService {

    private final PostRepository postRepository;
    private final PostLikeRepository postLikeRepository;
    private final AlarmService alarmService;

    //좋아요 up
    @Transactional
    public void likePost(Long memberId, Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        try {
            postLikeRepository.save(PostLike.builder()
                    .postId(postId)
                    .memberId(memberId)
                    .build());
        } catch (DataIntegrityViolationException e) {
            String msg = e.getMostSpecificCause().getMessage();
            if (msg != null && msg.contains("uk_post_likes_post_member")) {
                throw new BusinessException(ErrorCode.ALREADY_LIKED);
            }
            throw e; // FK/NOT NULL 등 다른 제약 위반은 그대로 전파
        }

        post.increaseLikeCount(); // @Version으로 lost update 방지

        alarmService.createAlarm(Alarm.ofPostLike(post.getMember().getMemberId(), memberId, postId));
    }

    //좋아요 캔슬
    @Transactional
    public void unlikePost(Long memberId, Long postId){
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(()-> new BusinessException(ErrorCode.POST_NOT_FOUND));//존재하는 게시물인지 체크

        PostLike postLike = postLikeRepository.findByPostIdAndMemberId(postId,memberId)
                .orElseThrow(()-> new BusinessException(ErrorCode.LIKE_NOT_FOUND));//게시물에 좋아요 있는지 췤

        postLikeRepository.delete(postLike);//좋아요 삭제

        post.decreaseLikeCount();//게시물의 좋아요 카운트 down
    }
}
