package com.instagram.backend.domain.message.controller;

import com.instagram.backend.domain.message.dto.request.MessageSendRequest;
import com.instagram.backend.domain.message.dto.response.MessageResponse;
import com.instagram.backend.domain.message.service.MessageService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import com.instagram.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/*
  MessageController — 채팅방 메시지 전송/조회 API

  @RequestMapping("/api/chats/{roomId}/messages")
    — 모든 엔드포인트가 특정 채팅방에 귀속된 리소스
    — Chat 도메인의 메시지 관련 API를 별도 컨트롤러로 분리 (도메인 책임 분리)

  인증:
    — JWT 필터를 통해 인증된 사용자만 접근 가능
    — @AuthenticationPrincipal로 memberId를 꺼내 서비스에 전달

  @Valid 미사용:
    — 전송 요청은 dtype에 따라 필수 필드가 달라지므로 Bean Validation만으로 처리 불가
    — 서비스 레이어에서 dtype 분기 후 수동 검증
*/
@RestController
@RequestMapping("/api/chats/{roomId}/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    /*
      POST /api/chats/{roomId}/messages — 메시지 전송

      응답 코드: 201 CREATED
        — 신규 리소스 생성이므로 표준 Created 사용
        — 채팅방 생성 API(#36)와 같은 패턴
    */
    @PostMapping
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @Valid @RequestBody MessageSendRequest request) {

        // 인증 필터가 이미 통과시킨 상태라 userDetails는 non-null이 정상이지만,
        // 향후 필터 설정 변경 시 NPE → 500 누출을 방지하기 위한 명시적 방어
        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        MessageResponse response = messageService.sendMessage(
                userDetails.getMemberId(), roomId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(response, "메시지가 전송되었습니다.")
        );
    }

    /*
      GET /api/chats/{roomId}/messages — 메시지 목록 조회 (커서 페이지네이션)

      쿼리 파라미터:
        — cursor: 이전 응답의 next_cursor. 최초 요청 시 생략
        — limit : 한 페이지 크기 (생략 시 기본 30, 최대 100)

      응답 구조(CursorPageResponse):
        — content: MessageResponse 리스트 (messageId DESC 정렬)
        — next_cursor: 다음 페이지 요청에 사용할 cursor (마지막 페이지면 null)
        — has_next: 다음 페이지 존재 여부
    */
    @GetMapping
    public ResponseEntity<ApiResponse<CursorPageResponse<MessageResponse>>> getMessages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long roomId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {

        if (userDetails == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        CursorPageResponse<MessageResponse> response =
                messageService.getMessages(userDetails.getMemberId(), roomId, cursor, limit);

        return ResponseEntity.ok(
                ApiResponse.success(response, "메시지 목록 조회에 성공하였습니다.")
        );
    }
}
