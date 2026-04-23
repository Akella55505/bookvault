package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.*;

public interface OrderService {

    Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable);

    Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable);

    OrderDTO addOrder(List<BookItemDTO> bookItems, Long userId);
}
