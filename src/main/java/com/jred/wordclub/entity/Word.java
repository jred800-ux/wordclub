package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "words")
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String spelling;

    @Column(nullable = false, length = 500)
    private String meaning;

    @Column(length = 200)
    private String phonetic;

    @Column(length = 50)
    private String partOfSpeech;

    @Column(length = 100)
    private String root;

    @Column(length = 100)
    private String prefix;

    @Column(length = 100)
    private String suffix;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
