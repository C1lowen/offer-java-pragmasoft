package com.offer.java.offer.dto;

import lombok.Data;

@Data

public class ScriptResult {
    private String id;
    private String output;
    private String error;
    private Status status;


    public ScriptResult() {
        this.status = Status.QUEUE;
    }

    public ScriptResult(String id) {
        this.id = id;
        this.status = Status.QUEUE;
    }
}
