package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.filter.BookQueryFilter;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.impl.BookServiceImpl;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {
    private final BookServiceImpl bookService;
    private final ClientServiceImpl clientService;

    @GetMapping
    public ModelAndView getAllBooks(BookQueryFilter queryFilter,
                                    @AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("books/list");
        Sort sort = Sort.by(queryFilter.getSortDirection(), queryFilter.getSortBy().name().toLowerCase());
        Page<BookDTO> pageResponse;

        pageResponse = bookService.getAllBooksBySearch(queryFilter.getSearch(),
                PageRequest.of(queryFilter.getPage(), queryFilter.getItemsPerPage(), sort));

        mav.addObject("queryFilter", queryFilter);
        mav.addObject("books", pageResponse);
        if (user != null) mav.addObject("basket", clientService.getBasketItemsById(user.getId()));
        return mav;
    }

    @GetMapping("/{name}")
    public ModelAndView getBookDetails(@PathVariable String name,
                                       @AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("books/detail");

        BookDTO book = bookService.getBookByName(name);
        mav.addObject("book", book);
        if (user != null) mav.addObject("basket", clientService.getBasketItemsById(user.getId()));
        return mav;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/add")
    public String addBookPage(@ModelAttribute BookDTO bookDTO) {
        return "books/add";
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/add")
    public String addBook(@Valid BookDTO bookDTO, BindingResult bindingResult) {
        if (bindingResult.getFieldError() != null) {
            return "redirect:/books/add";
        }

        bookService.addBook(bookDTO);

        return "redirect:/books/" + bookDTO.getName();
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/edit/{name}")
    public ModelAndView editBookPage(@PathVariable String name, @ModelAttribute BookDTO bookDTO) {
        ModelAndView mav = new ModelAndView("books/edit");

        mav.addObject("bookDTO", bookService.getBookByName(name));

        return mav;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/edit/{name}")
    public String editBook(@PathVariable String name, @Valid BookDTO bookDTO, BindingResult bindingResult) {
        if (bindingResult.getFieldError() != null) {
            return "redirect:/books/edit/" + name;
        }

        bookService.updateBookByName(name, bookDTO);

        return "redirect:/books/" + name;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/delete/{name}")
    public String deleteBook(@PathVariable String name,
                             @RequestParam(defaultValue = "") String queryString) {
        bookService.deleteBookByName(name);

        return "redirect:/books" + (queryString.isBlank() ?  "" : "?" + queryString);
    }
}
