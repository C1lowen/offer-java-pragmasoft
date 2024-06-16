package com.offer.java.offer.dto;

import lombok.Data;

@Data

public class ScriptResult {
    private String output;
    private String error;
    private Status status;


    public ScriptResult() {
        this.status = Status.PROCESSING;
    }

    public ScriptResult(String error) {
        this.error = error;
        this.status = Status.PROCESSING;
    }
}
