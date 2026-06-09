package com.studentoj.statistics.controller;

import com.studentoj.common.auth.AuthContext;
import com.studentoj.common.auth.RequireRole;
import com.studentoj.common.auth.Role;
import com.studentoj.statistics.dto.ActivityResponse;
import com.studentoj.statistics.dto.OverviewResponse;
import com.studentoj.statistics.dto.StudentTodaySolvedResponse;
import com.studentoj.statistics.service.StatisticsService;
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
    @RequireRole(Role.STUDENT)
    public OverviewResponse myOverview() {
        return statisticsService.studentOverview(AuthContext.userId());
    }

    @GetMapping("/me/activity")
    @RequireRole(Role.STUDENT)
    public List<ActivityResponse> myActivity() {
        return statisticsService.activityForUser(AuthContext.userId());
    }

    @GetMapping("/teacher/overview")
    @RequireRole(Role.TEACHER)
    public OverviewResponse teacherOverview(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.teacherOverview(groupName, studentId);
    }

    @GetMapping("/teacher/activity")
    @RequireRole(Role.TEACHER)
    public List<ActivityResponse> teacherActivity(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.teacherActivity(groupName, studentId);
    }

    @GetMapping("/teacher/today-solved")
    @RequireRole(Role.TEACHER)
    public List<StudentTodaySolvedResponse> teacherTodaySolved(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "studentId", required = false) Long studentId) {
        return statisticsService.studentTodaySolved(groupName, studentId);
    }

}
