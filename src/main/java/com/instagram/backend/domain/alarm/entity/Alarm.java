package com.instagram.backend.domain.alarm.entity;

import com.instagram.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "alarms", indexes = {
        @Index(name = "idx_alarm_target_member_id", columnList = "target_member_id"),
        @Index(name = "idx_alarm_target_unread",   columnList = "target_member_id, read_at")
})
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
        Objects.requireNonNull(alarmType, "alarmType must not be null");
        Objects.requireNonNull(targetMemberId, "targetMemberId must not be null");
        Objects.requireNonNull(senderMemberId, "senderMemberId must not be null");
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
        Objects.requireNonNull(postId, "postId must not be null for POST_LIKE");
        return Alarm.builder()
                .alarmType(AlarmType.POST_LIKE)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(postId)
                .build();
    }

    public static Alarm ofComment(Long targetMemberId, Long senderMemberId, Long commentId, Long postId) {
        Objects.requireNonNull(commentId, "commentId must not be null for COMMENT");
        Objects.requireNonNull(postId, "postId must not be null for COMMENT");
        return Alarm.builder()
                .alarmType(AlarmType.COMMENT)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(commentId)
                .secondaryReferenceId(postId)
                .build();
    }

    public static Alarm ofCommentLike(Long targetMemberId, Long senderMemberId, Long commentId, Long postId) {
        Objects.requireNonNull(commentId, "commentId must not be null for COMMENT_LIKE");
        Objects.requireNonNull(postId, "postId must not be null for COMMENT_LIKE");
        return Alarm.builder()
                .alarmType(AlarmType.COMMENT_LIKE)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(commentId)
                .secondaryReferenceId(postId)
                .build();
    }

    public static Alarm ofStoryView(Long targetMemberId, Long senderMemberId, Long storyId) {
        Objects.requireNonNull(storyId, "storyId must not be null for STORY_VIEW");
        return Alarm.builder()
                .alarmType(AlarmType.STORY_VIEW)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(storyId)
                .build();
    }

    public static Alarm ofMessage(Long targetMemberId, Long senderMemberId,
                                   Long messageId, Long chatRoomId) {
        Objects.requireNonNull(messageId, "messageId must not be null for MESSAGE");
        Objects.requireNonNull(chatRoomId, "chatRoomId must not be null for MESSAGE");
        return Alarm.builder()
                .alarmType(AlarmType.MESSAGE)
                .targetMemberId(targetMemberId)
                .senderMemberId(senderMemberId)
                .referenceId(messageId)
                .secondaryReferenceId(chatRoomId)
                .build();
    }

    // 최초 읽음 시각을 보존 — 중복 호출 시 덮어쓰지 않음
    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }
}
