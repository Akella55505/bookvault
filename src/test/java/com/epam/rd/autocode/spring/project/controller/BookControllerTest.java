package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.filter.BookQueryFilter;
import com.epam.rd.autocode.spring.project.model.Client;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
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
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
@DisplayName("BookController")
public class BookControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookServiceImpl bookService;
    @MockBean
    private ClientServiceImpl clientService;

    private static final Client CLIENT = SecurityConfig.CLIENT;
    private static final BookDTO BOOK = new BookDTO("name", "genre", AgeGroup.TEEN, BigDecimal.ONE,LocalDate.now(),
            "author", 100, "characteristics", "description", Language.ENGLISH);

    @Nested
    @DisplayName("GET /books")
    class BooksPage {
        private static final BookQueryFilter QUERY_FILTER = new BookQueryFilter();
        private static final Page<BookDTO> PAGE = new PageImpl<>(List.of(new BookDTO()),
                PageRequest.of(0, 5), 1);

        @BeforeEach
        void setUp() {
            when(bookService.getAllBooksBySearch(anyString(), any(Pageable.class))).thenReturn(PAGE);
        }

        @Test
        @DisplayName("returns books/list view")
        void returnsView() throws Exception {

            mockMvc
                    .perform(get("/books")
                            .flashAttr("queryFilter", QUERY_FILTER))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/list"))
                    .andExpectAll(model().attribute("queryFilter", QUERY_FILTER),
                            model().attributeExists("books"),
                            model().attributeDoesNotExist("basket"));
        }

        @Test
        @DisplayName("adds basket when user provided")
        void addsBasket() throws Exception {
            when(clientService.getBasketItemsById(CLIENT.getId())).thenReturn(new HashSet<>());

            mockMvc
                    .perform(get("/books")
                            .flashAttr("queryFilter", QUERY_FILTER)
                            .with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/list"))
                    .andExpectAll(model().attribute("queryFilter", QUERY_FILTER),
                            model().attributeExists("books"),
                            model().attributeExists("basket"));
        }
    }

    @Nested
    @DisplayName("GET /books/{name}")
    class BookDetailsPage {
        @BeforeEach
        void setUp() {
            when(bookService.getBookByName(anyString())).thenReturn(BOOK);
        }

        @Test
        @DisplayName("returns books/detail view")
        void returnsView() throws Exception {

            mockMvc
                    .perform(get("/books/book"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/detail"))
                    .andExpectAll(model().attribute("book", BOOK),
                            model().attributeDoesNotExist("basket"));
        }

        @Test
        @DisplayName("adds basket when user provided")
        void addsBasket() throws Exception {
            when(clientService.getBasketItemsById(CLIENT.getId())).thenReturn(new HashSet<>());

            mockMvc
                    .perform(get("/books/book")
                            .with(user(CLIENT)))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/detail"))
                    .andExpectAll(model().attribute("book", BOOK),
                            model().attributeExists("basket"));
        }
    }

    @Nested
    @DisplayName("GET /books/add")
    class AddBookPage {
        @Test
        @DisplayName("returns books/add view")
        void returnsView() throws Exception {

            mockMvc
                    .perform(get("/books/add"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/add"))
                    .andExpect(model().attributeExists("bookDTO"));
        }
    }

    @Nested
    @DisplayName("POST /books/add")
    class AddBook {
        @Test
        @DisplayName("adds book")
        void addsBook() throws Exception {
            when(bookService.addBook(eq(BOOK))).thenReturn(BOOK);

            mockMvc
                    .perform(post("/books/add")
                            .flashAttr("bookDTO", BOOK))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books/" + BOOK.getName()));
        }

        @Test
        @DisplayName("validation error re-renders current view")
        void validationErrors() throws Exception {
            BookDTO book = new BookDTO();

            mockMvc
                    .perform(post("/books/add")
                            .flashAttr("bookDTO", book))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books/add"));

            verifyNoInteractions(bookService);
        }
    }

    @Nested
    @DisplayName("GET /books/edit/{name]")
    class EditBookPage {
        @Test
        @DisplayName("returns books/edit view")
        void returnsView() throws Exception {
            when(bookService.getBookByName(anyString())).thenReturn(BOOK);

            mockMvc
                    .perform(get("/books/edit/book"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("books/edit"))
                    .andExpect(model().attribute("bookDTO", BOOK));
        }
    }

    @Nested
    @DisplayName("POST /books/edit/{name]")
    class EditBook {
        @Test
        @DisplayName("edits existing book")
        void editsBook() throws Exception {
            when(bookService.updateBookByName(anyString(), eq(BOOK))).thenReturn(BOOK);

            mockMvc
                    .perform(post("/books/edit/book")
                            .flashAttr("bookDTO", BOOK))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books/" + BOOK.getName()));
        }

        @Test
        @DisplayName("validation error re-renders current view")
        void validationErrors() throws Exception {
            BookDTO book = new BookDTO();

            mockMvc
                    .perform(post("/books/edit/book")
                            .flashAttr("bookDTO", book))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books/edit/" + book.getName()));

            verifyNoInteractions(bookService);
        }
    }

    @Nested
    @DisplayName("POST /books/delete/{name]")
    class DeleteBook {
        @BeforeEach
        void setUp() {
            doNothing().when(bookService).deleteBookByName(anyString());
        }

        @Test
        @DisplayName("deletes existing book")
        void deletesBook() throws Exception {
            mockMvc
                    .perform(post("/books/delete/book"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books"));
        }

        @Test
        @DisplayName("adds provided queryString to redirect url")
        void addsQueryString() throws Exception {
            String queryString = "queryString";

            mockMvc
                    .perform(post("/books/delete/book")
                            .param("queryString", queryString))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(view().name("redirect:/books?" + queryString));
        }
    }
}
