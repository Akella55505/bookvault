package com.epam.rd.autocode.spring.project.controller;

import com.epam.rd.autocode.spring.project.dto.ClientDTO;
import com.epam.rd.autocode.spring.project.dto.ClientStatsDTO;
import com.epam.rd.autocode.spring.project.dto.OrderDTO;
import com.epam.rd.autocode.spring.project.dto.filter.ClientQueryFilter;
import com.epam.rd.autocode.spring.project.dto.filter.EmployeeOrderQueryFilter;
import com.epam.rd.autocode.spring.project.service.impl.ClientServiceImpl;
import com.epam.rd.autocode.spring.project.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final ClientServiceImpl clientService;
    private final OrderServiceImpl orderService;

    @GetMapping("/clients")
    public ModelAndView clientsPage(ClientQueryFilter queryFilter) {
        ModelAndView mav = new ModelAndView("employee/clients");
        Sort sort = Sort.by(queryFilter.getSortDirection(), queryFilter.getSortBy().name().toLowerCase());

        ClientStatsDTO clientStats = clientService.getClientStats();
        Page<ClientDTO> pageResponse = clientService.getAllClientsBySearch(queryFilter.getSearch(),
                PageRequest.of(queryFilter.getPage(), queryFilter.getItemsPerPage(), sort));

        mav.addObject("queryFilter", queryFilter);
        mav.addObject("clients", pageResponse);
        mav.addObject("stats", clientStats);

        return mav;
    }

    @PostMapping("/clients/toggle")
    public String toggleClient(@RequestParam String email,
                               @RequestParam(defaultValue = "") String queryString) {
        clientService.toggleClientByEmail(email);

        log.info("Client {} has been toggled", email);

        return "redirect:/employee/clients" + (queryString.isBlank() ?  "" : "?" + queryString);
    }

    @GetMapping("/orders")
    public ModelAndView ordersPage(EmployeeOrderQueryFilter queryFilter) {
        ModelAndView mav = new ModelAndView("employee/orders");
        Sort sort = Sort.by(queryFilter.getSortDirection(), "orderDate");

        Page<OrderDTO> orders = orderService.getAllOrdersBySearchAndConfirmed(queryFilter.getSearch(),
                PageRequest.of(queryFilter.getPage(), queryFilter.getItemsPerPage(), sort),
                queryFilter.isConfirmed());

        mav.addObject("orders", orders);
        mav.addObject("queryFilter", queryFilter);

        return mav;
    }
}
