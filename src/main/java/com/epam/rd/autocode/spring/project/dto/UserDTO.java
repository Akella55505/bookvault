package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDTO {
    @NotBlank(message = "{user.validation.email.blank}")
    @Email(message = "{user.validation.email.format}")
    @UniqueEmail
    private String email;
    private String name;
}
