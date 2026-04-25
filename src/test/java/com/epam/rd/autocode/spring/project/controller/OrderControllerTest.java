package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.Employee;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
@DisplayName("OrderController")
public class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceImpl orderService;

    private static final Client CLIENT = SecurityConfig.CLIENT;
    private static final Employee EMPLOYEE = SecurityConfig.EMPLOYEE;
    private static final OrderDTO ORDER = new OrderDTO(1L, "email", "email", LocalDateTime.now(),
            BigDecimal.ONE, List.of(new BookItemDTO()));

    @Nested
    @DisplayName("GET /orders/{id}")
    class OrderDetailsPage {
        @Test
        @DisplayName("returns orders/detail view")
        void returnsView() throws Exception {
            when(orderService.getOrderById(eq(ORDER.getId()))).thenReturn(ORDER);

            mockMvc
                    .perform(get("/orders/1"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("orders/detail"))
                    .andExpect(model().attribute("order", ORDER));
        }
    }

    @Nested
    @DisplayName("POST /orders/place")
    class PlaceOrder {
        @Test
        @DisplayName("adds order")
        void addsOrder() throws Exception {
            String basketJson = "[{\"bookName\": \"name\", \"quantity\": 1}]";

            when(orderService.addOrder(anyList(), eq(CLIENT.getId()))).thenReturn(ORDER);

            mockMvc
                    .perform(post("/orders/place")
                            .param("basketJson", basketJson)
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/orders"));
        }

        @Test
        @DisplayName("mapping errors re-renders current view")
        void mappingErrors() throws Exception {
            String basketJson = "";

            mockMvc
                    .perform(post("/orders/place")
                            .param("basketJson", basketJson)
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/basket"))
                    .andExpect(model().attributeExists("errorMessage"));

            verifyNoInteractions(orderService);
        }
    }

    @Nested
    @DisplayName("POST /orders/confirm")
    class ConfirmOrder {
        @BeforeEach
        void setUp() {
            doNothing().when(orderService).confirmOrderById(anyLong(), eq(EMPLOYEE.getId()));
        }

        @Test
        @DisplayName("confirms order")
        void confirmsOrder() throws Exception {
            mockMvc
                    .perform(post("/orders/confirm")
                            .param("id", "1")
                            .with(user(EMPLOYEE)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/orders"));
        }

        @Test
        @DisplayName("adds provided queryString to redirect url")
        void addsQueryString() throws Exception {
            String queryString = "queryString";

            mockMvc
                    .perform(post("/orders/confirm")
                            .param("id", "1")
                            .param("queryString", queryString)
                            .with(user(EMPLOYEE)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/orders?" + queryString));
        }
    }

    @Nested
    @DisplayName("POST /orders/delete")
    class DeleteOrder {
        @BeforeEach
        void setUp() {
            doNothing().when(orderService).deleteOrderById(anyLong());
        }

        @Test
        @DisplayName("deletes order")
        void deletesOrder() throws Exception {
            mockMvc
                    .perform(post("/orders/delete")
                            .param("id", "1"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/orders"));
        }

        @Test
        @DisplayName("adds provided queryString to redirect url")
        void addsQueryString() throws Exception {
            String queryString = "queryString";

            mockMvc
                    .perform(post("/orders/delete")
                            .param("id", "1")
                            .param("queryString", queryString))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/orders?" + queryString));
        }
    }
}
