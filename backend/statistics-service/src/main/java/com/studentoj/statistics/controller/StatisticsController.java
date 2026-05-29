package com.studentoj.statistics.controller;

import com.studentoj.statistics.dto.ActivityResponse;
import com.studentoj.statistics.dto.OverviewResponse;
import com.studentoj.statistics.dto.StudentTodaySolvedResponse;
import com.studentoj.statistics.service.StatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/me/overview")
    public OverviewResponse myOverview(HttpServletRequest request) {
        return statisticsService.studentOverview(extractUserId(request));
    }

    @GetMapping("/me/activity")
    public List<ActivityResponse> myActivity(HttpServletRequest request) {
        return statisticsService.activityForUser(extractUserId(request));
    }

    @GetMapping("/teacher/overview")
    public OverviewResponse teacherOverview(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.teacherOverview(groupName, studentId);
    }

    @GetMapping("/teacher/activity")
    public List<ActivityResponse> teacherActivity(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.teacherActivity(groupName, studentId);
    }

    @GetMapping("/teacher/today-solved")
    public List<StudentTodaySolvedResponse> teacherTodaySolved(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.studentTodaySolved(groupName, studentId);
    }

    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("X-User-Id");
        if (header == null || header.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(header.trim());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
