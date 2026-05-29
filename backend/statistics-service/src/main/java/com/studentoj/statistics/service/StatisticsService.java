package com.studentoj.statistics.service;

import com.studentoj.statistics.dto.ActivityResponse;
import com.studentoj.statistics.dto.OverviewResponse;
import com.studentoj.statistics.dto.StudentTodaySolvedResponse;
import com.studentoj.statistics.mapper.StatisticsMapper;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;

    public StatisticsService(StatisticsMapper statisticsMapper) {
        this.statisticsMapper = statisticsMapper;
    }

    public OverviewResponse overview() {
        int students = safeCount(() -> statisticsMapper.countStudents());
        int problems = safeCount(() -> statisticsMapper.countProblems());
        int subs = safeCount(() -> statisticsMapper.countSubmissions());
        int accepted = safeCount(() -> statisticsMapper.countAccepted());
        int days = safeCount(() -> statisticsMapper.countActiveDays());
        int passRate = subs == 0 ? 0 : (int) Math.round(accepted * 100.0 / subs);
        int todaySolved = safeCount(() -> statisticsMapper.countTodaySolved());
        int todayAttempted = safeCount(() -> statisticsMapper.countTodayAttempted());
        int todayPassed = safeCount(() -> statisticsMapper.countTodayPassed());
        return new OverviewResponse(students, problems, subs, passRate, days, todaySolved, todayAttempted, todayPassed);
    }

    public OverviewResponse teacherOverview(String groupName, Long studentId) {
        if (studentId != null && studentId > 0) {
            int subs = safeCount(() -> statisticsMapper.countSubmissionsByUser(studentId));
            int accepted = safeCount(() -> statisticsMapper.countAcceptedByUser(studentId));
            int days = safeCount(() -> statisticsMapper.countActiveDaysByUser(studentId));
            int passRate = subs == 0 ? 0 : (int) Math.round(accepted * 100.0 / subs);
            int todaySolved = safeCount(() -> statisticsMapper.countTodaySolvedByUser(studentId));
            int todayAttempted = safeCount(() -> statisticsMapper.countTodayAttemptedByUser(studentId));
            int todayPassed = safeCount(() -> statisticsMapper.countTodayPassedByUser(studentId));
            return new OverviewResponse(1, safeCount(statisticsMapper::countProblems), subs, passRate, days, todaySolved, todayAttempted, todayPassed);
        }
        if (groupName != null && !groupName.isBlank()) {
            String normalizedGroupName = groupName.trim();
            int students = safeCount(() -> statisticsMapper.countStudentsByGroupName(normalizedGroupName));
            int subs = safeCount(() -> statisticsMapper.countSubmissionsByGroupName(normalizedGroupName));
            int accepted = safeCount(() -> statisticsMapper.countAcceptedByGroupName(normalizedGroupName));
            int days = safeCount(() -> statisticsMapper.countActiveDaysByGroupName(normalizedGroupName));
            int passRate = subs == 0 ? 0 : (int) Math.round(accepted * 100.0 / subs);
            int todaySolved = safeCount(() -> statisticsMapper.countTodaySolvedByGroupName(normalizedGroupName));
            int todayAttempted = safeCount(() -> statisticsMapper.countTodayAttemptedByGroupName(normalizedGroupName));
            int todayPassed = safeCount(() -> statisticsMapper.countTodayPassedByGroupName(normalizedGroupName));
            return new OverviewResponse(students, safeCount(statisticsMapper::countProblems), subs, passRate, days, todaySolved, todayAttempted, todayPassed);
        }
        return overview();
    }

    public OverviewResponse studentOverview(Long userId) {
        if (userId == null || userId <= 0) {
            return overview();
        }
        int subs = safeCount(() -> statisticsMapper.countSubmissionsByUser(userId));
        int accepted = safeCount(() -> statisticsMapper.countAcceptedByUser(userId));
        int solved = safeCount(() -> statisticsMapper.countSolvedByUser(userId));
        int days = safeCount(() -> statisticsMapper.countActiveDaysByUser(userId));
        int passRate = subs == 0 ? 0 : (int) Math.round(accepted * 100.0 / subs);
        int todaySolved = safeCount(() -> statisticsMapper.countTodaySolvedByUser(userId));
        int todayAttempted = safeCount(() -> statisticsMapper.countTodayAttemptedByUser(userId));
        int todayPassed = safeCount(() -> statisticsMapper.countTodayPassedByUser(userId));
        return new OverviewResponse(solved, safeCount(statisticsMapper::countProblems), subs, passRate, days, todaySolved, todayAttempted, todayPassed);
    }

    public List<ActivityResponse> activity() {
        List<Map<String, Object>> rows;
        try {
            rows = statisticsMapper.selectActivityAggregate();
        } catch (Exception e) {
            rows = List.of();
        }
        return rows.stream().map(this::toActivity).toList();
    }

    public List<ActivityResponse> teacherActivity(String groupName, Long studentId) {
        if (studentId != null && studentId > 0) {
            return activityForUser(studentId);
        }
        if (groupName != null && !groupName.isBlank()) {
            List<Map<String, Object>> rows;
            try {
                rows = statisticsMapper.selectActivityByGroupName(groupName.trim());
            } catch (Exception e) {
                rows = List.of();
            }
            return rows.stream().map(this::toActivity).toList();
        }
        return activity();
    }

    public List<StudentTodaySolvedResponse> studentTodaySolved(String groupName, Long studentId) {
        String normalizedGroupName = groupName == null || groupName.isBlank() ? null : groupName.trim();
        Long normalizedStudentId = studentId == null || studentId <= 0 ? null : studentId;
        List<Map<String, Object>> rows;
        try {
            rows = statisticsMapper.selectStudentTodaySolved(normalizedGroupName, normalizedStudentId);
        } catch (Exception e) {
            rows = List.of();
        }
        return rows.stream().map(this::toStudentTodaySolved).toList();
    }

    public List<ActivityResponse> activityForUser(Long userId) {
        if (userId == null || userId <= 0) {
            return activity();
        }
        List<Map<String, Object>> rows;
        try {
            rows = statisticsMapper.selectActivityByUser(userId);
        } catch (Exception e) {
            rows = List.of();
        }
        return rows.stream().map(this::toActivity).toList();
    }

    private ActivityResponse toActivity(Map<String, Object> row) {
        Object dateObj = row.get("date");
        LocalDate date;
        if (dateObj instanceof LocalDate ld) {
            date = ld;
        } else if (dateObj instanceof Date d) {
            date = d.toLocalDate();
        } else {
            date = LocalDate.now();
        }
        Object countObj = row.get("count");
        int count = countObj == null ? 0 : ((Number) countObj).intValue();
        return new ActivityResponse(date, count);
    }

    private StudentTodaySolvedResponse toStudentTodaySolved(Map<String, Object> row) {
        return new StudentTodaySolvedResponse(
                row.get("userId") == null ? null : ((Number) row.get("userId")).longValue(),
                (String) row.get("studentNo"),
                (String) row.get("realName"),
                (String) row.get("groupName"),
                row.get("todaySolved") == null ? 0 : ((Number) row.get("todaySolved")).intValue()
        );
    }

    private int safeCount(java.util.function.Supplier<Integer> supplier) {
        try {
            Integer v = supplier.get();
            return v == null ? 0 : v;
        } catch (Exception e) {
            return 0;
        }
    }
}
