package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    void deleteByExpirationDateBefore(LocalDateTime now);

    Optional<RefreshToken> findByUserEmail(String userEmail);
}
