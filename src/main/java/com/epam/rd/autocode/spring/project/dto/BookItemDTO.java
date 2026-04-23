package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookItemDTO {
    private String bookName;
    private Integer quantity;
}
