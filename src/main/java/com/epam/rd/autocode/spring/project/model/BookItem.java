package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "book_items")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;
    private Integer quantity;
}
