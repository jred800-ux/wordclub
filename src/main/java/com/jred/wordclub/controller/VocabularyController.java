package com.jred.wordclub.controller;

import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.*;
import com.jred.wordclub.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/vocabulary")
@RequiredArgsConstructor
public class VocabularyController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long bookId) {
        Page<Vocabulary> result = vocabularyService.listWords(bookId, PageRequest.of(page, size));
        Map<String, Object> data = new HashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("number", result.getNumber());
        return Result.ok(data);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> detail(@PathVariable Long id) {
        Vocabulary word = vocabularyService.getWord(id)
                .orElseThrow(() -> new RuntimeException("单词不存在"));
        List<VocExample> examples = vocabularyService.getExamples(id);
        Map<String, Object> data = new HashMap<>();
        data.put("word", word);
        data.put("examples", examples);
        return Result.ok(data);
    }

    @GetMapping("/search")
    public Result<Map<String, Object>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<Vocabulary> result = vocabularyService.search(q, PageRequest.of(page, size));
        Map<String, Object> data = new HashMap<>();
        data.put("content", result.getContent());
        data.put("totalElements", result.getTotalElements());
        return Result.ok(data);
    }
}
