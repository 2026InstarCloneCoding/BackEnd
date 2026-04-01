package com.instagram.backend.domain.auth.service;

import com.instagram.backend.domain.auth.dto.*;
import com.instagram.backend.domain.auth.entity.RefreshToken;
import com.instagram.backend.domain.auth.repository.RefreshTokenRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import com.instagram.backend.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        if (memberRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .username(request.getUsername())
                .name(request.getName())
                .build();
        memberRepository.save(member);

        return SignupResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .build();
    }

    public LoginResponse login(LoginRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(member.getMemberId(), member.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getMemberId());

        refreshTokenRepository.deleteByMemberId(member.getMemberId());
        refreshTokenRepository.save(RefreshToken.builder()
                .memberId(member.getMemberId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshExpiration() / 1000))
                .build());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtTokenProvider.getAccessExpiration() / 1000)
                .member(LoginResponse.MemberInfo.builder()
                        .memberId(member.getMemberId())
                        .username(member.getUsername())
                        .imageUrl(member.getImageUrl())
                        .build())
                .build();
    }

    public RefreshResponse refresh(String refreshTokenStr) {
        jwtTokenProvider.validateToken(refreshTokenStr);

        if (!"refresh".equals(jwtTokenProvider.getTypeFromToken(refreshTokenStr))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        Long memberId = jwtTokenProvider.getMemberIdFromToken(refreshTokenStr);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, member.getRole());

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .expiresIn(jwtTokenProvider.getAccessExpiration() / 1000)
                .build();
    }

    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> refreshTokenRepository.deleteByMemberId(token.getMemberId()));
    }
}
