package com.offer.java.offer.dto;

import lombok.Data;

@Data
public class ScriptInfoResponse {
    private String id;
    private String script;
    private ScriptResultDTO result;
    private long startTime;
    private long duration;
}
