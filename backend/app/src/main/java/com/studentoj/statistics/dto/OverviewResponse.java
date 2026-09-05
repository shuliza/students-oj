package com.studentoj.statistics.dto;

public record OverviewResponse(
        Integer students,
        Integer problems,
        Integer submissions,
        Integer passRate,
        Integer activeDays,
        Integer todaySolved,
        Integer todayAttempted,
        Integer todayPassed,
        Integer todaySubmissions,
        Integer acceptedProblems,
        Integer streakDays) {
}
