package com.instagram.backend.domain.follow.controller;

import com.instagram.backend.domain.follow.service.FollowService;
import com.instagram.backend.global.dto.ApiResponse;
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
}
