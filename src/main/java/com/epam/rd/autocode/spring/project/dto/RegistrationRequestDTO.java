package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.service.PasswordService;
import com.epam.rd.autocode.spring.project.validation.UniqueEmail;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationRequestDTO {
    @NotBlank(message = "{user.validation.email.blank}")
    @Email(message = "{user.validation.email.format}")
    @UniqueEmail
    private String email;
    @NotBlank(message = "{client.validation.password.blank}")
    @Size(min = 8, message = "{client.validation.password.size}")
    @Pattern(
            regexp = PasswordService.PASSWORD_REGEX,
            message = "{auth.register.fail.password}"
    )
    private String password;
    private String name;
}
