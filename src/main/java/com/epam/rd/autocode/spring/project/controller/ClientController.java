package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.filter.ClientOrderQueryFilter;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.OrderService;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientController {
    private final ClientServiceImpl clientService;
    private final OrderService orderService;

    @GetMapping("/basket")
    public ModelAndView basketPage(@AuthenticationPrincipal User user,
                                   @RequestParam(required = false) String errorMessage) {
        ModelAndView mav = new ModelAndView("client/basket");

        Set<BookDTO> basketItems = clientService.getBasketItemsById(user.getId());
        mav.addObject("basketItems", basketItems);
        mav.addObject("total", basketItems.stream().map(BookDTO::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add));

        if (errorMessage != null) {
            mav.addObject("errorMessage", errorMessage);
        }

        return mav;
    }

    @GetMapping("/orders")
    public ModelAndView ordersPage(ClientOrderQueryFilter queryFilter,
                                   @AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("client/orders");
        Sort sort = Sort.by(queryFilter.getSortDirection(), "orderDate");

        Page<OrderDTO> pageResponse = orderService.getOrdersByClient(user.getEmail(),
                PageRequest.of(queryFilter.getPage(), queryFilter.getItemsPerPage(), sort));

        mav.addObject("queryFilter", queryFilter);
        mav.addObject("orders", pageResponse);
        return mav;
    }

    @PostMapping("/balance/deposit")
    public String deposit(@AuthenticationPrincipal User user, BigDecimal deposit) {
        clientService.depositById(user.getId(), deposit);

        log.info("User {} just made a deposit of amount {}", user.getEmail(), deposit);

        return "redirect:/profile";
    }

    @PostMapping("/balance/withdraw")
    public String withdraw(@AuthenticationPrincipal User user, BigDecimal deposit) {
        clientService.withdrawById(user.getId(), deposit);

        return "redirect:/client/orders";
    }

    @GetMapping("/delete")
    public String deleteUser(@AuthenticationPrincipal User user) {
        clientService.deleteClientByEmail(user.getEmail());
        return "redirect:/";
    }
}
