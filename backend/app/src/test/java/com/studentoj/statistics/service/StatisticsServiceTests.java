package com.studentoj.statistics.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.studentoj.statistics.dto.OverviewResponse;
import com.studentoj.statistics.mapper.StatisticsMapper;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StatisticsServiceTests {
    @Test
    void studentOverviewUsesExplicitSubmissionSolvedAndStreakMetrics() {
        StatisticsMapper mapper = mock(StatisticsMapper.class);
        when(mapper.countSubmissionsByUser(7L)).thenReturn(6);
        when(mapper.countAcceptedByUser(7L)).thenReturn(3);
        when(mapper.countSolvedByUser(7L)).thenReturn(2);
        when(mapper.countActiveDaysByUser(7L)).thenReturn(5);
        when(mapper.countProblems()).thenReturn(20);
        when(mapper.countTodaySolvedByUser(7L)).thenReturn(1);
        when(mapper.countTodayAttemptedByUser(7L)).thenReturn(2);
        when(mapper.countTodayPassedByUser(7L)).thenReturn(1);
        when(mapper.countTodaySubmissionsByUser(7L)).thenReturn(4);
        when(mapper.selectActivityByUser(7L)).thenReturn(List.of(
                Map.of("date", Date.valueOf(LocalDate.now().minusDays(1)), "count", 1),
                Map.of("date", Date.valueOf(LocalDate.now()), "count", 2)));

        OverviewResponse result = new StatisticsService(mapper).studentOverview(7L);

        assertEquals(4, result.todaySubmissions());
        assertEquals(2, result.acceptedProblems());
        assertEquals(5, result.activeDays());
        assertEquals(2, result.streakDays());
        assertEquals(50, result.passRate());
    }
}
