package com.epam.rd.autocode.spring.project.controller;

import jakarta.servlet.RequestDispatcher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomErrorController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("CustomErrorController")
public class CustomErrorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /error")
    class Error {
        @Test
        @DisplayName("returns error view")
        void returnsView() throws Exception {
            mockMvc
                    .perform(get("/error"))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error"));
        }

        @Test
        @DisplayName("returns error view with status provided")
        void returnsViewWithStatus() throws Exception {
            mockMvc
                    .perform(get("/error").requestAttr(RequestDispatcher.ERROR_STATUS_CODE, 404))
                    .andExpect(status().isOk())
                    .andExpect(view().name("error"))
                    .andExpect(model().attribute("status", 404));
        }
    }
}
