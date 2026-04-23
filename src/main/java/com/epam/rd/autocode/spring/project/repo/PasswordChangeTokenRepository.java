package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.PasswordChangeToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordChangeTokenRepository extends CrudRepository<PasswordChangeToken, Long> {
    void deleteByExpirationDateBefore(LocalDateTime now);

    Optional<PasswordChangeToken> findByToken(String token);

    Optional<PasswordChangeToken> findByUserEmail(String email);
}
