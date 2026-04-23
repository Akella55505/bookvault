package com.epam.rd.autocode.spring.project.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeOrderQueryFilter extends QueryFilter {
    private String search = "";
    private Sort.Direction sortDirection = Sort.Direction.DESC;
    private boolean confirmed = false;
}

