package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_voc_book")
public class VocBook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vocbkid")
    private Long id;

    @Column(name = "wordid", nullable = false)
    private Long wordId;

    @Column(name = "bookid", nullable = false)
    private Long bookId;
}
