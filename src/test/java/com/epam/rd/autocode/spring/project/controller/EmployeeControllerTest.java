package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientStatsDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.filter.ClientQueryFilter;
import com.epam.rd.autocode.spring.project.dto.filter.EmployeeOrderQueryFilter;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;

@WebMvcTest(EmployeeController.class)
@Import(SecurityConfig.class)
@DisplayName("EmployeeController")
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderServiceImpl orderService;
    @MockBean
    private ClientServiceImpl clientService;

    @Nested
    @DisplayName("GET /employee/clients")
    class ClientsPage {
        @Test
        @DisplayName("returns employee/clients view")
        void returnsView() throws Exception {
            ClientQueryFilter queryFilter = new ClientQueryFilter();
            Page<ClientDTO> page = new PageImpl<>(List.of(new ClientDTO()), PageRequest.of(0, 5), 1);

            when(clientService.getClientStats()).thenReturn(new ClientStatsDTO());
            when(clientService.getAllClientsBySearch(anyString(), any(Pageable.class))).thenReturn(page);

            mockMvc
                    .perform(get("/employee/clients")
                            .flashAttr("queryFilter", queryFilter))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employee/clients"))
                    .andExpectAll(model().attribute("queryFilter", queryFilter),
                            model().attributeExists("clients"),
                            model().attributeExists("stats"));
        }
    }

    @Nested
    @DisplayName("POST /employee/clients/toggle")
    class ToggleClient {
        @Test
        @DisplayName("toggles client's active attribute")
        void togglesClient() throws Exception {
            doNothing().when(clientService).toggleClientByEmail(anyString());

            mockMvc
                    .perform(post("/employee/clients/toggle")
                            .param("email", "email"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/clients"));
        }

        @Test
        @DisplayName("adds provided queryString to redirect url")
        void addsQueryString() throws Exception {
            String queryString = "queryString";

            mockMvc
                    .perform(post("/employee/clients/toggle")
                            .param("email", "email")
                            .param("queryString", queryString))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/employee/clients?" + queryString));
        }
    }

    @Nested
    @DisplayName("GET /employee/orders")
    class OrdersPage {
        @Test
        @DisplayName("returns employee/orders view")
        void returnsView() throws Exception {
            EmployeeOrderQueryFilter queryFilter = new EmployeeOrderQueryFilter();
            OrderDTO order = new OrderDTO(1L, "email", "email", LocalDateTime.now(),
                    BigDecimal.ONE, List.of(new BookItemDTO()));
            Page<OrderDTO> page = new PageImpl<>(List.of(order), PageRequest.of(0, 5), 1);

            when(orderService.getAllOrdersBySearchAndConfirmed(anyString(), any(Pageable.class), anyBoolean())).thenReturn(page);

            mockMvc
                    .perform(get("/employee/orders")
                            .flashAttr("queryFilter", queryFilter))
                    .andExpect(status().isOk())
                    .andExpect(view().name("employee/orders"))
                    .andExpectAll(model().attribute("queryFilter", queryFilter),
                            model().attributeExists("orders"));
        }
    }
}
