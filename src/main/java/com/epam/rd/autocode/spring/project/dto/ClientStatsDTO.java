package com.epam.rd.autocode.spring.project.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ClientStatsDTO {
    private long total;
    private long blocked;
}
