package com.instagram.backend.domain.bookmark.service;

import com.instagram.backend.domain.bookmark.dto.BookmarkPostResponse;
import com.instagram.backend.domain.bookmark.entity.Bookmark;
import com.instagram.backend.domain.bookmark.repository.BookmarkRepository;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import com.instagram.backend.domain.post.repository.PostImageRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
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
        bookmarkRepository.delete(bookmark); // 저장 취소
    }

    /*
      저장한 게시물 목록 조회 (커서 기반 페이지네이션)

      전체 흐름 — 3단계 배치 조회로 N+1 문제 회피
        1) bookmarks 테이블에서 내가 저장한 (limit+1)개를 bookmark_id DESC 순으로 조회
        2) 1단계에서 얻은 post_id 목록으로 posts 테이블 IN 조회 (삭제되지 않은 것만)
        3) 2단계에서 살아있는 post_id 목록으로 post_images IN 조회 (대표 이미지 sort_order=0)

      왜 이렇게 단계를 나누나?
        — Bookmark 엔티티에는 post_id만 있고, Post와 PostImage는 별도 테이블
        — 항목마다 개별 조회하면 20개 항목 → 1+20+20 = 41 쿼리 (N+1 문제)
        — IN 절로 묶으면 → 1+1+1 = 3 쿼리만 발생

      삭제된 게시물은 어떻게 처리하나?
        — bookmarks에는 남아있지만 posts.is_deleted=true 인 경우
        — 2단계 IN 조회에서 자동으로 제외됨
        — 3단계 결과에도 안 들어가므로, 응답에는 살아있는 post들만 포함됨

      커서 방식 (FollowService와 동일한 패턴):
        — limit+1개를 조회해서 hasNext 판별
        — 응답에는 limit개만 노출, 마지막 항목의 bookmark_id가 next_cursor
    */
    public CursorPageResponse<BookmarkPostResponse> getMyBookmarks(Long memberId, String cursor, int limit) {
        // 1. limit 검증 (1~50)
        validateLimit(limit);

        // 2. 커서 기반 bookmarks 조회 (+1개로 hasNext 판별)
        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Bookmark> bookmarks;

        if (cursor == null) {
            // 첫 페이지
            bookmarks = bookmarkRepository.findByMemberIdOrderByBookmarkIdDesc(memberId, pageRequest);
        } else {
            // 다음 페이지: 이전 커서보다 작은 bookmark_id만
            bookmarks = bookmarkRepository.findByMemberIdAndBookmarkIdLessThanOrderByBookmarkIdDesc(
                    memberId, Long.parseLong(cursor), pageRequest);
        }

        // 3. hasNext 판별 후 실제 limit개만 사용
        boolean hasNext = bookmarks.size() > limit;
        List<Bookmark> resultBookmarks = hasNext ? bookmarks.subList(0, limit) : bookmarks;

        // 4. 빈 결과면 즉시 반환 (불필요한 쿼리 방지)
        if (resultBookmarks.isEmpty()) {
            return CursorPageResponse.of(new ArrayList<>(), null, false);
        }

        // 5. post_id 목록 추출 → posts IN 조회 (삭제된 것 제외)
        List<Long> postIds = resultBookmarks.stream().map(Bookmark::getPostId).toList();
        List<Post> posts = postRepository.findByPostIdInAndIsDeletedFalse(postIds);

        // post_id → Post 매핑 (조회 결과를 Map으로 만들어 O(1) 룩업)
        Map<Long, Post> postMap = new HashMap<>();
        for (Post post : posts) {
            postMap.put(post.getPostId(), post);
        }

        // 6. 살아있는 post_id로 대표 이미지(sort_order=0) IN 조회
        List<Long> alivePostIds = posts.stream().map(Post::getPostId).toList();
        Map<Long, String> thumbnailMap = new HashMap<>();
        if (!alivePostIds.isEmpty()) {
            List<PostImage> thumbnails = postImageRepository.findByPostPostIdInAndSortOrder(alivePostIds, 0);
            for (PostImage image : thumbnails) {
                thumbnailMap.put(image.getPost().getPostId(), image.getImageUrl());
            }
        }

        // 7. bookmarks 순서대로 응답 DTO 생성 (삭제된 게시물은 자동 제외)
        //    — bookmark_id DESC 순서를 유지해야 하므로 resultBookmarks를 기준으로 순회
        List<BookmarkPostResponse> content = new ArrayList<>();
        for (Bookmark bookmark : resultBookmarks) {
            Post post = postMap.get(bookmark.getPostId());
            if (post == null) {
                continue; // 삭제된 게시물은 건너뜀
            }
            String thumbnailUrl = thumbnailMap.get(post.getPostId());
            content.add(BookmarkPostResponse.of(post.getPostId(), thumbnailUrl));
        }

        // 8. next_cursor = 마지막 bookmark의 bookmark_id (정렬 기준이 bookmark_id이므로)
        //    — 주의: post가 삭제돼서 content에서 제외돼도 cursor는 bookmarks 기준으로 산출
        //      그래야 다음 페이지에서 누락 없이 이어서 조회됨
        String nextCursor = hasNext
                ? String.valueOf(resultBookmarks.get(resultBookmarks.size() - 1).getBookmarkId())
                : null;

        return CursorPageResponse.of(content, nextCursor, hasNext);
    }

    /*
      limit 검증 — API 명세에 따라 1~50 범위만 허용
      FollowService와 동일한 정책
    */
    private void validateLimit(int limit) {
        if (limit < 1 || limit > 50) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }
    }
}
