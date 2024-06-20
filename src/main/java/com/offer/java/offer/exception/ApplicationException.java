package com.offer.java.offer.exception;

import com.offer.java.offer.dto.ScriptResponse;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class ApplicationException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final ScriptResponse scriptResponse;

    public ApplicationException(HttpStatus httpStatus, ScriptResponse scriptResponse) {
        this.httpStatus = httpStatus;
        this.scriptResponse = scriptResponse;
    }
}