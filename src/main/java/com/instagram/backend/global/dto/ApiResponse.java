package com.instagram.backend.global.dto;

/*
  import
  - JsonInclude — JSON 변환 시 어떤 필드를 포함할지 설정
  - Builder — 객체를 편하게 만드는 패턴
  - Getter — get 메서드 자동 생성
*/

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/*
  클래스 위의 어노테이션

  @Getter — 이게 없으면 이걸 직접 다 써야 함:
  public boolean getSuccess() { return this.success; }
  public T getData() { return this.data; }
  public String getMessage() { return this.message; }
  // ... 필드마다 하나씩
  Lombok이 이걸 자동으로 만들어줌.

  @JsonInclude(NON_NULL) — null인 필드는 JSON에서 빠짐:
  // 성공 시 error가 null → 아예 안 나옴
  { "success": true, "data": {...}, "message": "..." }

  // 이게 없으면 이렇게 나옴 (지저분함)
  { "success": true, "data": {...}, "message": "...", "error": null }

  <T> — 제네릭. 가장 헷갈릴 수 있는데:
  ApiResponse<MyProfileResponse>  // data에 프로필이 들어감
  ApiResponse<List<PostResponse>> // data에 게시물 목록이 들어감
  ApiResponse<Void>               // data 없음 (에러 응답)
  T 자리에 어떤 타입이든 넣을 수 있어서, 하나의 클래스로 모든 API 응답을 처리 가능.
 */

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String message;
    private final ErrorDetail error;

    @Builder
    private ApiResponse(boolean success, T data, String message, ErrorDetail error) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.error = error;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }

    // 에러 응답
    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .error(new ErrorDetail(code, message))
                .build();
    }

    @Getter
    public static class ErrorDetail {
        private final String code;
        private final String message;

        public ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
