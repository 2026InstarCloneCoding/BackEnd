package com.instagram.backend.domain.search.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.backend.domain.hashtag.entity.Hashtag;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/*
  HashtagSearchResponse — 해시태그 검색 응답 DTO

  설계:
    — 명세서의 응답 구조를 그대로 반영 (hashtags 배열)
    — 각 항목: hashtag_id, name, post_count
    — 유저 검색의 is_following 같은 관계 필드는 의도적으로 제외
      (명세 외 추가는 스코프 침범. 추후 클라이언트 요구사항 생기면 재논의)

  of() 팩토리 메서드:
    — Hashtag 엔티티 리스트를 받아 DTO 변환
    — 정렬은 Repository 의 ORDER BY 가 보장하므로 추가 정렬 없음
*/
@Getter
@Builder
public class HashtagSearchResponse {

    @JsonProperty("hashtags")
    private final List<HashtagInfo> hashtags;

    @Getter
    @Builder
    public static class HashtagInfo {

        @JsonProperty("hashtag_id")
        private final Long hashtagId;

        @JsonProperty("name")
        private final String name;

        @JsonProperty("post_count")
        private final int postCount;
    }

    public static HashtagSearchResponse of(List<Hashtag> hashtags) {
        List<HashtagInfo> hashtagInfos = hashtags.stream()
                .map(h -> HashtagInfo.builder()
                        .hashtagId(h.getHashtagId())
                        .name(h.getName())
                        .postCount(h.getPostCount())
                        .build())
                .toList();

        return HashtagSearchResponse.builder()
                .hashtags(hashtagInfos)
                .build();
    }
}
