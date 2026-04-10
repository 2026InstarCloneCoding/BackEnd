package com.instagram.backend.domain.bookmark.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/*
  BookmarkPostResponse — "저장한 게시물 목록" 전용 응답 DTO

  왜 PostResponse를 재사용하지 않고 새로 만들었나?
    — 인스타그램 "저장됨" 화면은 썸네일 그리드 형태 (post_id + 대표 이미지만 필요)
    — PostResponse에는 content, like_count, comment_count, member 등 무거운 필드가 많음
    — 목록 API에서 그 모든 데이터를 매번 채우면 응답 크기 + 쿼리 수가 급증
    — 상세 정보가 필요할 때만 GET /api/posts/{postId}로 추가 조회하는 패턴
    — 트레이드오프: 응답 가벼움 ↔ 상세 보기 시 추가 요청 1회
*/
@Getter
public class BookmarkPostResponse {

    @JsonProperty("post_id")
    private final Long postId;

    @JsonProperty("thumbnail_url")
    private final String thumbnailUrl;

    private BookmarkPostResponse(Long postId, String thumbnailUrl) {
        this.postId = postId;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static BookmarkPostResponse of(Long postId, String thumbnailUrl) {
        return new BookmarkPostResponse(postId, thumbnailUrl);
    }
}
