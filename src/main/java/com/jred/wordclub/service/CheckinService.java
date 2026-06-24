package com.jred.wordclub.service;

import com.jred.wordclub.entity.UserCheckin;
import com.jred.wordclub.exception.BusinessException;
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
            throw new BusinessException("今天已经打卡过了");
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

    /**
     * 计算连续打卡天数。
     * 一次性取出用户所有打卡日期（倒序），在内存中遍历计算连续天数，
     * 避免 N 次数据库查询。
     */
    private int calculateStreak(Long userId) {
        LocalDate today = LocalDate.now();
        List<LocalDate> dates = checkinRepository.findByUserIdOrderByCheckinDateDesc(userId)
                .stream()
                .map(UserCheckin::getCheckinDate)
                .collect(java.util.stream.Collectors.toList());

        if (dates.isEmpty()) return 0;

        int streak = 0;
        LocalDate expected = today;
        for (LocalDate d : dates) {
            if (d.equals(expected)) {
                streak++;
                expected = expected.minusDays(1);
            } else if (d.isBefore(expected)) {
                break; // gap found, streak ends
            }
            // if d > expected, skip duplicate or future date
        }
        return streak;
    }
}
