package com.incheck.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GameDto {

    private String pgn;

    private boolean rated;

    private Long endTime;

    private PlayerDto white;

    private PlayerDto black;

    private boolean won;

    private AccuracyDto accuracies;

}
