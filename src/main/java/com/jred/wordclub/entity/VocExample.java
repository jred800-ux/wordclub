package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_voc_examples")
public class VocExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exapid")
    private Long id;

    @Column(name = "wordid", nullable = false)
    private Long wordId;

    @Column(name = "en", length = 255)
    private String enSentence;

    @Column(name = "cn", length = 255)
    private String cnSentence;

    @Column
    private Integer heat;

    @Column(length = 18)
    private String adddate;
}
