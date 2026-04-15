package com.instagram.backend.domain.chat.controller;

import com.instagram.backend.domain.chat.dto.ChatRoomCreateRequest;
import com.instagram.backend.domain.chat.dto.ChatRoomCreateResponse;
import com.instagram.backend.domain.chat.dto.ChatRoomListResponse;
import com.instagram.backend.domain.chat.service.ChatService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/*
  ChatController — 채팅방 생성 및 목록 조회 API

  @RequestMapping("/api/chats")
    — 이 컨트롤러의 모든 엔드포인트가 /api/chats로 시작
    — 향후 이슈 B(메시지 송수신)의 /api/chats/{room_id}/messages도 이 경로 아래에 추가될 예정이지만,
      별도 MessageController로 분리 가능 (도메인 책임 분리)

  인증:
    — 모든 엔드포인트는 JWT 필터를 통해 인증된 사용자만 접근 가능
    — @AuthenticationPrincipal로 로그인된 memberId를 꺼내서 서비스에 전달
*/
@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /*
      POST /api/chats — 채팅방 생성

      응답 코드: 201 CREATED
        — 리소스를 새로 생성한 경우 201이 표준 (단순 성공 200이 아님)
        — ResponseEntity.status(HttpStatus.CREATED)로 명시

      @Valid
        — ChatRoomCreateRequest의 @NotEmpty 등 검증 어노테이션 실행
        — 검증 실패 시 MethodArgumentNotValidException → GlobalExceptionHandler가 처리
    */
    @PostMapping
    public ResponseEntity<ApiResponse<ChatRoomCreateResponse>> createChatRoom(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChatRoomCreateRequest request) {
        ChatRoomCreateResponse response =
                chatService.createChatRoom(userDetails.getMemberId(), request.getMemberIds());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(response, "채팅방이 생성되었습니다.")
        );
    }

    /*
      GET /api/chats — 내 채팅방 목록 조회

      응답 데이터는 List<ChatRoomListResponse>
        — PDF 명세에 페이지네이션이 없어서 전체 목록 반환
        — 향후 사용자 증가 시 CursorPageResponse 적용 검토

      정렬 기준:
        — 서비스 레이어에서 처리 (last_message.sent_at DESC, 없으면 created_at DESC)

      반환 타입:
        — ResponseEntity<ApiResponse<...>> 로 감싸서 createChatRoom과 일관성 유지
        — 명시적 200 OK 반환. 향후 다른 상태코드(예: 304 Not Modified) 필요 시 확장 용이
    */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getMyChatRooms(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ChatRoomListResponse> response = chatService.getMyChatRooms(userDetails.getMemberId());
        return ResponseEntity.ok(
                ApiResponse.success(response, "채팅방 목록 조회에 성공하였습니다.")
        );
    }
}
