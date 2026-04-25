package com.epam.rd.autocode.spring.project.dto.filter;

import lombok.*;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public abstract class QueryFilter {
    private int page = 0;
    private int itemsPerPage = 5;
    private Sort.Direction sortDirection = Sort.Direction.ASC;
}
