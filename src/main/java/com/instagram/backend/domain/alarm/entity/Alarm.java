package com.instagram.backend.domain.alarm.entity;

import com.instagram.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AlarmType alarmType;

    @Column(nullable = false)
    private Long targetMemberId;

    @Column(nullable = false)
    private Long senderMemberId;

    private Long referenceId;

    private Long secondaryReferenceId;

    private LocalDateTime readAt;

    @Builder
    private Alarm(AlarmType alarmType, Long targetMemberId, Long senderMemberId,
                  Long referenceId, Long secondaryReferenceId) {
        this.alarmType = alarmType;
        this.targetMemberId = targetMemberId;
        this.senderMemberId = senderMemberId;
        this.referenceId = referenceId;
        this.secondaryReferenceId = secondaryReferenceId;
    }

    public static Alarm ofFollow(Long targetMemberId, Long senderMemberId) {
        return Alarm.builder()
                .alarmType(AlarmType.FOLLOW)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .build();
    }

    public static Alarm ofPostLike(Long targetMemberId, Long senderMemberId, Long postId) {
        return Alarm.builder()
                .alarmType(AlarmType.POST_LIKE)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(postId)
                .build();
    }

    public static Alarm ofComment(Long targetMemberId, Long senderMemberId, Long commentId, Long postId) {
        return Alarm.builder()
                .alarmType(AlarmType.COMMENT)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(commentId)
                .secondaryReferenceId(postId)
                .build();
    }

    public static Alarm ofCommentLike(Long targetMemberId, Long senderMemberId, Long commentId, Long postId) {
        return Alarm.builder()
                .alarmType(AlarmType.COMMENT_LIKE)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(commentId)
                .secondaryReferenceId(postId)
                .build();
    }

    public void markAsRead() {
        this.readAt = LocalDateTime.now();
    }
}
