package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookService")
public class BookServiceTest {
    @Mock
    private BookRepository bookRepository;

    private BookServiceImpl bookService;

    private static final Book BOOK = new Book(1L, "name", "genre", AgeGroup.TEEN, BigDecimal.ONE,
            LocalDate.now(), "author", 1, "characteristics", "description",
            Language.ENGLISH, new HashSet<>(), new ArrayList<>());
    private static final Page<Book> PAGE = new PageImpl<>(List.of(BOOK));

    @BeforeEach
    void setUp() {
        bookService = new BookServiceImpl(bookRepository, new ModelMapper());
    }

    @Test
    @DisplayName("returns all books")
    void returnsAllBooks() {
        when(bookRepository.findAll(any(Pageable.class))).thenReturn(PAGE);

        Page<BookDTO> page = bookService.getAllBooks(PageRequest.of(0, 5));

        for (int i = 0; i < PAGE.getTotalElements(); i++) {
            assertTrue(compareBookToBookDTO(PAGE.getContent().get(i), page.getContent().get(i)));
        }
    }

    @Test
    @DisplayName("returns all books by provided search")
    void returnsBySearch() {
        when(bookRepository.findAllBySearch(anyString(), any(Pageable.class))).thenReturn(PAGE);

        Page<BookDTO> page = bookService.getAllBooksBySearch("", PageRequest.of(0, 5));

        for (int i = 0; i < PAGE.getTotalElements(); i++) {
            assertTrue(compareBookToBookDTO(PAGE.getContent().get(i), page.getContent().get(i)));
        }
    }

    @Test
    @DisplayName("returns book by its name")
    void returnsByName() {
        when(bookRepository.findByName(eq(BOOK.getName()))).thenReturn(Optional.of(BOOK));

        BookDTO bookDTO = bookService.getBookByName(BOOK.getName());

        assertTrue(compareBookToBookDTO(BOOK, bookDTO));
    }

    @Test
    @DisplayName("updates book by its name")
    void updatesByName() {
        Book book = new Book(1L, "name", null, null, null, null, null,
                null, null, null, null, null, null);
        BookDTO bookDTO = new BookDTO("newName", null, null, null, null,
                null, null, null, null, null);

        when(bookRepository.findByName(eq(book.getName()))).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        BookDTO returnedBookDTO = bookService.updateBookByName(book.getName(), bookDTO);

        assertEquals(bookDTO.getName(), returnedBookDTO.getName());
        assertEquals(bookDTO.getName(), book.getName());
    }

    @Test
    @DisplayName("deletes book by its name")
    void deletesByName() {
        doNothing().when(bookRepository).deleteByName(BOOK.getName());

        bookService.deleteBookByName(BOOK.getName());

        verify(bookRepository).deleteByName(BOOK.getName());
    }

    @Test
    @DisplayName("adds book")
    void addsByName() {
        ModelMapper modelMapper = new ModelMapper();

        when(bookRepository.save(any(Book.class))).thenReturn(BOOK);

        BookDTO bookDTO = bookService.addBook(modelMapper.map(BOOK, BookDTO.class));

        assertTrue(compareBookToBookDTO(BOOK, bookDTO));
    }

    private boolean compareBookToBookDTO(Book book, BookDTO bookDTO) {
        return book.getName().equals(bookDTO.getName()) &&
                book.getGenre().equals(bookDTO.getGenre()) &&
                book.getAgeGroup().equals(bookDTO.getAgeGroup()) &&
                book.getPrice().equals(bookDTO.getPrice()) &&
                book.getPublicationDate().equals(bookDTO.getPublicationDate()) &&
                book.getAuthor().equals(bookDTO.getAuthor()) &&
                book.getPages().equals(bookDTO.getPages()) &&
                book.getCharacteristics().equals(bookDTO.getCharacteristics()) &&
                book.getDescription().equals(bookDTO.getDescription()) &&
                book.getLanguage().equals(bookDTO.getLanguage());
    }
}
