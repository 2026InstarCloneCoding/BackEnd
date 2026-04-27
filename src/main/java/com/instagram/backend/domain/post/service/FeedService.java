package com.instagram.backend.domain.post.service;

import com.instagram.backend.domain.bookmark.repository.BookmarkRepository;
import com.instagram.backend.domain.comment.repository.CommentRepository;
import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.hashtag.entity.PostHashtag;
import com.instagram.backend.domain.hashtag.repository.HashtagFollowRepository;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.domain.hashtag.repository.PostHashtagRepository;
import com.instagram.backend.domain.post.dto.response.FeedListResponse;
import com.instagram.backend.domain.post.dto.response.FeedResponse;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import com.instagram.backend.domain.post.repository.PostImageRepository;
import com.instagram.backend.domain.post.repository.PostLikeRepository;
import com.instagram.backend.domain.post.repository.PostRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final CommentRepository commentRepository;
    private final FollowRepository followRepository;
    private final HashtagFollowRepository hashtagFollowRepository;
    private final PostHashtagRepository postHashtagRepository;
    private final HashtagRepository hashtagRepository;

    public FeedListResponse getFeed(Long memberId, Long cursor, int size) {
        if (size < 1 || size > 30) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }

        // 팔로우한 멤버 ID 목록
        List<Long> followingMemberIds = followRepository.findFollowingIdsByFollowerId(memberId);

        // 팔로우한 해시태그의 게시물 ID 목록
        List<Long> followedHashtagIds = hashtagFollowRepository.findHashtagIdsByMemberId(memberId);
        List<Long> hashtagPostIds = followedHashtagIds.isEmpty() ? List.of() : postHashtagRepository.findPostIdsByHashtagIdIn(followedHashtagIds);

        // 팔로우한 사람도 해시태그도 없으면 빈 피드 반환
        if (followingMemberIds.isEmpty() && hashtagPostIds.isEmpty()) {
            return FeedListResponse.empty();
        }

        // 피드 게시물 조회 (size+1 로 다음 페이지 존재 여부 확인)
        PageRequest pageable = PageRequest.of(0, size + 1);
        List<Post> rawPosts = (cursor == null) ? postRepository.findFeedPostsFirst(followingMemberIds, hashtagPostIds, pageable) : postRepository.findFeedPostsWithCursor(followingMemberIds, hashtagPostIds, cursor, pageable);

        boolean hasMore = rawPosts.size() > size;
        List<Post> posts = hasMore ? rawPosts.subList(0, size) : rawPosts;
        Long nextCursor = hasMore ? posts.get(posts.size() - 1).getPostId() : null;

        if (posts.isEmpty()) {
            return FeedListResponse.empty();
        }

        List<Long> postIds = posts.stream().map(Post::getPostId).toList();

        // 이미지 일괄 조회 — ToMany IN 쿼리 + groupingBy Map
        Map<Long, List<PostImage>> imageMap = postImageRepository
                .findByPostPostIdInOrderBySortOrderAsc(postIds)
                .stream()
                .collect(Collectors.groupingBy(img -> img.getPost().getPostId()));

        // 해시태그 이름 일괄 조회 — ToMany IN 쿼리 + groupingBy Map
        Map<Long, List<Long>> postToHashtagIds = postHashtagRepository
                .findByPostIdIn(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PostHashtag::getPostId,
                        Collectors.mapping(PostHashtag::getHashtagId, Collectors.toList())
                ));

        List<Long> allHashtagIds = postToHashtagIds.values().stream()
                .flatMap(List::stream).distinct().toList();
        Map<Long, String> hashtagNameMap = allHashtagIds.isEmpty()
                ? Map.of()
                : hashtagRepository.findByHashtagIdIn(allHashtagIds).stream()
                        .collect(Collectors.toMap(h -> h.getHashtagId(), h -> h.getName()));

        // 좋아요/저장 Set 조회 — O(1) 조회로 N+1 방지
        Set<Long> likedPostIds = postLikeRepository.findPostIdsByMemberIdAndPostIdIn(memberId, postIds);
        Set<Long> bookmarkedPostIds = bookmarkRepository.findPostIdsByMemberIdAndPostIdIn(memberId, postIds);

        // 댓글 수 일괄 집계
        Map<Long, Integer> commentCountMap = commentRepository
                .countByPostIdInAndIsDeletedFalse(postIds)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // 응답 조립
        List<FeedResponse> feedItems = posts.stream().map(post -> {
            Long pId = post.getPostId();
            List<PostImage> images = imageMap.getOrDefault(pId, List.of());
            List<String> hashtags = postToHashtagIds
                    .getOrDefault(pId, List.of())
                    .stream()
                    .map(hid -> hashtagNameMap.getOrDefault(hid, ""))
                    .filter(s -> !s.isEmpty())
                    .toList();
            return FeedResponse.of(
                    post,
                    images,
                    hashtags,
                    likedPostIds.contains(pId),
                    bookmarkedPostIds.contains(pId),
                    commentCountMap.getOrDefault(pId, 0)
            );
        }).toList();

        return FeedListResponse.of(feedItems, nextCursor, hasMore);
    }
}
