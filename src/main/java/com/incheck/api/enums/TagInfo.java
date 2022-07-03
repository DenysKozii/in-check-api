package com.incheck.api.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@AllArgsConstructor
public enum TagInfo {

    HIGH_WIN_RATE("High Win Rate", "Some description", TagType.GOOD),
    NEVER_SURRENDER("Never Surrender", "Some description", TagType.GOOD),
    GOOD_MOOD("Good Mood", "Some description", TagType.GOOD),
    EXECUTIONER("Executioner", "Some description", TagType.GOOD),
    HIGH_ACCURACY("High Accuracy", "Some description", TagType.GOOD),
    LOW_WIN_RATE("Low Win Rate", "Some description", TagType.BAD),
    SURRENDERER("Surrenderer", "Some description", TagType.BAD),
    BAD_MOOD("Bad Mood", "Some description", TagType.BAD),
    INACTIVE("Inactive", "Some description", TagType.BAD),
    UNWARMED("Unwarmed", "Some description", TagType.NEUTRAL),
    UNDERVALUED("Undervalued", "Some description", TagType.NEUTRAL),
    OVERVALUED("Overvalued", "Some description", TagType.NEUTRAL),
    BUSTER("Buster", "Some description", TagType.NEUTRAL),
    SWIFT("Swift", "Some description", TagType.NEUTRAL);

    private final String  title;
    private final String  description;
    private final TagType type;


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TagType getType() {
        return type;
    }

}