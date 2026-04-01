package com.instagram.backend.global.config;

import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!memberRepository.existsByEmail("root@instagram.com")) {
            memberRepository.save(Member.builder()
                    .userId("1")
                    .email("root@instagram.com")
                    .memberPassword(passwordEncoder.encode("root1234"))
                    .memberUsername("root")
                    .memberName("Root")
                    .build());
            System.out.println("[DataInitializer] Root 계정 생성 완료: root@instagram.com / root1234");
        }
    }
}
