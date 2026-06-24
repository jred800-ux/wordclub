package com.jred.wordclub.repository;

import com.jred.wordclub.entity.UserWordProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserWordProgressRepository extends JpaRepository<UserWordProgress, Long> {

    Optional<UserWordProgress> findByUserIdAndWordId(Long userId, Long wordId);

    List<UserWordProgress> findByUserIdAndBookId(Long userId, Long bookId);

    List<UserWordProgress> findByUserIdAndNextReviewAtBefore(Long userId, LocalDateTime now);

    long countByUserIdAndStatus(Long userId, String status);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);

    long countByUserIdAndUpdatedAtAfterAndCreatedAtBefore(Long userId, LocalDateTime updatedSince, LocalDateTime createdBefore);

    long countByUserIdAndBookId(Long userId, Long bookId);

    long countByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);

    Optional<UserWordProgress> findFirstByUserIdAndBookIdOrderByUpdatedAtDesc(Long userId, Long bookId);

    void deleteAllByUserId(Long userId);
}
