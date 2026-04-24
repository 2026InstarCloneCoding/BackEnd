package com.instagram.backend.domain.alarm.dto;

import com.instagram.backend.domain.alarm.entity.Alarm;
import com.instagram.backend.domain.alarm.entity.AlarmType;
import com.instagram.backend.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AlarmResponse {

    private Long alarmId;
    private AlarmType alarmType;
    private Long senderMemberId;
    private String senderUsername;
    private String senderProfileImageUrl;
    private Long referenceId;
    private Long secondaryReferenceId;
    private boolean isRead;
    private LocalDateTime createdAt;

    public static AlarmResponse of(Alarm alarm, Member sender) {
        return AlarmResponse.builder()
                .alarmId(alarm.getAlarmId())
                .alarmType(alarm.getAlarmType())
                .senderMemberId(sender.getMemberId())
                .senderUsername(sender.getMemberUsername())
                .senderProfileImageUrl(sender.getImageUrl())
                .referenceId(alarm.getReferenceId())
                .secondaryReferenceId(alarm.getSecondaryReferenceId())
                .isRead(alarm.getReadAt() != null)
                .createdAt(alarm.getCreatedAt())
                .build();
    }
}
