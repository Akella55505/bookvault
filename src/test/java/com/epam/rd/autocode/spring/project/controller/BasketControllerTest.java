package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(BasketController.class)
@Import(SecurityConfig.class)
@DisplayName("BasketController")
public class BasketControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientServiceImpl clientService;

    private static final String BOOK_NAME = "book";
    private static final Client CLIENT = SecurityConfig.CLIENT;

    @Nested
    @DisplayName("POST /basket/add")
    class AddBook {
        @BeforeEach
        void setUp() {
            doNothing().when(clientService).addBookToBasket(CLIENT.getId(), BOOK_NAME);
        }

        @Test
        @DisplayName("adds book to basket with returnUrl not provided")
        void successAddBook() throws Exception {
            mockMvc
                    .perform(post("/basket/add")
                            .param("bookName", BOOK_NAME)
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/"));
        }

        @Test
        @DisplayName("adds book to basket and redirects to returnUrl")
        void successAddBookWithReturnUrl() throws Exception {
            mockMvc
                    .perform(post("/basket/add")
                            .param("bookName", BOOK_NAME)
                            .param("returnUrl", "/client/basket")
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/basket"));
        }
    }

    @Nested
    @DisplayName("POST /basket/remove")
    class RemoveBook {
        @Test
        @DisplayName("removes book from basket with returnUrl not provided")
        void successDeleteBook() throws Exception {
            doNothing().when(clientService).removeBookFromBasket(CLIENT.getId(), BOOK_NAME);

            mockMvc
                    .perform(post("/basket/remove")
                            .param("bookName", BOOK_NAME)
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/basket"));
        }
    }

    @Nested
    @DisplayName("POST /basket/clear")
    class ClearBasket {
        @Test
        @DisplayName("clears client's basket")
        void successClearBasket() throws Exception {
            doNothing().when(clientService).clearBasket(CLIENT.getId());

            mockMvc
                    .perform(post("/basket/clear")
                            .with(user(CLIENT)))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/client/basket"));
        }
    }
}
