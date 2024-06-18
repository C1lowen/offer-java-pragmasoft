package com.offer.java.offer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageException {
    private String id;
    private String message;
}
