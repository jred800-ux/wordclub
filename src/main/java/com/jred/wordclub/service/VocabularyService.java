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

@Service
@RequiredArgsConstructor
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;
    private final BookRepository bookRepository;
    private final VocExampleRepository vocExampleRepository;
    private final UserWordProgressRepository progressRepository;
    private final UserFavoriteRepository favoriteRepository;

    public List<Book> listBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBook(Long bookId) {
        return bookRepository.findById(bookId);
    }

    public Page<Vocabulary> listWords(Long bookId, Pageable pageable) {
        if (bookId != null) {
            return vocabularyRepository.findByBookId(bookId, pageable);
        }
        return vocabularyRepository.findAll(pageable);
    }

    public Optional<Vocabulary> getWord(Long id) {
        return vocabularyRepository.findById(id);
    }

    public Page<Vocabulary> search(String keyword, Pageable pageable) {
        return vocabularyRepository.findBySpellingContaining(keyword, pageable);
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
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        Map<String, Long> stats = new HashMap<>();
        stats.put("todayLearned", progressRepository.countByUserIdAndCreatedAtAfter(userId, todayStart));
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
