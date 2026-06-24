package com.jred.wordclub.service;

import com.jred.wordclub.entity.UserCheckin;
import com.jred.wordclub.repository.UserCheckinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CheckinService {

    private final UserCheckinRepository checkinRepository;

    @Transactional
    public Map<String, Object> checkin(Long userId) {
        LocalDate today = LocalDate.now();
        if (checkinRepository.existsByUserIdAndCheckinDate(userId, today)) {
            throw new RuntimeException("今天已经打卡过了");
        }
        UserCheckin checkin = new UserCheckin();
        checkin.setUserId(userId);
        checkin.setCheckinDate(today);
        checkinRepository.save(checkin);

        int streak = calculateStreak(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("streak", streak);
        result.put("totalCheckins", checkinRepository.countByUserId(userId));
        result.put("checkinDate", today);
        return result;
    }

    public Map<String, Object> getStats(Long userId) {
        int streak = calculateStreak(userId);
        long totalCheckins = checkinRepository.countByUserId(userId);
        Optional<UserCheckin> last = checkinRepository.findTopByUserIdOrderByCheckinDateDesc(userId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("streakDays", streak);
        stats.put("totalCheckins", totalCheckins);
        stats.put("lastCheckinDate", last.map(UserCheckin::getCheckinDate).orElse(null));
        stats.put("checkedInToday", checkinRepository.existsByUserIdAndCheckinDate(userId, LocalDate.now()));
        return stats;
    }

    private int calculateStreak(Long userId) {
        LocalDate today = LocalDate.now();
        boolean todayChecked = checkinRepository.existsByUserIdAndCheckinDate(userId, today);
        LocalDate cursor = todayChecked ? today : today.minusDays(1);

        int streak = 0;
        while (checkinRepository.existsByUserIdAndCheckinDate(userId, cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        return streak;
    }
}
