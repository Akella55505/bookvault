package com.epam.rd.autocode.spring.project.service.impl;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.exception.NotFoundException;
import com.epam.rd.autocode.spring.project.model.Book;
import com.epam.rd.autocode.spring.project.repo.BookRepository;
import com.epam.rd.autocode.spring.project.service.BookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final ModelMapper modelMapper;

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAll(pageable);
        List<BookDTO> bookList = booksPage.stream().map(book -> modelMapper.map(book, BookDTO.class)).toList();
        return new PageImpl<>(bookList, pageable, booksPage.getTotalElements());
    }

    public Page<BookDTO> getAllBooksBySearch(String search, Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAllBySearch(search.toLowerCase(), pageable);
        List<BookDTO> bookList = booksPage.stream().map(book -> modelMapper.map(book, BookDTO.class)).toList();
        return new PageImpl<>(bookList, pageable, booksPage.getTotalElements());
    }

    @Override
    public BookDTO getBookByName(String name) {
        Book book = bookRepository.findByName(name).orElseThrow(() -> new NotFoundException("Book not found"));
        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public BookDTO updateBookByName(String name, BookDTO bookDTO) {
        Book book = bookRepository.findByName(name).orElseThrow(() -> new NotFoundException("Book not found"));

        modelMapper.map(bookDTO, book);
        book = bookRepository.save(book);

        return modelMapper.map(book, BookDTO.class);
    }

    @Override
    @Transactional
    public void deleteBookByName(String name) {
        bookRepository.deleteByName(name);
    }

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        Book book = new Book();

        modelMapper.map(bookDTO, book);
        book = bookRepository.save(book);

        return modelMapper.map(book, BookDTO.class);
    }
}
