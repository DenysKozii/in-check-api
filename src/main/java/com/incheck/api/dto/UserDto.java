package com.incheck.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.incheck.api.enums.TagInfo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserDto {

    private String username;

    private Integer wins = 0;

    private Integer loses = 0;

    private Integer draws = 0;

    private Double winRate;

    private List<OpeningSuggestDto> openings = new ArrayList<>();

    private List<TagInfo> tags = new ArrayList<>();

}
