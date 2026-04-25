package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.filter.ClientOrderQueryFilter;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(ClientController.class)
@Import(SecurityConfig.class)
@DisplayName("ClientController")
public class ClientControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientServiceImpl clientService;
    @MockBean
    private OrderService orderService;

    private static final Client CLIENT = SecurityConfig.CLIENT;

    @Nested
    @DisplayName("GET /client/basket")
    class BasketPage {
        private static final Set<BookDTO> BASKET_ITEMS = new HashSet<>();

        @BeforeEach
        void setUp() {
            when(clientService.getBasketItemsById(CLIENT.getId())).thenReturn(BASKET_ITEMS);
        }

        @Test
        @DisplayName("returns client/basket view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/client/basket")
                            .with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("client/basket"))
                    .andExpectAll(model().attribute("basketItems", BASKET_ITEMS),
                            model().attribute("total", BASKET_ITEMS
                                    .stream().map(BookDTO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add)));
        }

        @Test
        @DisplayName("returns view with provided errorMessage")
        void returnsWithError() throws Exception {
            String errorMessage = "error";

            mockMvc
                    .perform(get("/client/basket")
                            .param("errorMessage", errorMessage)
                            .with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("client/basket"))
                    .andExpect(model().attribute("errorMessage", errorMessage));
        }
    }

    @Nested
    @DisplayName("GET /client/orders")
    class OrdersPage {
        @Test
        @DisplayName("returns client/orders view")
        void returnsView() throws Exception {
            ClientOrderQueryFilter queryFilter = new ClientOrderQueryFilter();
            Page<OrderDTO> page = new PageImpl<>(List.of(new OrderDTO()), PageRequest.of(0, 5), 1);

            when(orderService.getOrdersByClient(eq(CLIENT.getEmail()), any(Pageable.class))).thenReturn(page);

            mockMvc
                    .perform(get("/client/orders")
                            .flashAttr("queryFilter", queryFilter)
                            .with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("client/orders"))
                    .andExpectAll(model().attribute("queryFilter", queryFilter),
                            model().attributeExists("orders"));
        }
    }

    @Nested
    @DisplayName("POST /client/balance/deposit")
    class Deposit {
        @Test
        @DisplayName("adds provided deposit to user balance")
        void addsDeposit() throws Exception {
            doNothing().when(clientService).depositById(eq(CLIENT.getId()), any(BigDecimal.class));

            mockMvc
                    .perform(post("/client/balance/deposit")
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/profile"));
        }
    }

    @Nested
    @DisplayName("POST /client/balance/withdraw")
    class Withdraw {
        @Test
        @DisplayName("subtracts provided deposit from user balance")
        void subtractsDeposit() throws Exception {
            doNothing().when(clientService).withdrawById(eq(CLIENT.getId()), any(BigDecimal.class));

            mockMvc
                    .perform(post("/client/balance/withdraw")
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/orders"));
        }
    }

    @Nested
    @DisplayName("POST /client/delete")
    class DeleteUser {
        @Test
        @DisplayName("deletes client permanently")
        void deletesClient() throws Exception {
            doNothing().when(clientService).deleteClientByEmail(eq(CLIENT.getEmail()));

            mockMvc
                    .perform(get("/client/delete")
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"));
        }
    }
}
