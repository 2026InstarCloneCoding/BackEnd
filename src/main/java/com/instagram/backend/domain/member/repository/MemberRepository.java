package com.instagram.backend.domain.member.repository;

import com.instagram.backend.domain.member.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /*
      유저 검색 — username 또는 name으로 LIKE 검색

      목적: GET /api/search/users의 검색 결과 반환
        — 소프트 삭제된 회원 제외
        — 본인(myId) 제외 — 자기 자신은 검색 결과에 불필요
        — username과 name 양쪽 모두 부분 일치 검색 (OR 조건)
        — Pageable로 limit 제어

      LIKE 검색:
        — JPQL에서 LIKE CONCAT('%', :q, '%') 패턴 사용
        — Spring Data JPA의 Containing 파생 메서드도 가능하지만,
          OR 조건 + 본인 제외 + 삭제 제외를 한 쿼리에 담으려면 @Query가 깔끔함
    */
    @Query("SELECT m FROM Member m " +
           "WHERE m.isDeleted = false " +
           "AND m.memberId != :myId " +
           "AND (m.memberUsername LIKE CONCAT('%', :q, '%') ESCAPE '\\' " +
           "OR m.name LIKE CONCAT('%', :q, '%') ESCAPE '\\')")
    List<Member> searchByUsernameOrName(@Param("myId") Long myId, @Param("q") String q, Pageable pageable);
}
