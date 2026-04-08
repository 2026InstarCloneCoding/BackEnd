package com.instagram.backend.global.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/*
  CursorPageResponse — 커서 기반 페이지네이션 공용 응답 DTO

  왜 offset 방식이 아니라 cursor 방식을 사용하나?
    — offset 방식: "10번째부터 20개 줘" → 데이터가 많아지면 느려짐 (DB가 앞의 10개를 건너뛰어야 함)
    — cursor 방식: "이 ID 이후로 20개 줘" → 항상 일정한 속도 (인덱스를 타므로)
    — 인스타그램처럼 무한 스크롤이 필요한 서비스에 적합

  제네릭 <T>를 사용하는 이유:
    — 팔로워 목록, 팔로잉 목록, 게시물 목록 등 다양한 곳에서 재사용
    — T에 FollowListResponse, PostResponse 등 어떤 타입이든 넣을 수 있음
    — ApiResponse<T>와 같은 원리

  next_cursor:
    — 다음 페이지를 요청할 때 보내야 할 커서 값
    — null이면 더 이상 데이터가 없다는 뜻 (마지막 페이지)

  has_next:
    — 다음 페이지가 있는지 여부
    — 프론트엔드가 "더 보기" 버튼을 보여줄지 결정할 때 사용
*/
@Getter
@Builder
public class CursorPageResponse<T> {

    private List<T> content;

    @JsonProperty("next_cursor")
    private String nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;

    public static <T> CursorPageResponse<T> of(List<T> content, String nextCursor, boolean hasNext) {
        return CursorPageResponse.<T>builder()
                .content(content)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }
}
