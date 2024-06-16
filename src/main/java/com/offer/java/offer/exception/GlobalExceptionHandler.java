package com.offer.java.offer.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


@ControllerAdvice
@AllArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<?> handlePaymentException(ApplicationException ex) {
        return new ResponseEntity<>(ErrorResponse.builder().errorMessage(ex.getMessage()).build(), ex.getHttpStatus());
    }

}

