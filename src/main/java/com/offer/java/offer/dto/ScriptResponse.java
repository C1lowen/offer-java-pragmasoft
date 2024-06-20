package com.offer.java.offer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScriptResponse {
    private StatusOperation statusOperation;
    private String message;
    private String id;

    public ScriptResponse(StatusOperation statusOperation, String message, String id) {
        this.statusOperation = statusOperation;
        this.message = message;
        this.id = id;
    }
}
