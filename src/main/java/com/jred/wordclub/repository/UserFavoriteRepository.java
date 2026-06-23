package com.jred.wordclub.repository;

import com.jred.wordclub.entity.UserFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserFavoriteRepository extends JpaRepository<UserFavorite, Long> {

    List<UserFavorite> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<UserFavorite> findByUserIdAndWordId(Long userId, Long wordId);

    void deleteByUserIdAndWordId(Long userId, Long wordId);
}
