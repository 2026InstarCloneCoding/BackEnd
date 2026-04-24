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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void createAlarm(Alarm alarm) {
        if (alarm.getTargetMemberId().equals(alarm.getSenderMemberId())) return;
        alarmRepository.save(alarm);
    }

    public CursorPageResponse<AlarmResponse> getAlarms(Long memberId, String cursor, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit + 1);
        List<Alarm> alarms;

        if (cursor == null) {
            alarms = alarmRepository.findByTargetMemberIdOrderByAlarmIdDesc(memberId, pageRequest);
        } else {
            alarms = alarmRepository.findByTargetMemberIdAndAlarmIdLessThanOrderByAlarmIdDesc(
                    memberId, Long.parseLong(cursor), pageRequest);
        }

        boolean hasNext = alarms.size() > limit;
        List<Alarm> result = hasNext ? alarms.subList(0, limit) : alarms;

        Set<Long> senderIds = result.stream()
                .map(Alarm::getSenderMemberId)
                .collect(Collectors.toSet());

        Map<Long, Member> senderMap = memberRepository.findAllById(senderIds).stream()
                .collect(Collectors.toMap(Member::getMemberId, m -> m));

        List<AlarmResponse> content = result.stream()
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
        alarm.markAsRead();
    }

    public long getUnreadCount(Long memberId) {
        return alarmRepository.countByTargetMemberIdAndReadAtIsNull(memberId);
    }
}
