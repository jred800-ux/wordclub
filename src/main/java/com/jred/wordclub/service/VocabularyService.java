package com.jred.wordclub.service;

import com.jred.wordclub.entity.*;
import com.jred.wordclub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final BookRepository bookRepository;
    private final VocExampleRepository vocExampleRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserFavoriteRepository favoriteRepository;
    private final BlacklistService blacklistService;

    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBook(Long bookId) {
        return bookRepository.findById(bookId);
    }

    public Page<Vocabulary> listWords(Long bookId, Long userId, Pageable pageable) {
        if (bookId != null) {
            Set<Long> blacklistedIds = userId != null
                    ? blacklistService.getBlacklistedWordIds(userId)
                    : Collections.emptySet();
            List<Long> excludeList = blacklistedIds.isEmpty()
                    ? List.of(-1L)
                    : new ArrayList<>(blacklistedIds);
            return vocabularyRepository.findByBookIdExcluding(bookId, excludeList, pageable);
        }
        return vocabularyRepository.findAll(pageable);
    }

    public Optional<Vocabulary> getWord(Long id) {
        return vocabularyRepository.findById(id);
    }

    public Page<Vocabulary> search(String keyword, Pageable pageable) {
        return vocabularyRepository.findBySpellingOrParaphrase(keyword, pageable);
    }

    public Map<String, Object> getBookProgress(Long userId, Long bookId, int dailyNewWordCount) {
        Map<String, Object> result = new HashMap<>();
        Set<Long> blacklisted = blacklistService.getBlacklistedWordIds(userId);
        List<Long> excludeList = blacklisted.isEmpty() ? List.of(-1L) : new ArrayList<>(blacklisted);
        long totalWords = vocabularyRepository.countByBookIdExcluding(bookId, excludeList);
        result.put("totalWords", totalWords);
        long studiedCount = progressRepository.countByUserIdAndBookId(userId, bookId);
        result.put("studiedCount", studiedCount);
        long masteredCount = progressRepository.countByUserIdAndBookIdAndStatus(userId, bookId, "MASTERED");
        result.put("masteredCount", masteredCount);
        double pct = totalWords > 0 ? Math.round(studiedCount * 1000.0 / totalWords) / 10.0 : 0;
        result.put("completionPercent", pct);

        // Estimated completion date
        long remaining = totalWords - studiedCount;
        if (remaining > 0 && dailyNewWordCount > 0) {
            int daysNeeded = (int) Math.ceil((double) remaining / dailyNewWordCount);
            String estimatedDate = java.time.LocalDate.now().plusDays(daysNeeded).toString();
            result.put("estimatedCompletionDate", estimatedDate);
        } else if (remaining <= 0) {
            result.put("estimatedCompletionDate", "已完成");
        }

        Optional<UserWordProgress> last = progressRepository
                .findFirstByUserIdAndBookIdOrderByUpdatedAtDesc(userId, bookId);
        if (last.isPresent()) {
            Long lastWordId = last.get().getWordId();
            result.put("lastWordId", lastWordId);
            // Use COUNT query to find position — avoids loading all words into memory
            long pos = vocabularyRepository.countByBookIdUpToWordId(bookId, lastWordId);
            if (pos > 0) {
                int zeroIndexed = (int) (pos - 1);
                result.put("resumePage", zeroIndexed / 20);
                result.put("resumeIndex", zeroIndexed % 20);
            }
        }
        return result;
    }

    public List<VocExample> getExamples(Long wordId) {
        return vocExampleRepository.findByWordId(wordId);
    }

    public Optional<UserWordProgress> getProgress(Long userId, Long wordId) {
        return progressRepository.findByUserIdAndWordId(userId, wordId);
    }

    @Transactional
    public UserWordProgress recordReview(Long userId, Long wordId, Long bookId, int quality) {
        UserWordProgress progress = progressRepository.findByUserIdAndWordId(userId, wordId)
                .orElseGet(() -> {
                    UserWordProgress p = new UserWordProgress();
                    p.setUserId(userId);
                    p.setWordId(wordId);
                    p.setBookId(bookId);
                    p.setStatus("NEW");
                    return p;
                });

        sm2Update(progress, quality);
        return progressRepository.save(progress);
    }

    public Map<String, Long> getTodayStats(Long userId) {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        Map<String, Long> stats = new HashMap<>();

        long todayNewCount = progressRepository.countByUserIdAndCreatedAtAfter(userId, todayStart);
        long todayReviewCount = progressRepository
                .countByUserIdAndUpdatedAtAfterAndCreatedAtBefore(userId, todayStart, todayStart);
        long pendingReviewCount = progressRepository.countByUserIdAndNextReviewAtBefore(userId, LocalDateTime.now());

        stats.put("todayNewCount", todayNewCount);
        stats.put("todayReviewCount", todayReviewCount);
        stats.put("pendingReviewCount", pendingReviewCount);
        stats.put("todayLearned", todayNewCount + todayReviewCount);
        stats.put("mastered", progressRepository.countByUserIdAndStatus(userId, "MASTERED"));
        stats.put("reviewing", progressRepository.countByUserIdAndStatus(userId, "REVIEW"));
        return stats;
    }

    public List<UserWordProgress> getReviewQueue(Long userId) {
        return progressRepository.findByUserIdAndNextReviewAtBefore(userId, LocalDateTime.now());
    }

    public List<UserFavorite> listFavorites(Long userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void addFavorite(Long userId, Long wordId) {
        if (favoriteRepository.findByUserIdAndWordId(userId, wordId).isEmpty()) {
            UserFavorite fav = new UserFavorite();
            fav.setUserId(userId);
            fav.setWordId(wordId);
            favoriteRepository.save(fav);
        }
    }

    @Transactional
    public void removeFavorite(Long userId, Long wordId) {
        favoriteRepository.deleteByUserIdAndWordId(userId, wordId);
    }

    public boolean isFavorited(Long userId, Long wordId) {
        return favoriteRepository.findByUserIdAndWordId(userId, wordId).isPresent();
    }

    public long countFavorites(Long userId) {
        return favoriteRepository.countByUserId(userId);
    }

    public List<Map<String, Object>> getFavoriteWords(Long userId) {
        List<UserFavorite> favs = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        if (favs.isEmpty()) return List.of();
        List<Long> wordIds = favs.stream().map(UserFavorite::getWordId).toList();
        Map<Long, Vocabulary> wordMap = vocabularyRepository.findByIdIn(wordIds)
                .stream().collect(Collectors.toMap(Vocabulary::getId, v -> v));
        // Preserve favorite order (newest first)
        return favs.stream()
                .filter(f -> wordMap.containsKey(f.getWordId()))
                .map(f -> {
                    Vocabulary v = wordMap.get(f.getWordId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("id", v.getId());
                    item.put("spelling", v.getSpelling());
                    item.put("ukPhonetic", v.getUkPhonetic());
                    item.put("usPhonetic", v.getUsPhonetic());
                    item.put("paraphrase", v.getParaphrase());
                    item.put("frequency", v.getFrequency());
                    item.put("favoritedAt", f.getCreatedAt());
                    return item;
                }).toList();
    }

    private void sm2Update(UserWordProgress p, int quality) {
        if (quality < 0) quality = 0;
        if (quality > 5) quality = 5;

        if (quality < 3) {
            p.setRepetitions(0);
            p.setReviewInterval(1);
            p.setStatus("LEARNING");
        } else {
            int reps = p.getRepetitions() + 1;
            double ef = p.getEaseFactor() + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));
            if (ef < 1.3) ef = 1.3;

            int interval;
            if (reps == 1) interval = 1;
            else if (reps == 2) interval = 3;
            else interval = (int) Math.round(p.getReviewInterval() * ef);

            p.setEaseFactor(ef);
            p.setRepetitions(reps);
            p.setReviewInterval(interval);
            p.setStatus(reps >= 5 ? "MASTERED" : "REVIEW");
        }

        p.setNextReviewAt(LocalDateTime.now().plusDays(p.getReviewInterval()));
    }
}
