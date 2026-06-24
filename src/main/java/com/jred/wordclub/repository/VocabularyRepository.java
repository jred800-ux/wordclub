package com.jred.wordclub.repository;

import com.jred.wordclub.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Long> {

    Page<Vocabulary> findBySpellingContaining(String spelling, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v WHERE v.spelling LIKE CONCAT('%',:kw,'%') OR v.paraphrase LIKE CONCAT('%',:kw,'%')")
    Page<Vocabulary> findBySpellingOrParaphrase(String kw, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId ORDER BY v.id ASC")
    Page<Vocabulary> findByBookId(Long bookId, Pageable pageable);

    @Query("SELECT v FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId ORDER BY v.id ASC")
    List<Vocabulary> findAllByBookId(Long bookId);

    @Query("SELECT COUNT(v) FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId")
    long countByBookId(Long bookId);

    @Query("SELECT v FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId AND v.id NOT IN :excludeIds ORDER BY v.id ASC")
    Page<Vocabulary> findByBookIdExcluding(Long bookId, List<Long> excludeIds, Pageable pageable);

    @Query("SELECT COUNT(v) FROM Vocabulary v JOIN VocBook vb ON v.id = vb.wordId WHERE vb.bookId = :bookId AND v.id NOT IN :excludeIds")
    long countByBookIdExcluding(Long bookId, List<Long> excludeIds);
}
