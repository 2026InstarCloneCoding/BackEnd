package com.instagram.backend.domain.follow.service;

import com.instagram.backend.domain.alarm.entity.Alarm;
import com.instagram.backend.domain.alarm.service.AlarmService;
import com.instagram.backend.domain.follow.dto.FollowListResponse;
import com.instagram.backend.domain.follow.entity.Follow;
import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/*
  FollowService — 팔로우/언팔로우 비즈니스 로직

  왜 MemberService가 아니라 별도 Service로 분리했나?
    — 팔로우는 "두 유저 간의 관계"를 다루는 로직
    — MemberService는 "한 유저의 프로필"을 다루는 로직
    — 관심사가 다르므로 분리하면 각 Service가 작아지고 유지보수가 쉬움
    — 실무에서도 도메인별로 Service를 나누는 것이 일반적

  @Transactional(readOnly = true)
    — 클래스 레벨: 기본적으로 읽기 전용 (조회 성능 최적화)
    — 데이터를 변경하는 메서드에만 @Transactional을 따로 붙임
*/
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final AlarmService alarmService;

    /*
      팔로우
      — 검증 순서: 자기자신 팔로우 방지 → 대상 존재 확인 → 중복 팔로우 확인
      — 검증을 먼저 하는 이유: DB에 잘못된 데이터가 들어가는 것을 방지 (방어적 프로그래밍)

      Follow 엔티티 저장 + 양쪽 Member의 카운트 증감
        — 나(follower)의 followingCount +1
        — 상대(following)의 followerCount +1
        — 이렇게 카운트를 직접 관리하면 프로필 조회 시 COUNT 쿼리가 필요 없음

      왜 findMemberById를 두 번 호출하나?
        — 나(me)와 상대(target) 둘 다 Member 엔티티가 필요
        — 나: followingCount를 올리기 위해
        — 상대: followerCount를 올리기 위해 + 존재 확인
    */
    @Transactional
    public void follow(Long myMemberId, Long targetMemberId) {
        // 1. 자기 자신을 팔로우하는지 검증
        if (myMemberId.equals(targetMemberId)) {
            throw new BusinessException(ErrorCode.CANNOT_FOLLOW_SELF);
        }

        // 2. 팔로우 대상이 존재하는지 확인
        Member target = findMemberById(targetMemberId);
        Member me = findMemberById(myMemberId);

        // 3. 이미 팔로우했는지 확인 (중복 방지)
        if (followRepository.existsByFollowerIdAndFollowingId(myMemberId, targetMemberId)) {
            throw new BusinessException(ErrorCode.ALREADY_FOLLOWING);
        }

        // 4. Follow 관계 저장
        Follow follow = Follow.builder()
                .followerId(myMemberId)
                .followingId(targetMemberId)
                .build();
        followRepository.save(follow);

        // 5. 양쪽 카운트 업데이트 (dirty checking으로 자동 UPDATE)
        me.incrementFollowingCount();
        target.incrementFollowerCount();

        alarmService.createAlarm(Alarm.ofFollow(targetMemberId, myMemberId));
    }

    /*
      언팔로우
      — 팔로우 관계가 없으면 FOLLOW_NOT_FOUND 에러
      — Follow 엔티티 삭제 + 양쪽 카운트 감소

      delete() vs deleteById()
        — delete(entity): 엔티티 객체를 넘겨서 삭제
        — deleteById(id): ID만으로 삭제
        — 여기서는 이미 조회한 follow 객체가 있으므로 delete() 사용
    */
    @Transactional
    public void unfollow(Long myMemberId, Long targetMemberId) {
        // 1. 팔로우 대상이 존재하는지 확인
        Member target = findMemberById(targetMemberId);
        Member me = findMemberById(myMemberId);

        // 2. 팔로우 관계 조회 (없으면 에러)
        Follow follow = followRepository.findByFollowerIdAndFollowingId(myMemberId, targetMemberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FOLLOW_NOT_FOUND));

        // 3. Follow 관계 삭제
        followRepository.delete(follow);

        // 4. 양쪽 카운트 감소
        me.decrementFollowingCount();
        target.decrementFollowerCount();
    }

    /*
      팔로워 목록 조회 (커서 기반 페이지네이션)

      커서 페이지네이션 동작 원리:
        1. 첫 요청: cursor=null → 가장 최신 팔로워부터 limit개 조회
        2. 다음 요청: cursor=마지막follow_id → 그 ID보다 작은(이전) 것들 중 limit개 조회
        3. 결과가 limit개보다 많으면(+1개 요청) → 다음 페이지가 있다는 뜻

      왜 limit+1개를 조회하나?
        — 20개를 요청했는데 21개가 나오면 → 다음 페이지 있음 (hasNext=true)
        — 20개 이하로 나오면 → 마지막 페이지 (hasNext=false)
        — 실제 응답에는 limit개만 담고, +1개는 hasNext 판별용으로만 사용

      Follow → FollowListResponse 변환:
        — Follow 엔티티에는 followerId/followingId(숫자)만 있음
        — 프로필 정보(username, name 등)를 보여주려면 Member를 조회해야 함
        — 팔로워 목록: followerId로 Member 조회 (나를 팔로우하는 사람)
        — 팔로잉 목록: followingId로 Member 조회 (내가 팔로우하는 사람)
    */
    public CursorPageResponse<FollowListResponse> getFollowers(Long myMemberId, String username, String cursor, int limit) {
        // 1. username으로 대상 Member 조회
        Member target = findMemberByUsername(username);

        // 2. limit 검증 (1~50)
        validateLimit(limit);

        // 3. 커서 기반으로 Follow 목록 조회 (+1개 더 조회해서 다음 페이지 여부 확인)
        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Follow> follows;

        if (cursor == null) {
            // 첫 페이지: 커서 없이 최신순으로
            follows = followRepository.findByFollowingIdOrderByFollowIdDesc(target.getMemberId(), pageRequest);
        } else {
            // 다음 페이지: 커서(follow_id) 이전 데이터만
            follows = followRepository.findByFollowingIdAndFollowIdLessThanOrderByFollowIdDesc(
                    target.getMemberId(), Long.parseLong(cursor), pageRequest);
        }

        // 4. hasNext 판별 후 실제 응답 데이터는 limit개만
        boolean hasNext = follows.size() > limit;
        List<Follow> resultFollows = hasNext ? follows.subList(0, limit) : follows;

        // 5. Follow → FollowListResponse 변환 (팔로워 = followerId로 Member 조회)
        List<FollowListResponse> content = resultFollows.stream()
                .map(follow -> {
                    Member follower = findMemberById(follow.getFollowerId());
                    boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myMemberId, follower.getMemberId());
                    return FollowListResponse.of(follower, isFollowing);
                })
                .collect(Collectors.toList());

        // 6. 다음 커서 = 마지막 항목의 follow_id
        String nextCursor = hasNext ? String.valueOf(resultFollows.get(resultFollows.size() - 1).getFollowId()) : null;

        return CursorPageResponse.of(content, nextCursor, hasNext);
    }

    /*
      팔로잉 목록 조회 — 팔로워 목록과 구조 동일, 조회 방향만 반대
      — 팔로워: followingId로 검색 → followerId의 Member 정보 반환
      — 팔로잉: followerId로 검색 → followingId의 Member 정보 반환
    */
    public CursorPageResponse<FollowListResponse> getFollowing(Long myMemberId, String username, String cursor, int limit) {
        Member target = findMemberByUsername(username);
        validateLimit(limit);

        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Follow> follows;

        if (cursor == null) {
            follows = followRepository.findByFollowerIdOrderByFollowIdDesc(target.getMemberId(), pageRequest);
        } else {
            follows = followRepository.findByFollowerIdAndFollowIdLessThanOrderByFollowIdDesc(
                    target.getMemberId(), Long.parseLong(cursor), pageRequest);
        }

        boolean hasNext = follows.size() > limit;
        List<Follow> resultFollows = hasNext ? follows.subList(0, limit) : follows;

        // 팔로잉 = followingId로 Member 조회 (내가 팔로우하는 사람)
        List<FollowListResponse> content = resultFollows.stream()
                .map(follow -> {
                    Member following = findMemberById(follow.getFollowingId());
                    boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myMemberId, following.getMemberId());
                    return FollowListResponse.of(following, isFollowing);
                })
                .collect(Collectors.toList());

        String nextCursor = hasNext ? String.valueOf(resultFollows.get(resultFollows.size() - 1).getFollowId()) : null;

        return CursorPageResponse.of(content, nextCursor, hasNext);
    }

    /*
      limit 검증 — API 명세에 따라 1~50 범위만 허용
    */
    private void validateLimit(int limit) {
        if (limit < 1 || limit > 50) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }
    }

    /*
      username으로 Member 조회 (삭제되지 않은 유저만)
    */
    private Member findMemberByUsername(String username) {
        return memberRepository.findByMemberUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /*
      private 헬퍼 — Member 조회
      MemberService에도 같은 메서드가 있지만, Service 간 직접 호출을 피하기 위해 각자 가짐
      → Service끼리 의존하면 순환 참조 위험이 있고, 각 Service가 독립적으로 동작하는 것이 좋음
    */
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
