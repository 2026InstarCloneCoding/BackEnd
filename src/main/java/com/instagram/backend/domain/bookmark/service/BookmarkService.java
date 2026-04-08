package com.instagram.backend.domain.bookmark.service;

import com.instagram.backend.domain.bookmark.entity.Bookmark;
import com.instagram.backend.domain.bookmark.repository.BookmarkRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final PostRepository postRepository;
    private final BookmarkRepository bookmarkRepository;

    //게시물 저장
    @Transactional
    public void bookmarkPost(Long memberId, Long postId){
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));//게시물 확인
        if (bookmarkRepository.existsByPostIdAndMemberId(postId,memberId)) {
            throw new BusinessException(ErrorCode.ALREADY_BOOKMARKED);
        }
        bookmarkRepository.save(Bookmark.builder()
                .postId(postId)
                .memberId(memberId)
                .build());
    }
    @Transactional
    public void unbookmarkPost(Long memberId, Long postId){
        postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));//게시물 확인

        Bookmark bookmark = bookmarkRepository.findByPostIdAndMemberId(postId, memberId)
                .orElseThrow(()-> new BusinessException(ErrorCode.BOOKMARK_NOT_FOUND));
    }
}
