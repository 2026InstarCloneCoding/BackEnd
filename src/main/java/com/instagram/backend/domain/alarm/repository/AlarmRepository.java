package com.instagram.backend.domain.alarm.repository;

import com.instagram.backend.domain.alarm.entity.Alarm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {

    List<Alarm> findByTargetMemberIdOrderByAlarmIdDesc(Long targetMemberId, Pageable pageable);

    List<Alarm> findByTargetMemberIdAndAlarmIdLessThanOrderByAlarmIdDesc(Long targetMemberId, Long cursor, Pageable pageable);

    long countByTargetMemberIdAndReadAtIsNull(Long targetMemberId);
}
