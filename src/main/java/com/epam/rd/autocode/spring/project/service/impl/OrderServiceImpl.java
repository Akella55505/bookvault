package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotEnoughMoneyException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.BookItem;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.repo.BookItemRepository;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;
    private final ClientRepository clientRepository;
    private final BookRepository bookRepository;
    private final BookItemRepository bookItemRepository;

    public Page<OrderDTO> getAllOrdersBySearchAndConfirmed(String search, Pageable pageable, boolean confirmed) {
        Page<Order> ordersPage;

        if (confirmed) {
            ordersPage = orderRepository.findAllBySearchAndEmployeeIdIsNotNull(search, pageable);
        } else {
            ordersPage = orderRepository.findAllBySearchAndEmployeeIdIsNull(search, pageable);
        }

        List<OrderDTO> orderList = ordersPage.stream().map(order -> modelMapper.map(order, OrderDTO.class)).toList();
        return new PageImpl<>(orderList, pageable, ordersPage.getTotalElements());
    }

    @Override
    public Page<OrderDTO> getOrdersByClient(String clientEmail, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAllByClientEmail(clientEmail, pageable);
        List<OrderDTO> ordersList = ordersPage.stream().map(order -> modelMapper.map(order, OrderDTO.class)).toList();
        return new PageImpl<>(ordersList, pageable, ordersPage.getTotalElements());
    }

    @Override
    public Page<OrderDTO> getOrdersByEmployee(String employeeEmail, Pageable pageable) {
        Page<Order> ordersPage = orderRepository.findAllByEmployeeEmail(employeeEmail, pageable);
        List<OrderDTO> ordersList = ordersPage.stream().map(order -> modelMapper.map(order, OrderDTO.class)).toList();
        return new PageImpl<>(ordersList, pageable, ordersPage.getTotalElements());
    }

    @Override
    @Transactional
    public OrderDTO addOrder(List<BookItemDTO> bookItemDTOs, Long clientId) {
        Client client = clientRepository.findById(clientId).orElseThrow(() -> new NotFoundException("Client not found"));

        Order order = Order.builder()
                .client(client)
                .build();
        order = orderRepository.save(order);

        Order finalOrder = order;
        List<BookItem> bookItems = bookItemDTOs.stream().map(item -> BookItem.builder()
                .book(bookRepository.findByName(item.getBookName()).orElseThrow(() -> new NotFoundException("Book not found")))
                .order(finalOrder)
                .quantity(item.getQuantity())
                .build()).collect(Collectors.toList());
        bookItemRepository.saveAll(bookItems);

        BigDecimal totalPrice = BigDecimal.ZERO;
        for (BookItem bookItem : bookItems) {
            totalPrice = totalPrice.add(bookItem.getBook().getPrice()
                    .multiply(BigDecimal.valueOf(bookItem.getQuantity())));
        }

        if (totalPrice.compareTo(client.getBalance()) > 0) {
            throw new NotEnoughMoneyException("Not enough money for the operation");
        }

        order.setBookItems(bookItems);
        order.setPrice(totalPrice);
        order = orderRepository.save(order);

        client.setBalance(client.getBalance().subtract(totalPrice));
        clientRepository.save(client);
        clientRepository.clearBasketByClientId(clientId);

        return modelMapper.map(order, OrderDTO.class);
    }

    @Transactional
    public void confirmOrderById(Long orderId, Long employeeId) {
        orderRepository.updateEmployeeEmailById(orderId, employeeId);
    }

    @Transactional
    public void deleteOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        Client client = order.getClient();
        client.setBalance(client.getBalance().add(order.getPrice()));
        orderRepository.deleteById(id);
    }

    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new NotFoundException("Order not found"));
        return modelMapper.map(order, OrderDTO.class);
    }
}
