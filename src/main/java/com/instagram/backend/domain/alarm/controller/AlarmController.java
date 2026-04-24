package com.instagram.backend.domain.alarm.controller;

import com.instagram.backend.domain.alarm.dto.AlarmResponse;
import com.instagram.backend.domain.alarm.service.AlarmService;
import com.instagram.backend.global.dto.ApiResponse;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping
    public ApiResponse<CursorPageResponse<AlarmResponse>> getAlarms(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        CursorPageResponse<AlarmResponse> response = alarmService.getAlarms(userDetails.getMemberId(), cursor, limit);
        return ApiResponse.success(response, "알림 목록 조회에 성공하였습니다.");
    }

    @PatchMapping("/{alarmId}/read")
    public ApiResponse<Void> readAlarm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long alarmId) {
        alarmService.readAlarm(userDetails.getMemberId(), alarmId);
        return ApiResponse.success(null, "알림을 읽음 처리하였습니다.");
    }

    @GetMapping("/unread-count")
    public ApiResponse<Long> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        long count = alarmService.getUnreadCount(userDetails.getMemberId());
        return ApiResponse.success(count, "미읽음 알림 개수 조회에 성공하였습니다.");
    }
}
