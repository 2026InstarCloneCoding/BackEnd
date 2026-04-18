package com.instagram.backend.domain.hashtag.service;

import com.instagram.backend.domain.hashtag.dto.response.HashtagResponse;
import com.instagram.backend.domain.hashtag.entity.Hashtag;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

    private final HashtagRepository hashtagRepository;
    //해시 태그 단건 조회
    public HashtagResponse getHashtag(String name) {
        if (name == null || name.isBlank()){
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        Hashtag hashtag = hashtagRepository.findByName(name.toLowerCase())
                .orElseThrow(() -> new BusinessException(ErrorCode.HASHTAG_NOT_FOUND));
        return HashtagResponse.from(hashtag);
    }

}
