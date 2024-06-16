package com.offer.java.offer.dto;

import lombok.Data;

@Data
public class ScriptModel {
    private String script;
    private SortedByTime sortedTime;
    private SortedByStatus sortedStatus;
}
