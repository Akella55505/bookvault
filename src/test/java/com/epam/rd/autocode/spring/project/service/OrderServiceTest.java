package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.exception.NotEnoughMoneyException;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Order;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookItemRepository;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.repo.ClientRepository;
import com.epam.rd.autocode.spring.project.repo.OrderRepository;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookItemRepository bookItemRepository;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long CLIENT_ID = 1L;
    private static final Long ORDER_ID = 10L;
    private static final Long EMPLOYEE_ID = 5L;
    private static final String CLIENT_EMAIL = "client@example.com";
    private static final String EMP_EMAIL = "emp@example.com";
    private static final String BOOK_NAME = "Clean Code";
    private static final Pageable PAGE = PageRequest.of(0, 10);

    private Client client;
    private Book book;
    private Order order;
    private OrderDTO orderDTO;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(CLIENT_ID)
                .email(CLIENT_EMAIL)
                .balance(new BigDecimal("100.00"))
                .build();

        book = new Book(1L, BOOK_NAME, "genre", AgeGroup.TEEN, new BigDecimal("20.00"),
                LocalDate.now(), "author", 1, "characteristics", "description",
                Language.ENGLISH, new HashSet<>(), new ArrayList<>());

        order = Order.builder()
                .id(ORDER_ID)
                .client(client)
                .price(BigDecimal.ZERO)
                .build();

        orderDTO = new OrderDTO();
        orderDTO.setId(ORDER_ID);
    }

    @Nested
    @DisplayName("getAllOrdersBySearchAndConfirmed()")
    class GetAllOrdersBySearch {
        @Test
        @DisplayName("confirmed=true queries findAllBySearchAndEmployeeIdIsNotNull")
        void confirmedDelegatesToNotNullQuery() {
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAllBySearchAndEmployeeIdIsNotNull(anyString(), eq(PAGE)))
                    .thenReturn(page);
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getAllOrdersBySearchAndConfirmed("q", PAGE, true);

            verify(orderRepository).findAllBySearchAndEmployeeIdIsNotNull("q", PAGE);
            verify(orderRepository, never()).findAllBySearchAndEmployeeIdIsNull(any(), any());
            assertThat(result.getContent()).containsExactly(orderDTO);
        }

        @Test
        @DisplayName("confirmed=false queries findAllBySearchAndEmployeeIdIsNull")
        void unconfirmedDelegatesToNullQuery() {
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAllBySearchAndEmployeeIdIsNull(anyString(), eq(PAGE)))
                    .thenReturn(page);
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getAllOrdersBySearchAndConfirmed("q", PAGE, false);

            verify(orderRepository).findAllBySearchAndEmployeeIdIsNull("q", PAGE);
            verify(orderRepository, never()).findAllBySearchAndEmployeeIdIsNotNull(any(), any());
            assertThat(result.getContent()).containsExactly(orderDTO);
        }

        @Test
        @DisplayName("maps each Order to OrderDTO and preserves total elements")
        void preservesTotalElements() {
            Order order2 = Order.builder().id(2L).client(client).build();
            OrderDTO dto2 = new OrderDTO();
            Page<Order> page = new PageImpl<>(List.of(order, order2), PAGE, 50);
            when(orderRepository.findAllBySearchAndEmployeeIdIsNull(any(), eq(PAGE))).thenReturn(page);
            when(modelMapper.map(order,  OrderDTO.class)).thenReturn(orderDTO);
            when(modelMapper.map(order2, OrderDTO.class)).thenReturn(dto2);

            Page<OrderDTO> result = orderService.getAllOrdersBySearchAndConfirmed("", PAGE, false);

            assertThat(result.getTotalElements()).isEqualTo(50);
            assertThat(result.getContent()).containsExactly(orderDTO, dto2);
        }
    }

    @Nested
    @DisplayName("getOrdersByClient()")
    class GetOrdersByClient {
        @Test
        @DisplayName("delegates to findAllByClientEmail and maps results")
        void delegatesAndMaps() {
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAllByClientEmail(CLIENT_EMAIL, PAGE)).thenReturn(page);
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getOrdersByClient(CLIENT_EMAIL, PAGE);

            verify(orderRepository).findAllByClientEmail(CLIENT_EMAIL, PAGE);
            assertThat(result.getContent()).containsExactly(orderDTO);
        }

        @Test
        @DisplayName("preserves pagination metadata")
        void preservesPaginationMetadata() {
            Page<Order> page = new PageImpl<>(List.of(order), PAGE, 42);
            when(orderRepository.findAllByClientEmail(CLIENT_EMAIL, PAGE)).thenReturn(page);
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getOrdersByClient(CLIENT_EMAIL, PAGE);

            assertThat(result.getTotalElements()).isEqualTo(42);
            assertThat(result.getPageable()).isEqualTo(PAGE);
        }
    }

    @Nested
    @DisplayName("getOrdersByEmployee()")
    class GetOrdersByEmployee {
        @Test
        @DisplayName("delegates to findAllByEmployeeEmail and maps results")
        void delegatesAndMaps() {
            Page<Order> page = new PageImpl<>(List.of(order));
            when(orderRepository.findAllByEmployeeEmail(EMP_EMAIL, PAGE)).thenReturn(page);
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            Page<OrderDTO> result = orderService.getOrdersByEmployee(EMP_EMAIL, PAGE);

            verify(orderRepository).findAllByEmployeeEmail(EMP_EMAIL, PAGE);
            assertThat(result.getContent()).containsExactly(orderDTO);
        }
    }

    @Nested
    @DisplayName("addOrder()")
    class AddOrder {
        private BookItemDTO itemDTO;

        @BeforeEach
        void setUp() {
            itemDTO = new BookItemDTO();
            itemDTO.setBookName(BOOK_NAME);
            itemDTO.setQuantity(2);
        }

        private void stubHappyPath() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.of(book));
            when(bookItemRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(orderDTO);
        }

        @Test
        @DisplayName("throws NotFoundException when client does not exist")
        void clientNotFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.addOrder(List.of(itemDTO), CLIENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Client not found");

            verifyNoInteractions(orderRepository, bookRepository, bookItemRepository);
        }

        @Test
        @DisplayName("throws NotFoundException when a book does not exist")
        void bookNotFound() {
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.addOrder(List.of(itemDTO), CLIENT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Book not found");
        }

        @Test
        @DisplayName("throws NotEnoughMoneyException when total price exceeds client balance")
        void notEnoughMoney() {
            client.setBalance(new BigDecimal("10.00"));
            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.of(book));
            when(bookItemRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

            assertThatThrownBy(() -> orderService.addOrder(List.of(itemDTO), CLIENT_ID))
                    .isInstanceOf(NotEnoughMoneyException.class)
                    .hasMessageContaining("Not enough money");
        }

        @Test
        @DisplayName("deducts total price from client balance on success")
        void deductsBalance() {
            stubHappyPath();

            orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            ArgumentCaptor<Client> captor = ArgumentCaptor.forClass(Client.class);
            verify(clientRepository).save(captor.capture());
            assertThat(captor.getValue().getBalance())
                    .isEqualByComparingTo(new BigDecimal("60.00"));
        }

        @Test
        @DisplayName("saves order twice — once to get ID, once with items and price")
        void orderSavedTwice() {
            stubHappyPath();

            orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            verify(orderRepository, times(2)).save(any(Order.class));
        }

        @Test
        @DisplayName("sets correct total price on the order")
        void setsCorrectPrice() {
            stubHappyPath();

            orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(2)).save(captor.capture());
            Order savedOrder = captor.getAllValues().get(1);
            assertThat(savedOrder.getPrice()).isEqualByComparingTo(new BigDecimal("40.00"));
        }

        @Test
        @DisplayName("clears basket after successful order")
        void clearsBasket() {
            stubHappyPath();

            orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            verify(clientRepository).clearBasketByClientId(CLIENT_ID);
        }

        @Test
        @DisplayName("returns mapped OrderDTO")
        void returnsMappedDto() {
            stubHappyPath();

            OrderDTO result = orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            assertThat(result).isEqualTo(orderDTO);
        }

        @Test
        @DisplayName("handles multiple book items and sums prices correctly")
        void multipleItems() {
            Book book2 = new Book(1L, "SICP", "genre", AgeGroup.TEEN, new BigDecimal("30.00"),
                    LocalDate.now(), "author", 1, "characteristics", "description",
                    Language.ENGLISH, new HashSet<>(), new ArrayList<>());
            BookItemDTO item2 = new BookItemDTO();
            item2.setBookName("SICP");
            item2.setQuantity(1);

            when(clientRepository.findById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(bookRepository.findByName(BOOK_NAME)).thenReturn(Optional.of(book));
            when(bookRepository.findByName("SICP")).thenReturn(Optional.of(book2));
            when(bookItemRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));
            when(modelMapper.map(any(Order.class), eq(OrderDTO.class))).thenReturn(orderDTO);

            orderService.addOrder(List.of(itemDTO, item2), CLIENT_ID);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository, times(2)).save(captor.capture());
            assertThat(captor.getAllValues().get(1).getPrice())
                    .isEqualByComparingTo(new BigDecimal("70.00"));
        }

        @Test
        @DisplayName("exact balance equals total price — order succeeds (boundary)")
        void exactBalanceBoundary() {
            client.setBalance(new BigDecimal("40.00"));
            stubHappyPath();

            OrderDTO result = orderService.addOrder(List.of(itemDTO), CLIENT_ID);

            assertThat(result).isEqualTo(orderDTO);
        }
    }

    @Nested
    @DisplayName("confirmOrderById()")
    class ConfirmOrder {
        @Test
        @DisplayName("delegates to orderRepository.updateEmployeeEmailById")
        void delegatesToRepository() {
            doNothing().when(orderRepository).updateEmployeeEmailById(ORDER_ID, EMPLOYEE_ID);

            orderService.confirmOrderById(ORDER_ID, EMPLOYEE_ID);

            verify(orderRepository).updateEmployeeEmailById(ORDER_ID, EMPLOYEE_ID);
        }
    }

    @Nested
    @DisplayName("deleteOrderById()")
    class DeleteOrder {
        @Test
        @DisplayName("throws NotFoundException when order does not exist")
        void orderNotFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.deleteOrderById(ORDER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Order not found");

            verify(orderRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("refunds order price to client balance")
        void refundsBalance() {
            order.setPrice(new BigDecimal("40.00"));
            client.setBalance(new BigDecimal("60.00"));
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderService.deleteOrderById(ORDER_ID);

            assertThat(client.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        }

        @Test
        @DisplayName("calls deleteById with the correct order ID")
        void deletesById() {
            order.setPrice(BigDecimal.ZERO);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

            orderService.deleteOrderById(ORDER_ID);

            verify(orderRepository).deleteById(ORDER_ID);
        }
    }

    @Nested
    @DisplayName("getOrderById()")
    class GetOrderById {
        @Test
        @DisplayName("returns mapped OrderDTO for existing order")
        void returnsMappedDto() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(modelMapper.map(order, OrderDTO.class)).thenReturn(orderDTO);

            OrderDTO result = orderService.getOrderById(ORDER_ID);

            assertThat(result).isEqualTo(orderDTO);
        }

        @Test
        @DisplayName("throws NotFoundException when order does not exist")
        void orderNotFound() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderById(ORDER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("Order not found");

            verifyNoInteractions(modelMapper);
        }
    }
}