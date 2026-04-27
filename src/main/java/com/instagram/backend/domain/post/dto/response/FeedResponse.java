package com.instagram.backend.domain.post.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class FeedResponse {

    @JsonProperty("post_id")
    private final Long postId;

    @JsonProperty("content")
    private final String content;

    @JsonProperty("like_count")
    private final int likeCount;

    @JsonProperty("comment_count")
    private final int commentCount;

    @JsonProperty("comment_enabled")
    private final boolean commentEnabled;

    @JsonProperty("is_liked")
    private final boolean liked;

    @JsonProperty("is_bookmarked")
    private final boolean bookmarked;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @JsonProperty("images")
    private final List<String> images;

    @JsonProperty("hashtags")
    private final List<String> hashtags;

    @JsonProperty("member")
    private final MemberInfo member;

    private FeedResponse(Post post, List<String> images, List<String> hashtags,
                         boolean isLiked, boolean isBookmarked, int commentCount) {
        Member m = post.getMember();
        this.postId = post.getPostId();
        this.content = post.getContents();
        this.likeCount = post.getLikeCount();
        this.commentCount = commentCount;
        this.commentEnabled = post.isCommentEnabled();
        this.liked = isLiked;
        this.bookmarked = isBookmarked;
        this.createdAt = post.getCreatedAt();
        this.images = images;
        this.hashtags = hashtags;
        this.member = MemberInfo.from(m);
    }

    public static FeedResponse of(Post post, List<PostImage> images, List<String> hashtags, boolean isLiked, boolean isBookmarked, int commentCount) {
        List<String> imageUrls = images.stream().map(PostImage::getImageUrl).toList();
        return new FeedResponse(post, imageUrls, hashtags, isLiked, isBookmarked, commentCount);
    }

    @Getter
    public static class MemberInfo {

        @JsonProperty("member_id")
        private final Long memberId;

        @JsonProperty("username")
        private final String username;

        @JsonProperty("image_url")
        private final String imageUrl;

        private MemberInfo(Long memberId, String username, String imageUrl) {
            this.memberId = memberId;
            this.username = username;
            this.imageUrl = imageUrl;
        }

        public static MemberInfo from(Member member) {
            return new MemberInfo(member.getMemberId(), member.getMemberUsername(), member.getImageUrl());
        }
    }
}
