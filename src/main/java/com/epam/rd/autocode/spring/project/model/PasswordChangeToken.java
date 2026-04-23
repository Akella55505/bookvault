package com.epam.rd.autocode.spring.project.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "password_change_tokens")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordChangeToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String token;
    @Column(nullable = false, unique = true)
    private String userEmail;
    @Column(nullable = false)
    private String newPassword;
    @Builder.Default
    private LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(15);
}
