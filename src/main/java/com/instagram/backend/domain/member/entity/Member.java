package com.instagram.backend.domain.member.entity;

import com.instagram.backend.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
  @Entity — 이 클래스가 DB 테이블과 매핑되는 JPA 엔티티임을 선언
  @Table(name = "members") — 실제 DB 테이블 이름 지정 (클래스명과 다를 때 사용)
  @Getter — 모든 필드의 getter 자동 생성 (Lombok)
  @NoArgsConstructor(access = PROTECTED)
    — 파라미터 없는 기본 생성자를 protected로 생성
    — JPA는 내부적으로 기본 생성자가 필요하지만, 외부에서 new Member()로 생성하는 것은 막음
    — 객체 생성은 Builder를 통해서만 하도록 유도
*/
@Entity
@Table(name = "members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    /*
      @Id — 이 필드가 PK(Primary Key)임을 선언
      @GeneratedValue(IDENTITY) — DB의 AUTO_INCREMENT를 사용해서 자동 증가
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    /*
      @Column — DB 컬럼과 매핑 설정
      nullable = false → NOT NULL 제약조건
      unique = true → UNIQUE 제약조건
      length = 30 → VARCHAR(30) 같은 길이 제한
    */
    @Column(nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 10)
    private String role;

    @Column(length = 500)
    private String bio;

    @Column(length = 20)
    private String phone;

    /*
      @Enumerated(STRING) — Enum 값을 DB에 문자열로 저장
      EnumType.ORDINAL이면 숫자(0, 1, 2)로 저장되는데, 순서가 바뀌면 데이터가 꼬이므로
      실무에서는 항상 STRING을 사용
    */
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 50)
    private String imageType;

    @Column(length = 255)
    private String imageName;

    @Column(length = 36)
    private String imageUuid;

    /*
      soft delete — 실제 DB에서 행을 삭제하지 않고, is_deleted 플래그로 삭제 여부를 표시
      실무에서 많이 쓰는 패턴: 데이터 복구가 가능하고, 연관 데이터가 깨지지 않음
    */
    @Column(nullable = false)
    private Boolean isDeleted = false;

    private LocalDateTime deletedAt;

    /*
      반정규화(Denormalization) 필드
      — 팔로워/팔로잉 수를 매번 follows 테이블에서 COUNT 하면 느리므로
      — members 테이블에 미리 저장해두고, 팔로우/언팔로우 시 +1/-1 업데이트
      — 조회 성능을 위해 약간의 데이터 중복을 허용하는 전략
    */
    private Long followerCount = 0L;

    private Long followingCount = 0L;

    /*
      @Builder — 빌더 패턴으로 객체 생성
      Member.builder().username("john").name("John").build() 형태로 사용
      생성자에 필요한 필드만 나열해서, 어떤 값이 필수인지 명확하게 표현
    */
    @Builder
    public Member(Long userId, String username, String email, String password, String name) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = "ROLE_USER";
        this.isDeleted = false;
        this.followerCount = 0L;
        this.followingCount = 0L;
    }

    /*
      도메인 메서드 — Entity가 자신의 상태를 변경하는 비즈니스 메서드
      setter를 public으로 열지 않고, 의미 있는 이름의 메서드를 통해서만 변경 가능
      → 어디서 어떤 이유로 값이 바뀌는지 추적하기 쉬움
    */
    public void updateProfile(String name, String bio, String phone, Gender gender) {
        if (name != null) this.name = name;
        if (bio != null) this.bio = bio;
        if (phone != null) this.phone = phone;
        if (gender != null) this.gender = gender;
    }

    public void updateProfileImage(String imageUrl, String imageType, String imageName, String imageUuid) {
        this.imageUrl = imageUrl;
        this.imageType = imageType;
        this.imageName = imageName;
        this.imageUuid = imageUuid;
    }
}
