package com.incheck.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class GameDto {

    private String pgn;

    private PlayerDto white;

    private PlayerDto black;

    private boolean won;

}
