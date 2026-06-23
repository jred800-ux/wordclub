package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_vocabulary")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wordid")
    private Long id;

    @Column(nullable = false, length = 255)
    private String spelling;

    @Column(name = "ukphonetic", length = 255)
    private String ukPhonetic;

    @Column(name = "usphonetic", length = 255)
    private String usPhonetic;

    @Column(length = 1024)
    private String paraphrase;

    @Column(columnDefinition = "DOUBLE")
    private Double frequency;
}
