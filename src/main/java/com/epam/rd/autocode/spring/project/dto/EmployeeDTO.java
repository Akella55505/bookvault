package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class EmployeeDTO extends UserDTO {
    private String phone;
    private LocalDate birthDate;
}
