package com.instagram.backend.domain.follow.controller;

import com.instagram.backend.domain.follow.dto.FollowListResponse;
import com.instagram.backend.domain.follow.service.FollowService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
  FollowController — 팔로우/언팔로우 API

  @RequestMapping("/api/users")
    — MemberController와 같은 base path를 사용
    — Spring은 같은 path를 여러 Controller가 나눠 쓸 수 있음
    — 중요한 건 "메서드 + 전체 경로"의 조합이 겹치지 않으면 됨
      예: MemberController의 GET /me와 FollowController의 POST /{memberId}/follow는 겹치지 않음

  @PathVariable Long memberId
    — URL 경로의 {memberId} 값을 파라미터로 받음
    — 예: POST /api/users/5/follow → memberId = 5
    — 팔로우 대상의 member_id를 URL에 포함시키는 것은 RESTful API 설계 원칙
      (리소스를 URL로 표현: "5번 유저를 팔로우한다")
*/
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /*
      POST /api/users/{memberId}/follow — 팔로우

      왜 Request Body가 없나?
        — 팔로우에 필요한 정보는 딱 두 가지: 나(JWT), 상대(URL의 memberId)
        — 둘 다 이미 있으므로 Body가 필요 없음
        — GET/DELETE처럼 Body 없이 동작하는 POST도 있음
    */
    @PostMapping("/{memberId}/follow")
    public ApiResponse<Void> follow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId) {
        followService.follow(userDetails.getMemberId(), memberId);
        return ApiResponse.success(null, "팔로우에 성공하였습니다.");
    }

    /*
      DELETE /api/users/{memberId}/follow — 언팔로우

      왜 DELETE 메서드를 사용하나?
        — REST 원칙: 리소스를 삭제할 때 DELETE 사용
        — "팔로우 관계"라는 리소스를 삭제하는 행위이므로 DELETE가 적절
        — POST로 언팔로우를 만들어도 동작은 하지만, REST 규약에 맞지 않음
    */
    @DeleteMapping("/{memberId}/follow")
    public ApiResponse<Void> unfollow(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long memberId) {
        followService.unfollow(userDetails.getMemberId(), memberId);
        return ApiResponse.success(null, "언팔로우에 성공하였습니다.");
    }

    /*
      GET /api/users/{username}/followers — 팔로워 목록 조회

      @RequestParam — URL의 쿼리 파라미터를 받음
        — 예: GET /api/users/john/followers?cursor=100&limit=20
        — required = false: 첫 페이지에서는 cursor가 없으므로 선택적
        — defaultValue = "20": limit을 안 보내면 기본 20개

      @PathVariable vs @RequestParam 차이:
        — @PathVariable: URL 경로의 일부 (/users/{username}/followers → username)
        — @RequestParam: URL 뒤의 ?key=value (cursor, limit)
        — 리소스 식별 = PathVariable, 옵션/필터 = RequestParam
    */
    @GetMapping("/{username}/followers")
    public ApiResponse<CursorPageResponse<FollowListResponse>> getFollowers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        CursorPageResponse<FollowListResponse> response =
                followService.getFollowers(userDetails.getMemberId(), username, cursor, limit);
        return ApiResponse.success(response, "팔로워 목록 조회에 성공하였습니다.");
    }

    /*
      GET /api/users/{username}/following — 팔로잉 목록 조회
      — 팔로워 목록과 동일한 구조, 데이터 방향만 다름
    */
    @GetMapping("/{username}/following")
    public ApiResponse<CursorPageResponse<FollowListResponse>> getFollowing(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        CursorPageResponse<FollowListResponse> response =
                followService.getFollowing(userDetails.getMemberId(), username, cursor, limit);
        return ApiResponse.success(response, "팔로잉 목록 조회에 성공하였습니다.");
    }
}
