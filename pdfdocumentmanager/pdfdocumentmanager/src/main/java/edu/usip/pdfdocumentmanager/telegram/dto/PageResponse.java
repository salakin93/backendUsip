package edu.usip.pdfdocumentmanager.telegram.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class PageResponse<T> {

    private List<T> content;
    private int totalPages;
    private long totalElements;
    private int number;
    private int size;

}
