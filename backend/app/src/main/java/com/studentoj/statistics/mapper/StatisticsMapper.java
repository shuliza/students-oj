package com.studentoj.statistics.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StatisticsMapper {

    @Select("SELECT COUNT(*) FROM user WHERE role = 'STUDENT'")
    int countStudents();

    @Select("""
            SELECT COUNT(*)
            FROM user u
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE u.role = 'STUDENT' AND g.name = #{groupName}
            """)
    int countStudentsByGroupName(@Param("groupName") String groupName);

    @Select("SELECT COUNT(*) FROM problem WHERE status = 1")
    int countProblems();

    @Select("SELECT COUNT(*) FROM submission")
    int countSubmissions();

    @Select("SELECT COUNT(*) FROM submission WHERE status = 'ACCEPTED'")
    int countAccepted();

    @Select("""
            SELECT COUNT(*)
            FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                WHERE s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1
                      FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodaySolved();

    @Select("SELECT COUNT(*) FROM submission WHERE DATE(submitted_at) = CURRENT_DATE")
    int countTodaySubmissions();

    @Select("SELECT COUNT(DISTINCT activity_date) FROM student_activity")
    int countActiveDays();

    @Select("SELECT COUNT(*) FROM submission WHERE user_id = #{userId}")
    int countSubmissionsByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM submission WHERE user_id = #{userId} AND DATE(submitted_at) = CURRENT_DATE")
    int countTodaySubmissionsByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM submission s
            LEFT JOIN user u ON u.id = s.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE g.name = #{groupName}
            """)
    int countSubmissionsByGroupName(@Param("groupName") String groupName);

    @Select("""
            SELECT COUNT(*)
            FROM submission s
            LEFT JOIN user u ON u.id = s.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE g.name = #{groupName} AND DATE(s.submitted_at) = CURRENT_DATE
            """)
    int countTodaySubmissionsByGroupName(@Param("groupName") String groupName);

    @Select("SELECT COUNT(*) FROM submission WHERE user_id = #{userId} AND status = 'ACCEPTED'")
    int countAcceptedByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                WHERE s.user_id = #{userId}
                  AND s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1
                      FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodaySolvedByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*)
            FROM submission s
            LEFT JOIN user u ON u.id = s.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE s.status = 'ACCEPTED' AND g.name = #{groupName}
            """)
    int countAcceptedByGroupName(@Param("groupName") String groupName);

    @Select("""
            SELECT COUNT(*)
            FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                LEFT JOIN user u ON u.id = s.user_id
                LEFT JOIN class_group g ON g.id = u.group_id
                WHERE g.name = #{groupName}
                  AND s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1
                      FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodaySolvedByGroupName(@Param("groupName") String groupName);

    @Select("SELECT COUNT(DISTINCT problem_id) FROM submission WHERE user_id = #{userId} AND status = 'ACCEPTED'")
    int countSolvedByUser(@Param("userId") Long userId);

    @Select("SELECT COUNT(DISTINCT problem_id) FROM submission WHERE DATE(submitted_at) = CURRENT_DATE")
    int countTodayAttempted();

    @Select("SELECT COUNT(DISTINCT problem_id) FROM submission WHERE user_id = #{userId} AND DATE(submitted_at) = CURRENT_DATE")
    int countTodayAttemptedByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(DISTINCT problem_id) FROM submission s
            LEFT JOIN user u ON u.id = s.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE g.name = #{groupName} AND DATE(s.submitted_at) = CURRENT_DATE
            """)
    int countTodayAttemptedByGroupName(@Param("groupName") String groupName);

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                WHERE s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1 FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodayPassed();

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                WHERE s.user_id = #{userId}
                  AND s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1 FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodayPassedByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(*) FROM (
                SELECT DISTINCT s.user_id, s.problem_id
                FROM submission s
                LEFT JOIN user u ON u.id = s.user_id
                LEFT JOIN class_group g ON g.id = u.group_id
                WHERE g.name = #{groupName}
                  AND s.status = 'ACCEPTED'
                  AND DATE(s.submitted_at) = CURRENT_DATE
                  AND NOT EXISTS (
                      SELECT 1 FROM submission old
                      WHERE old.user_id = s.user_id
                        AND old.problem_id = s.problem_id
                        AND old.status = 'ACCEPTED'
                        AND DATE(old.submitted_at) < CURRENT_DATE
                  )
            ) today_new
            """)
    int countTodayPassedByGroupName(@Param("groupName") String groupName);

    @Select("SELECT COUNT(DISTINCT activity_date) FROM student_activity WHERE user_id = #{userId}")
    int countActiveDaysByUser(@Param("userId") Long userId);

    @Select("""
            SELECT COUNT(DISTINCT a.activity_date)
            FROM student_activity a
            LEFT JOIN user u ON u.id = a.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE g.name = #{groupName}
            """)
    int countActiveDaysByGroupName(@Param("groupName") String groupName);

    @Select("SELECT activity_date AS date, SUM(submission_count) AS count FROM student_activity " +
            "GROUP BY activity_date ORDER BY activity_date ASC")
    List<Map<String, Object>> selectActivityAggregate();

    @Select("SELECT activity_date AS date, submission_count AS count FROM student_activity " +
            "WHERE user_id = #{userId} " +
            "ORDER BY activity_date ASC")
    List<Map<String, Object>> selectActivityByUser(@Param("userId") Long userId);

    @Select("""
            SELECT a.activity_date AS date, SUM(a.submission_count) AS count
            FROM student_activity a
            LEFT JOIN user u ON u.id = a.user_id
            LEFT JOIN class_group g ON g.id = u.group_id
            WHERE g.name = #{groupName}
            GROUP BY a.activity_date
            ORDER BY a.activity_date ASC
            """)
    List<Map<String, Object>> selectActivityByGroupName(@Param("groupName") String groupName);

    @Select("""
            SELECT u.id AS userId,
                   u.student_no AS studentNo,
                   u.real_name AS realName,
                   g.name AS groupName,
                   COUNT(DISTINCT s.problem_id) AS todaySolved
            FROM user u
            LEFT JOIN class_group g ON g.id = u.group_id
            LEFT JOIN submission s
              ON s.user_id = u.id
             AND s.status = 'ACCEPTED'
             AND DATE(s.submitted_at) = CURRENT_DATE
             AND NOT EXISTS (
                 SELECT 1
                 FROM submission old
                 WHERE old.user_id = s.user_id
                   AND old.problem_id = s.problem_id
                   AND old.status = 'ACCEPTED'
                   AND DATE(old.submitted_at) < CURRENT_DATE
             )
            WHERE u.role = 'STUDENT'
              AND (#{groupName} IS NULL OR g.name = #{groupName})
              AND (#{studentId} IS NULL OR u.id = #{studentId})
            GROUP BY u.id, u.student_no, u.real_name, g.name
            ORDER BY g.id, u.student_no
            """)
    List<Map<String, Object>> selectStudentTodaySolved(
            @Param("groupName") String groupName,
            @Param("studentId") Long studentId);
}
