package com.instagram.backend.domain.search.service;

import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.search.dto.response.UserSearchResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
  SearchService — 검색 도메인 비즈니스 로직

  담당 기능:
    — searchUsers: GET /api/search/users

  설계 원칙:
    — 검색어(q)로 username과 name을 LIKE 검색 (OR 조건)
    — 본인 제외, 소프트 삭제 제외
    — is_following 판단은 IN 배치 조회로 N+1 회피
      (검색 결과 memberId 목록 → FollowRepository.findFollowingIdsAmong() 한 번 호출)
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    /*
      유저 검색 — GET /api/search/users

      처리 순서:
        1) 검색어 검증 (null → MISSING_REQUIRED_FIELD, blank → INVALID_SEARCH_QUERY)
        2) limit 정규화 (null이면 기본 10, 최대 50)
        3) Member LIKE 검색 (username OR name, 본인 제외, isDeleted=false)
        4) 검색 결과 memberId로 팔로우 여부 배치 조회
        5) 응답 DTO 조립

      검색 방식:
        — LIKE '%q%' → DB 인덱스를 못 타므로 풀 스캔
        — 대규모 서비스에서는 Elasticsearch 같은 검색 엔진이 필요하지만
          학습 목적 프로젝트에서는 LIKE로 충분 (MVP)
    */
    public UserSearchResponse searchUsers(Long myMemberId, String q, Integer limit) {
        // 1) 검색어 검증
        if (q == null) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_QUERY);
        }

        // LIKE 와일드카드 이스케이프
        //   — 사용자가 '%' 또는 '_'를 검색어에 포함하면 LIKE 패턴으로 해석되어
        //     의도치 않게 전체 회원이 반환될 수 있음 (정보 열거 위험)
        //   — '%'와 '_'를 이스케이프하여 리터럴 문자로 취급하게 함
        //   — JPQL 쿼리에 ESCAPE '\\' 절과 함께 동작
        String escaped = trimmed
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");

        // 2) limit 검증
        int size = (limit == null) ? DEFAULT_LIMIT : limit;
        if (size < 1 || size > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }

        // 3) LIKE 검색 (본인 제외, 소프트 삭제 제외)
        List<Member> members = memberRepository.searchByUsernameOrName(
                myMemberId, escaped, PageRequest.of(0, size));

        if (members.isEmpty()) {
            // 빈 결과 early return — 아래 findFollowingIdsAmong()에 빈 컬렉션이 전달되는 것을 방지
            // JPA의 IN () 절에 빈 컬렉션이 들어가면 구현체에 따라 SQL 에러 가능성 있음
            return UserSearchResponse.of(Collections.emptyList(), Collections.emptySet());
        }

        // 4) 팔로우 여부 배치 조회 (N+1 회피)
        //    — 검색 결과의 memberId를 모아서 한 번의 IN 쿼리로 처리
        //    — 반환값은 내가 팔로우 중인 memberId 목록 → Set으로 변환하여 O(1) lookup
        List<Long> memberIds = members.stream().map(Member::getMemberId).toList();
        Set<Long> followingIds = new HashSet<>(
                followRepository.findFollowingIdsAmong(myMemberId, memberIds));

        // 5) 응답 조립
        return UserSearchResponse.of(members, followingIds);
    }
}
