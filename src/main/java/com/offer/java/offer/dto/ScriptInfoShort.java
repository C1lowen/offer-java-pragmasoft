package com.offer.java.offer.dto;

import lombok.Data;

@Data
public class ScriptInfoShort {
    private String id;
    private String script;
    private ScriptResultDTO result;
    private long duration;
}
