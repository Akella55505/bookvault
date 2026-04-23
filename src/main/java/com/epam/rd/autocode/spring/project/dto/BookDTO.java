package com.epam.rd.autocode.spring.project.dto;

import com.epam.rd.autocode.spring.project.model.enums.AgeGroup;
import com.epam.rd.autocode.spring.project.model.enums.Language;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class BookDTO {
    @NotBlank(message = "{book.validation.name.blank}")
    private String name;
    private String genre;
    private AgeGroup ageGroup;
    @NotNull(message = "{book.validation.price.null}")
    @DecimalMin(value = "0.01", message = "{book.validation.price.min}")
    @Digits(integer = 8, fraction = 2, message = "{book.validation.price.digits}")
    private BigDecimal price;
    @PastOrPresent(message = "{book.validation.date.future}")
    private LocalDate publicationDate;
    @NotBlank(message = "{book.validation.author.blank}")
    private String author;
    @Positive(message = "{book.validation.pages.positive}")
    private Integer pages;
    @Size(max = 550, message = "{book.validation.characteristics.size}")
    private String characteristics;
    @Size(max = 5050, message = "{book.validation.description.size}")
    private String description;
    private Language language;
}
