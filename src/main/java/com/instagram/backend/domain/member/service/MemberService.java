package com.instagram.backend.domain.member.service;

import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.member.dto.*;
import com.instagram.backend.domain.member.entity.Gender;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/*
  @Service — 이 클래스가 비즈니스 로직을 담당하는 서비스 계층임을 선언
             Spring이 자동으로 빈(Bean)으로 등록해서 의존성 주입(DI)에 사용

  @Transactional(readOnly = true)
    — 클래스 레벨에 선언하면 모든 메서드에 읽기 전용 트랜잭션 적용
    — 읽기 전용이면 JPA가 변경 감지(dirty checking)를 안 하므로 성능 향상
    — 데이터를 변경하는 메서드에는 @Transactional을 따로 붙여서 읽기 전용을 해제

  @RequiredArgsConstructor
    — final 필드를 파라미터로 받는 생성자를 자동 생성 (Lombok)
    — Spring이 이 생성자를 보고 자동으로 의존성 주입 (생성자 주입 방식)
*/
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    /*
      내 프로필 조회
      — JWT에서 추출한 memberId로 Member 조회
      — postCount는 아직 Post 엔티티가 없으므로 0으로 반환 (TODO: Post 개발 후 연동)
    */
    public MyProfileResponse getMyProfile(Long memberId) {
        Member member = findMemberById(memberId);
        long postCount = 0; // TODO: postRepository.countByMemberIdAndIsDeletedFalse(memberId)
        return MyProfileResponse.from(member, postCount);
    }

    /*
      프로필 수정
      — @Transactional: 데이터를 변경하므로 읽기 전용 해제
      — JPA의 변경 감지(dirty checking):
        Entity 객체의 필드를 바꾸면, 트랜잭션이 끝날 때 JPA가 자동으로 UPDATE SQL 실행
        별도로 save()를 호출할 필요가 없음
    */
    @Transactional
    public ProfileUpdateResponse updateProfile(Long memberId, ProfileUpdateRequest request) {
        Member member = findMemberById(memberId);

        // gender 문자열 → Enum 변환 + 검증
        Gender gender = parseGender(request.getGender());

        // Entity의 도메인 메서드로 값 변경 → 트랜잭션 끝나면 자동 UPDATE
        member.updateProfile(request.getName(), request.getBio(), request.getPhone(), gender);

        return ProfileUpdateResponse.from(member);
    }

    /*
      다른 유저 프로필 조회
      — username으로 조회 + 삭제되지 않은 유저만
      — 내가 이 유저를 팔로우하고 있는지 확인
    */
    public UserProfileResponse getUserProfile(Long myMemberId, String username) {
        Member member = memberRepository.findByMemberUsernameAndIsDeletedFalse(username)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        long postCount = 0; // TODO: postRepository.countByMemberIdAndIsDeletedFalse(member.getMemberId())
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(myMemberId, member.getMemberId());

        return UserProfileResponse.from(member, postCount, isFollowing);
    }

    /*
      private 헬퍼 메서드 — 반복되는 Member 조회 로직을 추출
      찾지 못하면 MEMBER_NOT_FOUND 에러를 던짐
    */
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
    }

    /*
      gender 문자열을 Enum으로 변환
      null이면 null 반환 (선택 필드이므로)
      잘못된 값이면 INVALID_GENDER_VALUE 에러
    */
    private Gender parseGender(String gender) {
        if (gender == null) return null;
        try {
            return Gender.valueOf(gender.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_GENDER_VALUE);
        }
    }
}
