package com.instagram.backend.domain.alarm.entity;

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
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    private Long followId;

    private Long postlikeId;

    private Long commentId;

    private Long commentlikeId;

    private LocalDateTime readAt;

    private Boolean isDeleted;

    @Builder
    public Alarm(Long followId, Long postlikeId, Long commentId, Long commentlikeId) {
        this.followId = followId;
        this.postlikeId = postlikeId;
        this.commentId = commentId;
        this.commentlikeId = commentlikeId;
        this.isDeleted = false;
    }
}
