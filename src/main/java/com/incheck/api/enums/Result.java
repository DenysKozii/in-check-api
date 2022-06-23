package com.incheck.api.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Result {
    WIN("win"), RESIGNED("resigned"), TIMEOUT("timeout");

    private final String result;

    public String getResult() {
        return result;
    }
}
