package com.studentoj.statistics.dto;

public record StudentTodaySolvedResponse(
        Long userId,
        String studentNo,
        String realName,
        String groupName,
        Integer todaySolved) {
}
