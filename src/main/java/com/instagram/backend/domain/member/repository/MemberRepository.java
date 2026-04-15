package com.instagram.backend.domain.member.repository;

import com.instagram.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberUsernameAndIsDeletedFalse(String memberUsername);

    boolean existsByEmail(String email);

    boolean existsByMemberUsername(String memberUsername);

    // 여러 memberId를 한 번에 조회 (N+1 회피용 IN 배치 조회)
    // SELECT * FROM members WHERE member_id IN (?, ?, ...) AND is_deleted = false
    // 채팅방 참여자 목록, 메시지 발신자 정보 등 "여러 유저를 한 번에 가져와야 하는" 상황에 사용
    List<Member> findByMemberIdInAndIsDeletedFalse(Collection<Long> memberIds);
}
