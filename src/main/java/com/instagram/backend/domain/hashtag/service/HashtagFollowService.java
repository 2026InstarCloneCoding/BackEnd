package com.instagram.backend.domain.hashtag.service;

import com.instagram.backend.domain.hashtag.entity.HashtagFollow;
import com.instagram.backend.domain.hashtag.repository.HashtagFollowRepository;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagFollowService {

    private final HashtagFollowRepository hashtagFollowRepository;
    private final HashtagRepository hashtagRepository;


    private void validateIds(Long memberId, Long hashtagId) {
        if (memberId == null || hashtagId == null) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
    }

    // 해시태그 팔로우
    @Transactional
    public void followHashtag(Long memberId, Long hashtagId) {
        // 값 자체 유효성 검증
        validateIds(memberId, hashtagId);

        // DB 존재 여부 확인
        hashtagRepository.findById(hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HASHTAG_NOT_FOUND));

        // 비즈니스 규칙 — 빠른 경로 체크 후 DB 유니크 제약으로 동시성 보장
        if (hashtagFollowRepository.existsByMemberIdAndHashtagId(memberId, hashtagId)) {
            throw new BusinessException(ErrorCode.ALREADY_HASHTAG_FOLLOWED);
        }
        try {
            hashtagFollowRepository.save(HashtagFollow.builder()
                    .memberId(memberId)
                    .hashtagId(hashtagId)
                    .build());
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 유니크 제약 위반 → 동일 비즈니스 예외로 변환
            throw new BusinessException(ErrorCode.ALREADY_HASHTAG_FOLLOWED);
        }
    }

    // 해시태그 언팔로우
    @Transactional
    public void unfollowHashtag(Long memberId, Long hashtagId) {
        // 값 자체 유효성 검증
        validateIds(memberId, hashtagId);

        // DB 존재 여부 확인
        HashtagFollow follow = hashtagFollowRepository
                .findByMemberIdAndHashtagId(memberId, hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HASHTAG_FOLLOW_NOT_FOUND));

        hashtagFollowRepository.delete(follow);
    }
}
