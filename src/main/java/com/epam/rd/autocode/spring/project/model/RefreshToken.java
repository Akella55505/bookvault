package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String userEmail;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime expirationDate = LocalDateTime.now().plusDays(30);
}
