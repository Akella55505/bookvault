package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class OrderDTO {
    private Long id;
    private String clientEmail;
    private String employeeEmail;
    private LocalDateTime orderDate;
    private BigDecimal price;
    private List<BookItemDTO> bookItems;
}
