package com.jred.wordclub.service;

import com.jred.wordclub.entity.UserWordBlacklist;
import com.jred.wordclub.entity.Vocabulary;
import com.jred.wordclub.repository.UserWordBlacklistRepository;
import com.jred.wordclub.repository.VocabularyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final UserWordBlacklistRepository blacklistRepository;
    private final VocabularyRepository vocabularyRepository;

    @Transactional
    public void addToBlacklist(Long userId, Long wordId) {
        if (!blacklistRepository.existsByUserIdAndWordId(userId, wordId)) {
            UserWordBlacklist bl = new UserWordBlacklist();
            bl.setUserId(userId);
            bl.setWordId(wordId);
            blacklistRepository.save(bl);
        }
    }

    @Transactional
    public void removeFromBlacklist(Long userId, Long wordId) {
        blacklistRepository.deleteByUserIdAndWordId(userId, wordId);
    }

    public List<Map<String, Object>> listBlacklist(Long userId) {
        List<UserWordBlacklist> items = blacklistRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", item.getId());
            map.put("wordId", item.getWordId());
            map.put("createdAt", item.getCreatedAt());
            vocabularyRepository.findById(item.getWordId()).ifPresent(word -> {
                map.put("spelling", word.getSpelling());
                map.put("paraphrase", word.getParaphrase());
            });
            return map;
        }).collect(Collectors.toList());
    }

    public Set<Long> getBlacklistedWordIds(Long userId) {
        return blacklistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(UserWordBlacklist::getWordId)
                .collect(Collectors.toSet());
    }
}
