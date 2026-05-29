package com.studentoj.teacher.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TeacherMapper {

    @Select("SELECT u.id, u.username, u.student_no AS studentNo, u.real_name AS realName, u.status, g.name AS groupName " +
            "FROM user u LEFT JOIN class_group g ON g.id = u.group_id " +
            "WHERE u.role = 'STUDENT' ORDER BY u.id")
    List<Map<String, Object>> selectStudents();

    @Select("SELECT g.id, g.name, g.teacher_name AS teacherName, g.description, " +
            "(SELECT COUNT(*) FROM user u WHERE u.group_id = g.id AND u.role = 'STUDENT') AS studentCount " +
            "FROM class_group g ORDER BY g.id")
    List<Map<String, Object>> selectGroups();

    @Select("SELECT u.id, u.username, u.student_no AS studentNo, u.real_name AS realName, u.status " +
            "FROM user u WHERE u.group_id = #{groupId} AND u.role = 'STUDENT' ORDER BY u.student_no")
    List<Map<String, Object>> selectGroupMembers(@Param("groupId") Long groupId);

    @Update("UPDATE user SET group_id = #{groupId} WHERE id = #{userId} AND role = 'STUDENT'")
    int assignUserToGroup(@Param("userId") Long userId, @Param("groupId") Long groupId);

    @Update("UPDATE user SET group_id = NULL WHERE id = #{userId} AND role = 'STUDENT'")
    int removeUserFromGroup(@Param("userId") Long userId);

    @Insert("INSERT INTO class_group(name, teacher_name, description) VALUES(#{name}, #{teacherName}, #{description})")
    int insertGroup(@Param("name") String name, @Param("teacherName") String teacherName, @Param("description") String description);

    @Update("UPDATE class_group SET name = #{name}, teacher_name = #{teacherName}, description = #{description} WHERE id = #{id}")
    int updateGroup(@Param("id") Long id, @Param("name") String name, @Param("teacherName") String teacherName, @Param("description") String description);

    @Delete("DELETE FROM class_group WHERE id = #{id}")
    int deleteGroup(@Param("id") Long id);

    @Select("SELECT id FROM class_group WHERE id = #{id}")
    Long selectGroupById(@Param("id") Long id);

    @Select("SELECT id FROM class_group WHERE name = #{name}")
    Long selectGroupIdByName(@Param("name") String name);

    @Insert("INSERT INTO user(username, password_hash, real_name, student_no, group_id, role, status) " +
            "VALUES(#{username}, #{passwordHash}, #{realName}, #{studentNo}, #{groupId}, 'STUDENT', 'ACTIVE')")
    int insertStudent(@Param("username") String username, @Param("passwordHash") String passwordHash,
                      @Param("realName") String realName, @Param("studentNo") String studentNo,
                      @Param("groupId") Long groupId);

    @Select("""
            SELECT u.student_no AS studentNo, u.real_name AS realName, g.name AS groupName,
                   p.title AS problemTitle,
                   COUNT(s.id) AS submitCount,
                   MAX(s.score) AS bestScore,
                   MAX(CASE WHEN s.status = 'ACCEPTED' THEN 1 ELSE 0 END) AS passed
            FROM user u
            LEFT JOIN class_group g ON g.id = u.group_id
            CROSS JOIN problem p
            LEFT JOIN submission s ON s.user_id = u.id AND s.problem_id = p.id
            WHERE u.role = 'STUDENT' AND p.status = 1
              AND (#{groupId} IS NULL OR u.group_id = #{groupId})
              AND (#{studentId} IS NULL OR u.id = #{studentId})
            GROUP BY u.id, u.student_no, u.real_name, g.name, p.id, p.title
            ORDER BY u.student_no, p.id
            """)
    List<Map<String, Object>> selectGradeData(@Param("groupId") Long groupId, @Param("studentId") Long studentId);
}
