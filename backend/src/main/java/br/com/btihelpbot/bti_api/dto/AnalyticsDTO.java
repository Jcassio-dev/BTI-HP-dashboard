package br.com.btihelpbot.bti_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record AnalyticsDTO(
        List<OverTimePoint> overTime,
        List<HourPoint> byHour,
        ChatTypeCounts chatType
) {
    public record OverTimePoint(String date, long commands, long users) {}

    public record HourPoint(int hour, long count) {}

    public record ChatTypeCounts(long group, @JsonProperty("private") long privateChats) {}
}
