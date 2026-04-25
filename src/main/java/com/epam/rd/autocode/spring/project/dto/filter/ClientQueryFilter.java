package com.epam.rd.autocode.spring.project.dto.filter;

import com.epam.rd.autocode.spring.project.dto.filter.enums.ClientSortByOptions;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode(callSuper = true)
public class ClientQueryFilter extends QueryFilter {
    private String search = "";
    private ClientSortByOptions sortBy = ClientSortByOptions.NAME;
}
