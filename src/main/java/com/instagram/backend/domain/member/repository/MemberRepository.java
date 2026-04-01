package com.instagram.backend.domain.member.repository;

import com.instagram.backend.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/*
  JpaRepository<Member, Long>
  — Member 엔티티를 다루는 Repository (DB 접근 계층)
  — Long은 PK(memberId)의 타입
  — 기본 CRUD 메서드를 자동으로 제공: save(), findById(), findAll(), delete() 등

  Spring Data JPA의 메서드 이름 규칙:
  — findBy + 필드명 → 해당 필드로 조회하는 SQL을 자동 생성
  — existsBy + 필드명 → 존재 여부 확인 (boolean 반환)
  — Optional<Member> → 결과가 없을 수 있으므로 null 대신 Optional로 감싸서 반환
*/
public interface MemberRepository extends JpaRepository<Member, Long> {

    // SELECT * FROM members WHERE email = ? → 결과 없으면 Optional.empty()
    Optional<Member> findByEmail(String email);

    // SELECT * FROM members WHERE username = ? AND is_deleted = false
    Optional<Member> findByUsernameAndIsDeletedFalse(String username);

    // SELECT EXISTS(SELECT 1 FROM members WHERE email = ?)
    boolean existsByEmail(String email);

    // SELECT EXISTS(SELECT 1 FROM members WHERE username = ?)
    boolean existsByUsername(String username);
}
