package com.instagram.backend.domain.post.service;

import com.instagram.backend.domain.bookmark.repository.BookmarkRepository;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.hashtag.entity.Hashtag;
import com.instagram.backend.domain.hashtag.entity.PostHashtag;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.domain.hashtag.repository.PostHashtagRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.post.dto.PostCreateRequest;
import com.instagram.backend.domain.post.dto.PostCreateResponse;
import com.instagram.backend.domain.post.dto.PostResponse;
import com.instagram.backend.domain.post.dto.PostUpdateRequest;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import com.instagram.backend.domain.post.repository.PostImageRepository;
import com.instagram.backend.domain.post.repository.PostLikeRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final HashtagRepository hashtagRepository;
    private final PostHashtagRepository postHashtagRepository;

    // 게시물 단건 조회
    public PostResponse getPost(Long memberId, Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        List<PostImage> images = postImageRepository.findByPostPostIdOrderBySortOrderAsc(postId);
        boolean isLiked = postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
        boolean isBookmarked = bookmarkRepository.existsByPostIdAndMemberId(postId, memberId);
        int commentCount = commentRepository.countByPostIdAndIsDeletedFalse(postId);

        return PostResponse.of(post, images, isLiked, isBookmarked, commentCount);
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
        //해시태그 추출과 저장
        saveHashtags(post.getPostId(), request.getPostContents(), request.getHashtags());

        return PostCreateResponse.from(post);
    }

    // 게시물 수정
    @Transactional
    public void updatePost(Long memberId, Long postId, PostUpdateRequest request) {
        // 내용 2200자 초과 검증
        if (request.getPostContents() != null && request.getPostContents().length() > 2200) {
            throw new BusinessException(ErrorCode.EXCEED_CONTENT_LENGTH);
        }

        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 본인 게시물인지 확인
        if (!post.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        post.update(request.getPostContents(), request.isCommentEnabled());
    }

    // 게시물 삭제 (소프트 딜리트)
    @Transactional
    public void deletePost(Long memberId, Long postId) {
        Post post = postRepository.findByPostIdAndIsDeletedFalse(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 본인 게시물인지 확인
        if (!post.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 연결된 해시태그 카운트 감소 — findByPostId 한 번만 조회해서 재사용
        List<PostHashtag> postHashtags = postHashtagRepository.findByPostId(postId);
        if (!postHashtags.isEmpty()) {
            List<Long> linkedHashtagIds = postHashtags.stream()
                    .map(PostHashtag::getHashtagId).toList();
            List<Hashtag> hashtags = hashtagRepository.findByHashtagIdIn(linkedHashtagIds);
            hashtags.forEach(Hashtag::decreasePostCount);
            postHashtagRepository.deleteAll(postHashtags);
        }

        post.softDelete();
    }

    private void saveHashtags(Long postId, String contents, List<String> requestTags) {
        //본문에서 태그 추출
        Set<String> tagNames = new HashSet<>();
        if (contents != null) {
            Matcher matcher = Pattern.compile("#([\\w가-힣]+)").matcher(contents);//정규표현식을 자바가 읽을 수 있게 컴파일 된 것을 검색
            while (matcher.find()) { //find : 해시태그 있는 지 탐색
                tagNames.add(matcher.group(1).toLowerCase());//group(1): 정규표현식읠 그룹캐처 기능 : 정규표현식의 첫번 째괄호랑 매칭되는 문자열 가져오기
            }
        }

        // null·blank 원소 필터링
        if (requestTags != null) {
            requestTags.stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> tag.trim().toLowerCase())
                    .forEach(tagNames::add);
        }

        if (tagNames.isEmpty()) return;

        // N+1 → 배치 조회 후 없는 것만 saveAll
        List<Hashtag> existing = hashtagRepository.findByNameIn(new ArrayList<>(tagNames));
        Map<String, Hashtag> hashtagMap = existing.stream()
                .collect(Collectors.toMap(Hashtag::getName, h -> h));

        List<Hashtag> newHashtags = tagNames.stream()
                .filter(name -> !hashtagMap.containsKey(name))
                .map(name -> Hashtag.builder().name(name).build())
                .collect(Collectors.toList());

        if (!newHashtags.isEmpty()) {
            try {
                hashtagRepository.saveAll(newHashtags)
                        .forEach(h -> hashtagMap.put(h.getName(), h));
            } catch (DataIntegrityViolationException e) {
                // 동시 삽입 충돌 시 이미 저장된 행 재조회
                hashtagRepository.findByNameIn(
                        newHashtags.stream().map(Hashtag::getName).toList()
                ).forEach(h -> hashtagMap.put(h.getName(), h));
            }
        }

        for (String tagName : tagNames) {
            Hashtag hashtag = hashtagMap.get(tagName);
            if (hashtag == null) continue;
            hashtag.increasePostCount();
            postHashtagRepository.save(PostHashtag.builder()
                    .postId(postId)
                    .hashtagId(hashtag.getHashtagId())
                    .build());
        }
    }
}
