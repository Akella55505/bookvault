package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.BookItemDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderServiceImpl orderService;

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping("/{id}")
    public ModelAndView getOrderDetails(@PathVariable Long id) {
        ModelAndView mav = new ModelAndView("orders/detail");
        OrderDTO order = orderService.getOrderById(id);

        mav.addObject("order", order);

        return mav;
    }

    @PreAuthorize("hasRole('CLIENT')")
    @PostMapping("/place")
    public ModelAndView placeOrder(@RequestParam String basketJson,
                                   @AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("redirect:/client/orders");
        ObjectMapper mapper = new ObjectMapper();

        try {
            List<BookItemDTO> bookItems = mapper.readValue(basketJson, new TypeReference<>() {});
            orderService.addOrder(bookItems, user.getId());
        } catch (Exception e) {
            mav = new ModelAndView("redirect:/client/basket");
            mav.addObject("errorMessage", e.getMessage());
            return mav;
        }

        return mav;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/confirm")
    public String confirmOrder(@RequestParam Long id,
                               @RequestParam(defaultValue = "") String queryString,
                               @AuthenticationPrincipal User user) {
        orderService.confirmOrderById(id, user.getId());
        return "redirect:/employee/orders" + (queryString.isBlank() ?  "" : "?" + queryString);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping("/delete")
    public String confirmOrder(@RequestParam Long id,
                               @RequestParam(defaultValue = "") String queryString) {
        orderService.deleteOrderById(id);
        return "redirect:/employee/orders" + (queryString.isBlank() ?  "" : "?" + queryString);
    }
}
