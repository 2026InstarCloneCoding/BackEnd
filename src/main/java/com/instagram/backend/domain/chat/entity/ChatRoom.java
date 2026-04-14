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

    // 채팅방 이름 — nullable
    // 1:1 채팅방은 항상 NULL, 그룹 채팅방은 생성 직후 NULL 후 이름 설정 API로 값 채움
    // 프론트엔드는 null일 때 other_members의 username으로 화면 렌더링
    @Column(name = "chat_room_name", length = 20)
    private String chatRoomName;

    @Builder
    public ChatRoom(String chatRoomType, String chatRoomName) {
        this.chatRoomType = chatRoomType;
        this.chatRoomName = chatRoomName;
    }
}
