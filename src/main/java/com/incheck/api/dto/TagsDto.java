package com.incheck.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;


@Data
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TagsDto {

    private Boolean highWinRate;

    private Boolean lowWinRate;

    private Boolean goodMood;

    private Boolean badMood;

    private Boolean swift;

    private Boolean undervalued;

    private Boolean overvalued;

    private Boolean neverSurrender;

    private Boolean surrenderer;

}
