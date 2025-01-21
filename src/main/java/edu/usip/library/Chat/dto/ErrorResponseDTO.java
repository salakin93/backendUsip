package edu.usip.library.Chat.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponseDTO {
    private String type; // type of error
    private String title; // title of error
    private Integer status; // status of error
    private String detail; // detail of error
    private Object message; // message of error
    private LocalDateTime timestamp; // timestamp of error
    private String error; // error
}