package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_settings")
public class UserSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "new_word_count")
    private Integer newWordCount = 50;

    @Column(name = "review_ratio")
    private Integer reviewRatio = 1;

    @Column(name = "card_order", length = 20)
    private String cardOrder = "random";

    @Column(name = "large_font")
    private Boolean largeFont = false;

    @Column(name = "dark_mode")
    private Boolean darkMode = false;

    @Column(name = "exam_date", length = 20)
    private String examDate = "";

    @Column(name = "selected_book_id")
    private Long selectedBookId;

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
