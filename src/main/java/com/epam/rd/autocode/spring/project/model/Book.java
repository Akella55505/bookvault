package com.epam.rd.autocode.spring.project.model;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private String genre;
    @Enumerated(EnumType.STRING)
    private AgeGroup ageGroup;
    @Column(nullable = false)
    private BigDecimal price;
    private LocalDate publicationDate;
    @Column(nullable = false)
    private String author;
    private Integer pages;
    @Column(length = 550)
    private String characteristics;
    @Column(length = 5050)
    private String description;
    @Enumerated(EnumType.STRING)
    private Language language;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(mappedBy = "booksInBasket")
    private Set<Client> clients;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookItem> bookItems;
}
