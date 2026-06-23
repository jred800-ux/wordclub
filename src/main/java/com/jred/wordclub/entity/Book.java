package com.jred.wordclub.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "tb_book")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookid")
    private Long id;

    @Column(nullable = false, length = 255)
    private String bookname;

    @Column(name = "voccount")
    private Integer vocCount;

    @Column
    private Integer status;
}
