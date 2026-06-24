package com.jred.wordclub.service;

import com.jred.wordclub.entity.User;
import com.jred.wordclub.exception.BusinessException;
import com.jred.wordclub.exception.NotFoundException;
import com.jred.wordclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final UserSettingRepository userSettingRepository;
    private final UserCheckinRepository checkinRepository;
    private final UserWordBlacklistRepository blacklistRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User register(String username, String email, String rawPassword) {
        if (userRepository.existsByUsername(username)) {
            throw new BusinessException("用户名已存在");
        }
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("邮箱已被注册");
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setNickname(username);
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("邮箱未注册"));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("用户不存在"));
    }

    public boolean verifyPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    // --- Account deletion ---

    @Transactional
    public void deleteAccount(Long userId) {
        progressRepository.deleteAllByUserId(userId);
        favoriteRepository.deleteAllByUserId(userId);
        userSettingRepository.deleteByUserId(userId);
        checkinRepository.deleteAllByUserId(userId);
        blacklistRepository.deleteAllByUserId(userId);
        userRepository.deleteById(userId);
    }

    // --- Admin methods ---

    public Page<User> listUsers(Pageable pageable) {
        return userRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                keyword, keyword, pageable);
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = findById(userId);
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能禁用管理员账户");
        }
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        if ("ADMIN".equals(user.getRole())) {
            throw new BusinessException("不能删除管理员账户");
        }
        deleteAccount(userId);
    }

    public Map<String, Object> getUserDetail(Long userId) {
        User user = findById(userId);
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", user.getId());
        detail.put("username", user.getUsername());
        detail.put("email", user.getEmail());
        detail.put("nickname", user.getNickname());
        detail.put("enabled", user.getEnabled());
        detail.put("role", user.getRole());
        detail.put("createdAt", user.getCreatedAt());

        // User settings
        userSettingRepository.findByUserId(userId).ifPresentOrElse(
                setting -> detail.put("settings", setting),
                () -> detail.put("settings", null));

        // Learning stats
        Map<String, Object> learningStats = new HashMap<>();
        learningStats.put("mastered", progressRepository.countByUserIdAndStatus(userId, "MASTERED"));
        learningStats.put("reviewing", progressRepository.countByUserIdAndStatus(userId, "REVIEW"));
        learningStats.put("learning", progressRepository.countByUserIdAndStatus(userId, "LEARNING"));
        learningStats.put("newCount", progressRepository.countByUserIdAndStatus(userId, "NEW"));
        learningStats.put("favoritesCount", favoriteRepository.countByUserId(userId));
        learningStats.put("blacklistCount", blacklistRepository.countByUserId(userId));
        detail.put("learningStats", learningStats);

        // Checkin stats
        Map<String, Object> checkinStats = new HashMap<>();
        checkinStats.put("totalCheckins", checkinRepository.countByUserId(userId));
        checkinRepository.findTopByUserIdOrderByCheckinDateDesc(userId).ifPresentOrElse(
                c -> checkinStats.put("lastCheckinDate", c.getCheckinDate().toString()),
                () -> checkinStats.put("lastCheckinDate", null));
        detail.put("checkinStats", checkinStats);

        return detail;
    }
}
