package com.offer.java.offer.exception;

import org.springframework.http.HttpStatus;

public class ScriptRunException extends ApplicationException {

    public ScriptRunException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
