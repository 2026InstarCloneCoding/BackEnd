package com.instagram.backend.domain.chat.repository;

import com.instagram.backend.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 단건 조회 (소프트 삭제된 것 제외)
    // 향후 이슈 B(메시지 조회)에서도 사용 예정
    Optional<ChatRoom> findByChatRoomIdAndIsDeletedFalse(Long chatRoomId);

    // 여러 채팅방을 한 번에 조회 (N+1 회피)
    // GET /api/chats 에서 내가 속한 채팅방 정보를 IN 절 배치 조회할 때 사용
    // SELECT * FROM chat_rooms WHERE chat_room_id IN (?, ?, ...) AND is_deleted = false
    List<ChatRoom> findByChatRoomIdInAndIsDeletedFalse(Collection<Long> chatRoomIds);
}
