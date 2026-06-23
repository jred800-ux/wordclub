package com.jred.wordclub.repository;

import com.jred.wordclub.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {
}
