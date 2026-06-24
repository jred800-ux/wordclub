package com.jred.wordclub.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.UserFavorite;
import com.jred.wordclub.entity.UserWordProgress;
import com.jred.wordclub.entity.UserSetting;
import com.jred.wordclub.service.BlacklistService;
import com.jred.wordclub.service.CheckinService;
import com.jred.wordclub.service.UserSettingService;
import com.jred.wordclub.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final VocabularyService vocabularyService;
    private final UserSettingService userSettingService;
    private final CheckinService checkinService;
    private final BlacklistService blacklistService;

    @PostMapping("/review")
    public Result<UserWordProgress> recordReview(@RequestBody Map<String, Object> body) {
        long userId = StpUtil.getLoginIdAsLong();
        Long wordId = Long.valueOf(body.get("wordId").toString());
        Long bookId = body.get("bookId") != null ? Long.valueOf(body.get("bookId").toString()) : null;
        int quality = Integer.parseInt(body.get("quality").toString());
        return Result.ok(vocabularyService.recordReview(userId, wordId, bookId, quality));
    }

    @GetMapping("/queue")
    public Result<List<UserWordProgress>> reviewQueue() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.getReviewQueue(userId));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        long userId = StpUtil.getLoginIdAsLong();
        Map<String, Long> baseStats = vocabularyService.getTodayStats(userId);
        Map<String, Object> stats = new HashMap<>(baseStats);
        Map<String, Object> checkinStats = checkinService.getStats(userId);
        stats.put("streakDays", checkinStats.get("streakDays"));
        stats.put("totalCheckins", checkinStats.get("totalCheckins"));
        stats.put("checkedInToday", checkinStats.get("checkedInToday"));
        stats.put("favoriteCount", vocabularyService.countFavorites(userId));
        return Result.ok(stats);
    }

    @GetMapping("/progress/{wordId}")
    public Result<?> progress(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        return vocabularyService.getProgress(userId, wordId)
                .<Result<?>>map(Result::ok)
                .orElse(Result.ok(null));
    }

    @GetMapping("/progress/book/{bookId}")
    public Result<Map<String, Object>> bookProgress(@PathVariable Long bookId,
                                                     @RequestParam(defaultValue = "50") int dailyNewWordCount) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.getBookProgress(userId, bookId, dailyNewWordCount));
    }

    @GetMapping("/favorites")
    public Result<List<UserFavorite>> favorites() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.listFavorites(userId));
    }

    @PostMapping("/favorites/{wordId}")
    public Result<?> addFavorite(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        vocabularyService.addFavorite(userId, wordId);
        return Result.ok();
    }

    @DeleteMapping("/favorites/{wordId}")
    public Result<?> removeFavorite(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        vocabularyService.removeFavorite(userId, wordId);
        return Result.ok();
    }

    @GetMapping("/favorites/{wordId}/check")
    public Result<Boolean> checkFavorite(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.isFavorited(userId, wordId));
    }

    @GetMapping("/favorites/words")
    public Result<List<Map<String, Object>>> favoriteWords() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.getFavoriteWords(userId));
    }

    // --- Check-in ---

    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkin() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(checkinService.checkin(userId));
    }

    @GetMapping("/checkin/stats")
    public Result<Map<String, Object>> checkinStats() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(checkinService.getStats(userId));
    }

    // --- Blacklist (trash) ---

    @PostMapping("/blacklist/{wordId}")
    public Result<?> addToBlacklist(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        blacklistService.addToBlacklist(userId, wordId);
        return Result.ok();
    }

    @DeleteMapping("/blacklist/{wordId}")
    public Result<?> removeFromBlacklist(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        blacklistService.removeFromBlacklist(userId, wordId);
        return Result.ok();
    }

    @GetMapping("/blacklist")
    public Result<List<Map<String, Object>>> listBlacklist() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(blacklistService.listBlacklist(userId));
    }

    @GetMapping("/blacklist/ids")
    public Result<Set<Long>> blacklistedIds() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(blacklistService.getBlacklistedWordIds(userId));
    }

    // --- Settings ---

    @GetMapping("/settings")
    public Result<UserSetting> getSettings() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userSettingService.getSettings(userId));
    }

    @PutMapping("/settings")
    public Result<UserSetting> saveSettings(@RequestBody UserSetting body) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(userSettingService.saveSettings(userId, body));
    }
}
