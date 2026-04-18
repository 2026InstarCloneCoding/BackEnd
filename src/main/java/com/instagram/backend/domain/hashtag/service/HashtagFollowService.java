package com.instagram.backend.domain.hashtag.service;

import com.instagram.backend.domain.hashtag.entity.HashtagFollow;
import com.instagram.backend.domain.hashtag.repository.HashtagFollowRepository;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagFollowService {

    private final HashtagFollowRepository hashtagFollowRepository;
    private final HashtagRepository hashtagRepository;


    // 해시태그 팔로우
    @Transactional
    public void followHashtag(Long memberId, Long hashtagId){
        //해시태그 확인
        hashtagRepository.findById(hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HASHTAG_NOT_FOUND));
        //팔로우 중인 지 확인
        if (hashtagFollowRepository.existsByMemberIdAndHashtagId(memberId, hashtagId)) {
            throw new BusinessException(ErrorCode.ALREADY_HASHTAG_FOLLOWED);
        }
        hashtagFollowRepository.save(HashtagFollow.builder()
                .memberId(memberId)
                .hashtagId(hashtagId)
                .build());
    }
    // 해시태그 언팔로우
    @Transactional
    public void unfollowHashtag(Long memberId, Long hashtagId) {
        HashtagFollow follow = hashtagFollowRepository
                .findByMemberIdAndHashtagId(memberId, hashtagId)
                .orElseThrow(() -> new BusinessException(ErrorCode.HASHTAG_FOLLOW_NOT_FOUND));

        hashtagFollowRepository.delete(follow);
    }
}
