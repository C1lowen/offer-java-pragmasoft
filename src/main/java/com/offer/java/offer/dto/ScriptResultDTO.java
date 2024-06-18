package com.offer.java.offer.dto;

import lombok.Data;

@Data
public class ScriptResultDTO {
    private String output;
    private String error;
    private Status status;

    public ScriptResultDTO() {
        this.status = Status.QUEUE;
    }
}
