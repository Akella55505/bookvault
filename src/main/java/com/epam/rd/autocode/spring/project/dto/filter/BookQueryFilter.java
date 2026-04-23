package com.epam.rd.autocode.spring.project.dto.filter;

import com.epam.rd.autocode.spring.project.dto.filter.enums.BookSortByOptions;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BookQueryFilter extends QueryFilter {
    private String search = "";
    private BookSortByOptions sortBy = BookSortByOptions.NAME;
}
