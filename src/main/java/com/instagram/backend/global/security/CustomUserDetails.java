package com.instagram.backend.global.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT 토큰을 디코딩한 사용자 정보를 담는 클래스.
 *
 * 컨트롤러에서 현재 로그인한 사용자 정보를 꺼낼 때 사용합니다:
 *
 * @GetMapping("/posts")
 * public ApiResponse<?> getPosts(@AuthenticationPrincipal CustomUserDetails userDetails) {
 *     Long memberId = userDetails.getMemberId();
 * }
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final Long memberId;
    private final String email;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(Long memberId, String email, String password, String role) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.authorities = List.of(new SimpleGrantedAuthority(role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
}
