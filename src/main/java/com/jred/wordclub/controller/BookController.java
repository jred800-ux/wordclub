package com.jred.wordclub.controller;

import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.Book;
import com.jred.wordclub.service.VocabularyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final VocabularyService vocabularyService;

    @GetMapping
    public Result<List<Book>> list() {
        return Result.ok(vocabularyService.listBooks());
    }

    @GetMapping("/{id}")
    public Result<Book> detail(@PathVariable Long id) {
        return vocabularyService.getBook(id)
                .map(Result::ok)
                .orElseThrow(() -> new RuntimeException("词书不存在"));
    }
}
