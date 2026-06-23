package com.jred.wordclub.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.UserFavorite;
import com.jred.wordclub.entity.UserWordProgress;
import com.jred.wordclub.entity.UserSetting;
import com.jred.wordclub.service.UserSettingService;
import com.jred.wordclub.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/learning")
@RequiredArgsConstructor
public class LearningController {

    private final VocabularyService vocabularyService;
    private final UserSettingService userSettingService;

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
    public Result<Map<String, Long>> stats() {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.getTodayStats(userId));
    }

    @GetMapping("/progress/{wordId}")
    public Result<?> progress(@PathVariable Long wordId) {
        long userId = StpUtil.getLoginIdAsLong();
        return vocabularyService.getProgress(userId, wordId)
                .<Result<?>>map(Result::ok)
                .orElse(Result.ok(null));
    }

    @GetMapping("/progress/book/{bookId}")
    public Result<Map<String, Object>> bookProgress(@PathVariable Long bookId) {
        long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(vocabularyService.getBookProgress(userId, bookId));
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
