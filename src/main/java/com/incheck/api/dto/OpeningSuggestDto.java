package com.incheck.api.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.incheck.api.enums.TagInfo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@JsonNaming(value = PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OpeningSuggestDto {

    private String title;

    private String moves;

    private String representation;

    private String suggestTitle;

    private String suggestMoves;

    private OpeningSuggestDescriptionDto description;

}
