package com.jred.wordclub.controller;

import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.Word;
import com.jred.wordclub.repository.WordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {

    private final WordRepository wordRepository;

    @GetMapping
    public Result<List<Word>> list() {
        List<Word> words = wordRepository.findAll();
        return Result.ok(words);
    }

    @GetMapping("/{id}")
    public Result<Word> detail(@PathVariable Long id) {
        Word word = wordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("单词不存在"));
        return Result.ok(word);
    }
}
