package com.instagram.backend.domain.bookmark.controller;

import com.instagram.backend.domain.bookmark.dto.BookmarkPostResponse;
import com.instagram.backend.domain.bookmark.service.BookmarkService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
  BookmarkController — 저장한 게시물 목록 조회 전용 컨트롤러

  왜 PostController에 넣지 않고 별도 컨트롤러를 만들었나?
    — PostController는 "게시물 자체"의 CRUD + 좋아요/북마크 토글을 담당
    — "내가 저장한 게시물 목록"은 user의 컬렉션 조회 → /api/users/me/bookmarks 경로
    — base path가 /api/users로 다르고, 도메인 책임도 bookmark 쪽이므로 분리가 자연스러움
    — Spring은 같은 base path(/api/users)를 여러 컨트롤러가 공유 가능하므로 충돌 없음

  왜 /me 경로를 사용하나?
    — JWT에서 본인 식별이 가능하므로 URL에 member_id를 노출할 필요가 없음
    — /me 패턴은 "현재 로그인한 사용자"를 나타내는 RESTful 관용구
    — 보안적으로도 다른 사람의 북마크 목록은 조회 불가 (private 데이터)
*/
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;

    /*
      GET /api/users/me/bookmarks — 저장한 게시물 목록 조회

      쿼리 파라미터:
        — cursor (선택): 이전 페이지의 마지막 bookmark_id, 첫 페이지는 생략
        — limit (선택, 기본 20): 한 번에 가져올 개수, 1~50 범위

      응답: CursorPageResponse<BookmarkPostResponse>
        — content: [{ post_id, thumbnail_url }, ...]
        — next_cursor: 다음 페이지 요청 시 전달할 커서 (없으면 null)
        — has_next: 다음 페이지 존재 여부
    */
    @GetMapping("/me/bookmarks")
    public ApiResponse<CursorPageResponse<BookmarkPostResponse>> getMyBookmarks(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        CursorPageResponse<BookmarkPostResponse> response =
                bookmarkService.getMyBookmarks(userDetails.getMemberId(), cursor, limit);
        return ApiResponse.success(response, "저장한 게시물 목록 조회에 성공하였습니다.");
    }
}
