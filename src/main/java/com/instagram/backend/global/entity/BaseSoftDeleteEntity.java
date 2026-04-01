package com.instagram.backend.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

// BaseTimeEntity(createdAt, updatedAt)를 상속받고
// 소프트 딜리트(is_deleted, deleted_at)를 추가한 공통 베이스
// posts, members, stories, comments, chat_rooms, messages 등에서 사용
@Getter
@MappedSuperclass
public abstract class BaseSoftDeleteEntity extends BaseTimeEntity {

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    public void softDelete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
