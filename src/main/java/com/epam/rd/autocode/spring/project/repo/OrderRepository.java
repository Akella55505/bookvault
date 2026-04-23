package com.epam.rd.autocode.spring.project.repo;

import com.epam.rd.autocode.spring.project.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findAllByClientEmail(String clientEmail, Pageable pageable);
    Page<Order> findAllByEmployeeEmail(String employeeEmail, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE LOWER(o.client.email) LIKE %:search% AND o.employee.id IS NOT NULL")
    Page<Order> findAllBySearchAndEmployeeIdIsNotNull(String search, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE LOWER(o.client.email) LIKE %:search% AND o.employee.id IS NULL")
    Page<Order> findAllBySearchAndEmployeeIdIsNull(String search, Pageable pageable);

    @Modifying
    @Query("UPDATE Order o SET o.employee.id = :employeeId WHERE o.id = :id")
    void updateEmployeeEmailById(Long id, Long employeeId);
}
