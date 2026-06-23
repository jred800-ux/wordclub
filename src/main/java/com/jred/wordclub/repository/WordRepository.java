package com.jred.wordclub.repository;

import com.jred.wordclub.entity.Word;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WordRepository extends JpaRepository<Word, Long> {
}
