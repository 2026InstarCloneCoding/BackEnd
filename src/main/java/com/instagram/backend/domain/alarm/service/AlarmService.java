package com.instagram.backend.domain.alarm.service;

import com.instagram.backend.domain.alarm.dto.AlarmResponse;
import com.instagram.backend.domain.alarm.entity.Alarm;
import com.instagram.backend.domain.alarm.repository.AlarmRepository;
import com.instagram.backend.domain.member.entity.Member;
import com.instagram.backend.domain.member.repository.MemberRepository;
import com.instagram.backend.global.dto.CursorPageResponse;
import com.instagram.backend.global.exception.BusinessException;
import com.instagram.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class AlarmService {

    private static final String UNREAD_KEY_PREFIX = "alarm:unread:";
    private static final Duration UNREAD_TTL = Duration.ofDays(30);

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void createAlarm(Alarm alarm) {
        if (alarm.getTargetMemberId().equals(alarm.getSenderMemberId())) return;
        alarmRepository.save(alarm);
        try {
            String key = UNREAD_KEY_PREFIX + alarm.getTargetMemberId();
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, UNREAD_TTL);
        } catch (Exception e) {
            log.warn("Redis INCR 실패 (alarm:unread:{}): {}", alarm.getTargetMemberId(), e.getMessage());
        }
    }

    public CursorPageResponse<AlarmResponse> getAlarms(Long memberId, String cursor, int limit) {
        if (limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_LIMIT);
        }

        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Alarm> alarms;

        if (cursor == null || cursor.isBlank()) {
            alarms = alarmRepository.findByTargetMemberIdOrderByAlarmIdDesc(memberId, pageRequest);
        } else {
            try {
                alarms = alarmRepository.findByTargetMemberIdAndAlarmIdLessThanOrderByAlarmIdDesc(
                        memberId, Long.parseLong(cursor), pageRequest);
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INVALID_CURSOR);
            }
        }

        boolean hasNext = alarms.size() > limit;
        List<Alarm> result = hasNext ? alarms.subList(0, limit) : alarms;

        if (result.isEmpty()) {
            return CursorPageResponse.of(List.of(), null, false);
        }

        Set<Long> senderIds = result.stream()
                .map(Alarm::getSenderMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> senderMap = memberRepository.findByMemberIdInAndIsDeletedFalse(senderIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m));

        List<AlarmResponse> content = result.stream()
                .filter(alarm -> senderMap.containsKey(alarm.getSenderMemberId()))
                .map(alarm -> AlarmResponse.of(alarm, senderMap.get(alarm.getSenderMemberId())))
                .collect(Collectors.toList());

        String nextCursor = hasNext ? String.valueOf(result.get(result.size() - 1).getAlarmId()) : null;

        return CursorPageResponse.of(content, nextCursor, hasNext);
    }

    @Transactional
    public void readAlarm(Long memberId, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ALARM_NOT_FOUND));
        if (!alarm.getTargetMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.ALARM_ACCESS_DENIED);
        }
        boolean wasUnread = alarm.getReadAt() == null;
        alarm.markAsRead();
        if (wasUnread) {
            try {
                String key = UNREAD_KEY_PREFIX + memberId;
                Long current = redisTemplate.opsForValue().decrement(key);
                if (current != null && current < 0) {
                    redisTemplate.opsForValue().set(key, "0", UNREAD_TTL);
                }
            } catch (Exception e) {
                log.warn("Redis DECR 실패 (alarm:unread:{}): {}", memberId, e.getMessage());
            }
        }
    }

    public long getUnreadCount(Long memberId) {
        try {
            String value = redisTemplate.opsForValue().get(UNREAD_KEY_PREFIX + memberId);
            if (value != null) return Long.parseLong(value);
            long count = alarmRepository.countByTargetMemberIdAndReadAtIsNull(memberId);
            redisTemplate.opsForValue().set(UNREAD_KEY_PREFIX + memberId, String.valueOf(count), UNREAD_TTL);
            return count;
        } catch (Exception e) {
            log.warn("Redis 조회 실패 (alarm:unread:{}), DB fallback: {}", memberId, e.getMessage());
            return alarmRepository.countByTargetMemberIdAndReadAtIsNull(memberId);
        }
    }
}
