package com.epam.rd.autocode.spring.project.service;

import com.epam.rd.autocode.spring.project.dto.filter.QueryFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class PaginationHelperService {
    public String buildUrl(String baseUrl, QueryFilter queryFilter, int page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath(baseUrl);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> queryMap = mapper.convertValue(queryFilter, Map.class);

        if (queryFilter != null) {
            queryMap.forEach((key, value) -> {
                if (value != null) {
                    if (value instanceof String) value = ((String) value).toLowerCase();

                    if (key.equals("page")) builder.queryParam(key, page);
                    else builder.queryParam(key, value);
                }
            });
        }

        return builder.toUriString();
    }
}
