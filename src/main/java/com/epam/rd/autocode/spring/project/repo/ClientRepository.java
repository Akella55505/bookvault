package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.dto.ClientStatsDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);

    void deleteByEmail(String email);

    @Query("SELECT c.booksInBasket FROM Client c WHERE c.id = :clientId")
    Set<Book> findAllBooksInBasketById(Long clientId);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM baskets WHERE client_id = :clientId AND book_Id = :bookId")
    void removeBookFromBasketByClientIdAndBookId(Long clientId, Long bookId);

    @Modifying
    @Query(nativeQuery = true,
            value = "DELETE FROM baskets WHERE client_id = :clientId")
    void clearBasketByClientId(Long clientId);

    @Modifying
    @Query("UPDATE Client c SET c.password = :password WHERE c.id = :id")
    void updatePasswordById(Long id, String password);

    @Modifying
    @Query("UPDATE Client c SET c.balance = c.balance + :amount WHERE c.id = :id")
    void depositById(Long id, BigDecimal amount);

    @Modifying
    @Query("UPDATE Client c SET c.balance = c.balance - :amount WHERE c.id = :id")
    void withdrawById(Long id, BigDecimal amount);

    @Query("""
        SELECT new com.epam.rd.autocode.spring.project.dto.ClientStatsDTO(count(*), blocked)
        FROM Client JOIN (SELECT count(*) AS blocked FROM Client c WHERE c.active = false)
        """)
    ClientStatsDTO getClientStats();

    @Query("SELECT c FROM Client c WHERE LOWER(c.name) LIKE %:search% OR LOWER(c.email) LIKE %:search%")
    Page<Client> findAllBySearch(String search, Pageable pageable);
}
