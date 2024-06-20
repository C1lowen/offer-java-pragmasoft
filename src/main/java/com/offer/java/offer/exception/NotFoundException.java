package com.offer.java.offer.exception;

import com.offer.java.offer.dto.ScriptResponse;
import org.springframework.http.HttpStatus;

public class NotFoundException extends ApplicationException{



    public NotFoundException( ScriptResponse scriptResponse) {
        super( HttpStatus.NOT_FOUND, scriptResponse);
    }
}
