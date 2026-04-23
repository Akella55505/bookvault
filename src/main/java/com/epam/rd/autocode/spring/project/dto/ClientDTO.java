package com.epam.rd.autocode.spring.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO extends UserDTO {
    private BigDecimal balance;
    private Boolean active;
}
