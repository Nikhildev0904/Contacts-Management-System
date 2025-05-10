package com.cognitree.internship.cms.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int pageSize;
    private int number;
    private int numberOfElements;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PagedResponse<T> fromPage(Page<T> page) {
        PagedResponse<T> response = new PagedResponse<>();
        response.setContent(page.getContent());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setPageSize(page.getSize());
        response.setNumber(page.getNumber());
        response.setNumberOfElements(page.getNumberOfElements());
        response.setFirst(page.isFirst());
        response.setLast(page.isLast());
        response.setEmpty(page.isEmpty());
        return response;
    }
}