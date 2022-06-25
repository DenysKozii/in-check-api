package com.incheck.api.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;


@Data
@RequiredArgsConstructor
public class AccuracyDto {

    private Double white;

    private Double black;

}
