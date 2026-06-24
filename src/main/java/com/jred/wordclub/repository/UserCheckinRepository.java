package com.jred.wordclub.repository;

import com.jred.wordclub.entity.UserCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface UserCheckinRepository extends JpaRepository<UserCheckin, Long> {
    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate date);
    long countByUserId(Long userId);
    Optional<UserCheckin> findTopByUserIdOrderByCheckinDateDesc(Long userId);
    void deleteAllByUserId(Long userId);
}
