package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientDTO extends UserDTO {
    private BigDecimal balance;
    private Boolean active;
}
