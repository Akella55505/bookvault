package com.epam.rd.autocode.spring.project.dto.filter;

import lombok.*;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ClientOrderQueryFilter extends QueryFilter {
    private Sort.Direction sortDirection = Sort.Direction.DESC;
}
