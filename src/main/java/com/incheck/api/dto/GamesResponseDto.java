package com.incheck.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@RequiredArgsConstructor
public class GamesResponseDto {

    private List<GameDto> games = new ArrayList<>();

}
