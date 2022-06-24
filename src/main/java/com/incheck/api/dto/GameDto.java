package com.incheck.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class GameDto {

    private String pgn;

    private boolean rated;

    private String endTime;

    private PlayerDto white;

    private PlayerDto black;

    private boolean won;

}
