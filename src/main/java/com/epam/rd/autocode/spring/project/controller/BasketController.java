package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.model.User;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequiredArgsConstructor
@RequestMapping("/basket")
public class BasketController {
    private final ClientServiceImpl clientService;

    @PostMapping("/add")
    public String addBookToBasket(@RequestParam(required = false) String returnUrl,
                                  @AuthenticationPrincipal User user,
                                  @RequestParam String bookName) {
        clientService.addBookToBasket(user.getId(), bookName);
        return "redirect:" + (returnUrl == null ? "/" : returnUrl);
    }

    @PostMapping("/remove")
    public String removeBookFromBasket(@RequestParam String bookName,
                                       @AuthenticationPrincipal User user) {
        clientService.removeBookFromBasket(user.getId(), bookName);
        return "redirect:/client/basket";
    }

    @PostMapping("/clear")
    public ModelAndView clearBasket(@AuthenticationPrincipal User user) {
        ModelAndView mav = new ModelAndView("redirect:/client/basket");
        clientService.clearBasket(user.getId());
        return mav;
    }
}
