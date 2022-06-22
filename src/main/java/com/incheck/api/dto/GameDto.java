package com.incheck.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Data
@RequiredArgsConstructor
public class GameDto {

    private String id;

    private boolean won;

    private List<MoveDto> moves = new ArrayList<>();
}
