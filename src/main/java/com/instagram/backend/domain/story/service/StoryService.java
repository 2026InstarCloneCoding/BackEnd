package com.instagram.backend.domain.story.service;

import com.instagram.backend.domain.follow.repository.FollowRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.domain.story.dto.*;
import com.instagram.backend.domain.story.entity.Story;
import com.instagram.backend.domain.story.entity.StoryVisitor;
import com.instagram.backend.domain.story.repository.StoryRepository;
import com.instagram.backend.domain.story.repository.StoryVisitorRepository;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoryService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final StoryRepository storyRepository;
    private final StoryVisitorRepository storyVisitorRepository;
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public StoryCreateResponse createStory(Long memberId, StoryCreateRequest request) {
        if (request.getImageUrl() == null || request.getImageUrl().isBlank()) {
            throw new BusinessException(ErrorCode.MISSING_REQUIRED_FIELD);
        }
        validateImageType(request.getImageType());

        Story story = Story.builder()
                .memberId(memberId)
                .imageUrl(request.getImageUrl())
                .imageType(request.getImageType())
                .imageName(request.getImageName())
                .imageUuid(request.getImageUuid())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        storyRepository.save(story);
        return StoryCreateResponse.from(story);
    }

    @Transactional
    public StoryResponse getStory(Long memberId, Long storyId) {
        Story story = findStory(storyId);

        // TODO: BLOCKED_MEMBER — Block 도메인 구현 후 차단 관계 확인 추가

        if (!story.getMemberId().equals(memberId)) {
            if (!storyVisitorRepository.existsByStoryIdAndMemberId(storyId, memberId)) {
                try {
                    StoryVisitor visitor = StoryVisitor.builder()
                            .storyId(storyId)
                            .memberId(memberId)
                            .build();
                    storyVisitorRepository.save(visitor);
                } catch (DataIntegrityViolationException ignored) {
                    // 동시 요청으로 인한 중복 삽입 — DB UNIQUE 제약이 이미 막아줌
                }
            }
        }

        return StoryResponse.from(story);
    }

    @Transactional
    public void deleteStory(Long memberId, Long storyId) {
        Story story = findStory(storyId);
        checkOwner(story, memberId);
        story.softDelete();
    }

    public List<StoryFeedResponse> getStoryFeed(Long memberId) {
        List<Long> followingIds = followRepository.findFollowingIdsByFollowerId(memberId);
        if (followingIds.isEmpty()) {
            return List.of();
        }

        List<Story> stories = storyRepository.findByMemberIdInAndIsDeletedFalseAndExpiresAtAfterOrderByCreatedAtDesc(followingIds, LocalDateTime.now());
        if (stories.isEmpty()) {
            return List.of();
        }

        List<Long> memberIds = stories.stream()
                .map(Story::getMemberId)
                .distinct()
                .toList();

        Map<Long, Member> memberMap = memberRepository.findAllById(memberIds)
                .stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m));

        Map<Long, List<Story>> storiesByMember = stories.stream()
                .collect(Collectors.groupingBy(Story::getMemberId));

        return memberIds.stream()
                .filter(memberMap::containsKey)
                .map(id -> {
                    List<StoryResponse> memberStories = storiesByMember.getOrDefault(id, List.of())
                            .stream()
                            .map(StoryResponse::from)
                            .toList();
                    return StoryFeedResponse.of(memberMap.get(id), memberStories);
                })
                .toList();
    }

    public List<StoryVisitorResponse> getStoryVisitors(Long memberId, Long storyId) {
        Story story = findStory(storyId);
        checkOwner(story, memberId);

        List<StoryVisitor> visitors = storyVisitorRepository.findByStoryIdOrderByCreatedAtDesc(storyId);
        if (visitors.isEmpty()) {
            return List.of();
        }

        List<Long> visitorIds = visitors.stream()
                .map(StoryVisitor::getMemberId)
                .toList();

        Map<Long, Member> memberMap = memberRepository.findAllById(visitorIds)
                .stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m));

        return visitors.stream()
                .filter(v -> memberMap.containsKey(v.getMemberId()))
                .map(v -> StoryVisitorResponse.of(v, memberMap.get(v.getMemberId())))
                .toList();
    }

    private Story findStory(Long storyId) {
        return storyRepository.findByStoryIdAndIsDeletedFalseAndExpiresAtAfter(storyId, LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(ErrorCode.STORY_NOT_FOUND));
    }

    private void checkOwner(Story story, Long memberId) {
        if (!story.getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.STORY_ACCESS_DENIED);
        }
    }

    private void validateImageType(String imageType) {
        if (imageType == null || !ALLOWED_IMAGE_TYPES.contains(imageType)) {
            throw new BusinessException(ErrorCode.INVALID_IMAGE_TYPE);
        }
    }
}
