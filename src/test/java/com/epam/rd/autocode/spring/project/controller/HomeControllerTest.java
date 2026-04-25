package com.epam.rd.autocode.spring.project.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("HomeController")
public class HomeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /")
    class Home {
        @Test
        @DisplayName("returns index view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("index"));
        }
    }
}
