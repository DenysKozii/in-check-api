package com.incheck.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserDto {

    private Double winRate;

    private List<String> openings = new ArrayList<>();

    private TagsDto tags;

}
