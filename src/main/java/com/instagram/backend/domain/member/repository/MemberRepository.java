package com.instagram.backend.domain.member.repository;

import com.instagram.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMemberUsernameAndIsDeletedFalse(String memberUsername);

    boolean existsByEmail(String email);

    boolean existsByMemberUsername(String memberUsername);
}
