package com.instagram.backend.domain.post.service;

import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.post.dto.PostCreateRequest;
import com.instagram.backend.domain.post.dto.PostCreateResponse;
import com.instagram.backend.domain.post.dto.PostResponse;
import com.instagram.backend.domain.post.dto.PostUpdateRequest;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import com.instagram.backend.domain.post.repository.PostImageRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final MemberRepository memberRepository;

    // 게시물 단건 조회
    public PostResponse getPost(Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        return PostResponse.from(post);
    }

    // 게시물 생성
    @Transactional
    public PostCreateResponse createPost(Long memberId, PostCreateRequest request) {
        // 이미지 빈 배열 검증
        if (request.getImages() == null || request.getImages().isEmpty()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        // 이미지 10개 초과 검증
        if (request.getImages().size() > 10) {
            throw new BusinessException(ErrorCode.EXCEED_IMAGE_LIMIT);
        }
        // 내용 2200자 초과 검증
        if (request.getPostContents() != null && request.getPostContents().length() > 2200) {
            throw new BusinessException(ErrorCode.EXCEED_CONTENT_LENGTH);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 게시물 저장
        Post post = Post.builder()
                .member(member)
                .contents(request.getPostContents())
                .commentEnabled(request.isCommentEnabled())
                .build();
        postRepository.save(post);

        // 이미지 저장
        for (int i = 0; i < request.getImages().size(); i++) {
            var imageReq = request.getImages().get(i);
            PostImage postImage = PostImage.builder()
                    .post(post)
                    .imageUrl(imageReq.getPostImageUrl())
                    .imageType(imageReq.getPostImageType())
                    .imageName(imageReq.getPostImageName())
                    .imageUuid(imageReq.getPostImageUuid())
                    .altText(imageReq.getPostImageAltText())
                    .sortOrder(i)
                    .build();
            postImageRepository.save(postImage);
        }

        return PostCreateResponse.from(post);
    }

    // 게시물 수정
    @Transactional
    public PostResponse updatePost(Long memberId, Long postId, PostUpdateRequest request) {
        // 내용 2200자 초과 검증
        if (request.getPostContents() != null && request.getPostContents().length() > 2200) {
            throw new BusinessException(ErrorCode.EXCEED_CONTENT_LENGTH);
        }

        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 본인 게시물인지 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        post.update(request.getPostContents(), request.isCommentEnabled());
        return PostResponse.from(post);
    }

    // 게시물 삭제 (소프트 딜리트)
    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 본인 게시물인지 확인
        if (!post.getMember().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        post.softDelete();
        // save() 불필요 — @Transactional 안에서 필드 변경 시 자동 UPDATE (Dirty Checking)
    }
}
