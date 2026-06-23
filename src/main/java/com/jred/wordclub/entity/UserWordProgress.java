package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_word_progress")
public class UserWordProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "word_id", nullable = false)
    private Long wordId;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "ease_factor")
    private Double easeFactor = 2.5;

    @Column(name = "review_interval")
    private Integer reviewInterval = 0;

    @Column
    private Integer repetitions = 0;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Column(length = 20)
    private String status = "NEW";

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
