package com.instagram.backend.domain.post.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.post.entity.Post;
import com.instagram.backend.domain.post.entity.PostImage;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponse {

    @JsonProperty("post_id")
    private final Long postId;

    @JsonProperty("content")
    private final String content;

    @JsonProperty("comment_enabled")
    private final boolean commentEnabled;

    @JsonProperty("like_count")
    private final int likeCount;

    @JsonProperty("comment_count")
    private final int commentCount;

    @JsonProperty("is_liked")
    private final boolean liked;

    @JsonProperty("is_bookmarked")
    private final boolean bookmarked;

    @JsonProperty("created_at")
    private final LocalDateTime createdAt;

    @JsonProperty("images")
    private final List<ImageInfo> images;

    @JsonProperty("member")
    private final MemberInfo member;

    public static PostResponse of(Post post, List<PostImage> images,
                                  boolean isLiked, boolean isBookmarked, int commentCount) {
        return new PostResponse(post, images, isLiked, isBookmarked, commentCount);
    }

    private PostResponse(Post post, List<PostImage> images,
                         boolean isLiked, boolean isBookmarked, int commentCount) {
        this.postId = post.getPostId();
        this.content = post.getContents();
        this.commentEnabled = post.isCommentEnabled();
        this.likeCount = post.getLikeCount();
        this.commentCount = commentCount;
        this.liked = isLiked;
        this.bookmarked = isBookmarked;
        this.createdAt = post.getCreatedAt();
        this.images = images.stream().map(ImageInfo::from).toList();
        this.member = MemberInfo.from(post.getMember());
    }

    @Getter
    public static class ImageInfo {

        @JsonProperty("image_url")
        private final String imageUrl;

        @JsonProperty("sort_order")
        private final int sortOrder;

        private ImageInfo(String imageUrl, int sortOrder) {
            this.imageUrl = imageUrl;
            this.sortOrder = sortOrder;
        }

        public static ImageInfo from(PostImage postImage) {
            return new ImageInfo(postImage.getImageUrl(), postImage.getSortOrder());
        }
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
            return new MemberInfo(member.getId(), member.getMemberUsername(), member.getImageUrl());
        }
    }
}
