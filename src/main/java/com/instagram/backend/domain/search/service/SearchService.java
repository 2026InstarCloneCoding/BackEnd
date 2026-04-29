package com.instagram.backend.domain.search.service;

import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.hashtag.entity.Hashtag;
import com.instagram.backend.domain.hashtag.repository.HashtagRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.search.dto.response.HashtagSearchResponse;
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
    — searchHashtags: GET /api/search/hashtags

  설계 원칙:
    — 검색어(q)에 LIKE 와일드카드 이스케이프 적용 (정보 열거 방지)
    — limit 정규화 (기본 10, 최대 50)
    — 결과 비어있으면 빈 응답 즉시 반환 (IN 빈 컬렉션 방지 등)
    — 관계 정보(팔로우 여부)는 IN 배치 조회로 N+1 회피

  헬퍼 메서드 추출:
    — validateQuery / validateLimit / escapeLikePattern 은 두 검색에서 공유
    — 추후 검색 도메인 확장(게시물·장소 등) 시 별도 유틸 클래스로 분리 고려
      (현재는 도메인 외부 노출 필요성이 없어 private 유지)

  TODO:
    — LIKE 검색은 인덱스를 못 타므로 데이터가 커지면 풀 스캔. 운영 단계에서는
      Elasticsearch 등 검색엔진 도입 검토. 학습 프로젝트 MVP 수준에서는 LIKE 로 충분.
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private static final int DEFAULT_LIMIT = 10;
    private static final int MAX_LIMIT = 50;

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final HashtagRepository hashtagRepository;

    /*
      유저 검색 — GET /api/search/users

      처리 순서:
        1) 검색어 검증 (null → MISSING_REQUIRED_FIELD, blank → INVALID_SEARCH_QUERY)
        2) limit 정규화 (null이면 기본 10, 최대 50)
        3) Member LIKE 검색 (username OR name, 본인 제외, isDeleted=false)
        4) 검색 결과 memberId로 팔로우 여부 배치 조회
        5) 응답 DTO 조립
    */
    public UserSearchResponse searchUsers(Long myMemberId, String q, Integer limit) {
        String escaped = escapeLikePattern(validateQuery(q));
        int size = validateLimit(limit);

        List<Member> members = memberRepository.searchByUsernameOrName(
                myMemberId, escaped, PageRequest.of(0, size));

        if (members.isEmpty()) {
            // 빈 결과 early return — 아래 findFollowingIdsAmong()에 빈 컬렉션이 전달되는 것을 방지
            // JPA의 IN () 절에 빈 컬렉션이 들어가면 구현체에 따라 SQL 에러 가능성 있음
            return UserSearchResponse.of(Collections.emptyList(), Collections.emptySet());
        }

        // 팔로우 여부 배치 조회 (N+1 회피)
        //   — 검색 결과 memberId 를 모아서 한 번의 IN 쿼리로 처리
        //   — 반환값을 Set 으로 변환하여 O(1) lookup
        List<Long> memberIds = members.stream().map(Member::getMemberId).toList();
        Set<Long> followingIds = new HashSet<>(
                followRepository.findFollowingIdsAmong(myMemberId, memberIds));

        return UserSearchResponse.of(members, followingIds);
    }

    /*
      해시태그 검색 — GET /api/search/hashtags

      처리 순서:
        1) '#' 접두사 제거 ('#음식' === '음식')
        2) 검색어 검증 (null → MISSING_REQUIRED_FIELD, blank → INVALID_SEARCH_QUERY)
        3) LIKE 와일드카드 이스케이프
        4) limit 정규화
        5) Hashtag LIKE 검색 (postCount DESC, name ASC)
        6) 응답 DTO 조립

      유저 검색과의 차이:
        — '#' 접두사 처리 단계 추가
        — is_following 같은 관계 필드 없음 (명세 외 스코프)
        — 본인 제외 같은 필터 없음 (해시태그는 작성자 개념 없음)
    */
    public HashtagSearchResponse searchHashtags(Long myMemberId, String q, Integer limit) {
        String stripped = stripHashPrefix(q);
        String escaped = escapeLikePattern(validateQuery(stripped));
        int size = validateLimit(limit);

        List<Hashtag> hashtags = hashtagRepository.searchByName(
                escaped, PageRequest.of(0, size));

        if (hashtags.isEmpty()) {
            return HashtagSearchResponse.of(Collections.emptyList());
        }

        return HashtagSearchResponse.of(hashtags);
    }

    /*
      ─── 공통 헬퍼 메서드 ───

      검색 도메인 내부에서 두 endpoint 가 공유하는 검증/정규화 로직.
      외부 도메인에서는 사용하지 않으므로 private 유지.
      추후 검색 대상이 늘어나거나 다른 도메인에서 동일 검증이 필요해지면
      별도 SearchUtils / QueryValidator 클래스로 분리 고려.
    */

    /*
      검색어 검증
        — null  → MISSING_REQUIRED_FIELD (요청 필수 항목 미입력)
        — blank → INVALID_SEARCH_QUERY (공백만 입력)
      검증 통과 시 trim 된 문자열 반환
    */
    private String validateQuery(String q) {
        if (q == null) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        String trimmed = q.trim();
        if (trimmed.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_SEARCH_QUERY);
        }
        return trimmed;
    }

    /*
      limit 검증
        — null → 기본값(10)
        — 1 ~ 50 범위 외 → INVALID_LIMIT
    */
    private int validateLimit(Integer limit) {
        int size = (limit == null) ? DEFAULT_LIMIT : limit;
        if (size < 1 || size > MAX_LIMIT) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }
        return size;
    }

    /*
      LIKE 와일드카드 이스케이프
        — 사용자가 '%' 또는 '_'를 검색어에 포함하면 LIKE 패턴으로 해석되어
          의도치 않게 전체 row가 반환될 수 있음 (정보 열거 위험)
        — '\' 를 가장 먼저 이스케이프해야 이중 이스케이프 충돌 방지
        — JPQL 쿼리의 ESCAPE '\\' 절과 함께 동작
    */
    private String escapeLikePattern(String q) {
        return q
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    /*
      해시태그 검색어의 '#' 접두사 제거
        — 사용자가 '#음식' 으로 검색해도 '음식' 으로 검색한 것과 동일하게 처리
        — null 안전 (validateQuery 에서 null 처리)
    */
    private String stripHashPrefix(String q) {
        if (q == null) return null;
        String trimmed = q.trim();
        if (trimmed.startsWith("#")) {
            return trimmed.substring(1);
        }
        return q;
    }
}
