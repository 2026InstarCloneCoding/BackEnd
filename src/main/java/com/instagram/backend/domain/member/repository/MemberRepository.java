package com.instagram.backend.domain.member.repository;

import com.instagram.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberUsernameAndIsDeletedFalse(String memberUsername);

    // 단일 memberId 조회 (소프트 삭제된 계정 자동 제외)
    // 메시지 전송 응답에 작성자 username/profileImageUrl을 담을 때 사용
    // findById(Long)은 is_deleted 필터가 없어 탈퇴 계정의 정보가 노출될 수 있으므로
    // 프로젝트 전반의 "findByXxxIdAndIsDeletedFalse" 패턴을 따름
    Optional<Member> findByMemberIdAndIsDeletedFalse(Long memberId);

    boolean existsByEmail(String email);

    boolean existsByMemberUsername(String memberUsername);

    // 여러 memberId를 한 번에 조회 (N+1 회피용 IN 배치 조회)
    // SELECT * FROM members WHERE member_id IN (?, ?, ...) AND is_deleted = false
    // 채팅방 참여자 목록, 메시지 발신자 정보 등 "여러 유저를 한 번에 가져와야 하는" 상황에 사용
    List<Member> findByMemberIdInAndIsDeletedFalse(Collection<Long> memberIds);
}
