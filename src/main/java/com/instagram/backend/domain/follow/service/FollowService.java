package com.instagram.backend.domain.follow.service;

import com.instagram.backend.domain.follow.entity.Follow;
import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
      private 헬퍼 — Member 조회
      MemberService에도 같은 메서드가 있지만, Service 간 직접 호출을 피하기 위해 각자 가짐
      → Service끼리 의존하면 순환 참조 위험이 있고, 각 Service가 독립적으로 동작하는 것이 좋음
    */
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
