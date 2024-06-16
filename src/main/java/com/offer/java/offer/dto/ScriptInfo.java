package com.offer.java.offer.dto;

import lombok.Data;

import java.util.concurrent.Future;

@Data
public class ScriptInfo {
    private String id;
    private String script;
    private ScriptResult result;
    private long startTime;
    private long duration;
    private Future<?> future;

    public ScriptInfo(String id, String script) {
        this.id = id;
        this.script = script;
        this.result = new ScriptResult();
        this.startTime = System.currentTimeMillis();
    }
}
