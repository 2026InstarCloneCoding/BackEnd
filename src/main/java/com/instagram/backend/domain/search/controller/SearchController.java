package com.instagram.backend.domain.search.controller;

import com.instagram.backend.domain.search.dto.response.UserSearchResponse;
import com.instagram.backend.domain.search.service.SearchService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
  SearchController — 검색 API

  @RequestMapping("/api/search")
    — 검색 관련 엔드포인트를 하나의 컨트롤러에 모음
    — 향후 해시태그 검색(GET /api/search/hashtags) 추가 시에도 같은 컨트롤러에 배치

  인증:
    — 모든 엔드포인트는 JWT 인증 필요 (@AuthenticationPrincipal)
    — is_following 판단에 로그인 유저의 memberId가 필요하기 때문
*/
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /*
      GET /api/search/users — 유저 검색

      쿼리 파라미터:
        — q (필수): 검색어. username 또는 name에 대해 LIKE 검색
        — limit (선택): 결과 수. 기본 10, 최대 50

      응답:
        — members 배열 안에 각 유저의 기본 정보 + is_following
        — 검색 결과가 없으면 빈 배열 반환 (404가 아닌 200 + 빈 리스트)
    */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<UserSearchResponse>> searchUsers(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            // required=false로 받는 이유: 프레임워크 기본 예외(MissingServletRequestParameterException) 대신
            // 명세서에 정의된 MISSING_REQUIRED_FIELD 에러코드를 서비스 레이어에서 직접 반환하기 위함
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer limit) {

        UserSearchResponse response = searchService.searchUsers(
                userDetails.getMemberId(), q, limit);

        return ResponseEntity.ok(
                ApiResponse.success(response, "유저 검색에 성공하였습니다.")
        );
    }
}
