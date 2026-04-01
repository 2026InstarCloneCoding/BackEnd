package com.instagram.backend.domain.member.controller;

import com.instagram.backend.domain.member.dto.*;
import com.instagram.backend.domain.member.service.MemberService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/*
  @RestController — @Controller + @ResponseBody
    — 이 클래스의 모든 메서드가 JSON 응답을 반환함을 선언
    — @Controller만 쓰면 HTML 뷰를 반환, @RestController는 JSON API용

  @RequestMapping("/api/users")
    — 이 컨트롤러의 모든 엔드포인트가 /api/users로 시작
    — 메서드별 @GetMapping, @PatchMapping은 이 경로 뒤에 추가됨

  @AuthenticationPrincipal CustomUserDetails userDetails
    — JWT 필터가 토큰을 검증한 후 SecurityContext에 저장한 유저 정보를 꺼냄
    — userDetails.getMemberId()로 현재 로그인한 유저의 ID를 얻을 수 있음

  @Valid — Request Body의 Bean Validation 어노테이션(@Size 등)을 실행
    — 검증 실패 시 MethodArgumentNotValidException → GlobalExceptionHandler가 처리
*/
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // GET /api/users/me — 내 프로필 조회
    @GetMapping("/me")
    public ApiResponse<MyProfileResponse> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        MyProfileResponse response = memberService.getMyProfile(userDetails.getMemberId());
        return ApiResponse.success(response, "프로필 조회에 성공하였습니다.");
    }

    // PATCH /api/users/me — 프로필 수정
    @PatchMapping("/me")
    public ApiResponse<ProfileUpdateResponse> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        ProfileUpdateResponse response = memberService.updateProfile(userDetails.getMemberId(), request);
        return ApiResponse.success(response, "프로필이 수정되었습니다.");
    }

    // GET /api/users/{username} — 다른 유저 프로필 조회
    @GetMapping("/{username}")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable String username) {
        UserProfileResponse response = memberService.getUserProfile(userDetails.getMemberId(), username);
        return ApiResponse.success(response, "프로필 조회에 성공하였습니다.");
    }
}
