package com.moviemate.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private LocalDateTime timestamp;
    private Map<String, String> details;

    public ErrorResponse(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(String error, String message, int status, Map<String, String> details) {
        this(error, message, status);
        this.details = details;
    }
}