package com.epam.rd.autocode.spring.project.dto.filter;

import com.epam.rd.autocode.spring.project.dto.filter.enums.ClientSortByOptions;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientQueryFilter extends QueryFilter {
    private String search = "";
    private ClientSortByOptions sortBy = ClientSortByOptions.NAME;
}
