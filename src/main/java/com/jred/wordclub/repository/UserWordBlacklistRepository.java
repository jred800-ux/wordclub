package com.jred.wordclub.repository;

import com.jred.wordclub.entity.UserWordBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserWordBlacklistRepository extends JpaRepository<UserWordBlacklist, Long> {
    boolean existsByUserIdAndWordId(Long userId, Long wordId);
    void deleteByUserIdAndWordId(Long userId, Long wordId);
    List<UserWordBlacklist> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteAllByUserId(Long userId);
}
