package com.jred.wordclub.repository;

import com.jred.wordclub.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    Page<Vocabulary> findBySpellingContaining(String spelling, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId")
    Page<Vocabulary> findByBookId(Long bookId, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId")
    List<Vocabulary> findAllByBookId(Long bookId);

    @Query("SELECT COUNT(v) FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId")
    long countByBookId(Long bookId);
}
