package com.instagram.backend.domain.chat.entity;

import com.instagram.backend.global.entity.BaseSoftDeleteEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseSoftDeleteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chatRoomId;

    @Column(name = "chat_room_type", nullable = false, length = 20)
    private String chatRoomType;

    @Column(name = "chat_room_name", nullable = false, length = 20)
    private String chatRoomName;

    @Builder
    public ChatRoom(String chatRoomType, String chatRoomName) {
        this.chatRoomType = chatRoomType;
        this.chatRoomName = chatRoomName;
    }
}
