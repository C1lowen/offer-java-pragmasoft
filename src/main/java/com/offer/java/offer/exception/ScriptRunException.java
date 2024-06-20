package com.offer.java.offer.exception;

import com.offer.java.offer.dto.ScriptResponse;
import org.springframework.http.HttpStatus;

public class ScriptRunException extends ApplicationException {

    public ScriptRunException(ScriptResponse scriptResponse) {
        super(HttpStatus.BAD_REQUEST, scriptResponse);
    }
}
