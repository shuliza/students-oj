package com.studentoj.teacher.service;

import com.studentoj.teacher.dto.ClassGroupResponse;
import com.studentoj.teacher.dto.ExportRequest;
import com.studentoj.teacher.dto.GroupRequest;
import com.studentoj.teacher.dto.StudentResponse;
import com.studentoj.teacher.mapper.TeacherMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TeacherService {

    private static final Logger log = LoggerFactory.getLogger(TeacherService.class);

    private final TeacherMapper teacherMapper;

    public TeacherService(TeacherMapper teacherMapper) {
        this.teacherMapper = teacherMapper;
    }

    public List<StudentResponse> students() {
        try {
            return teacherMapper.selectStudents().stream().map(this::toStudent).toList();
        } catch (Exception e) {
            log.warn("Failed to load students from DB: {}", e.getMessage());
            return List.of();
        }
    }

    public List<ClassGroupResponse> groups() {
        try {
            return teacherMapper.selectGroups().stream().map(this::toGroup).toList();
        } catch (Exception e) {
            log.warn("Failed to load groups from DB: {}", e.getMessage());
            return List.of();
        }
    }

    public byte[] exportGrades(ExportRequest request) {
        Long groupId = (request != null && request.groupId() != null && request.groupId() > 0) ? request.groupId() : null;
        List<Map<String, Object>> data = teacherMapper.selectGradeData(groupId, null);
        return generateGradeExcel(data);
    }

    public byte[] exportStudentGrades(Long studentId) {
        if (studentId == null || studentId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "学生ID不能为空");
        }
        List<Map<String, Object>> data = teacherMapper.selectGradeData(null, studentId);
        return generateGradeExcel(data);
    }

    private byte[] generateGradeExcel(List<Map<String, Object>> data) {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("成绩导出");
            Row header = sheet.createRow(0);
            String[] cols = {"学号", "姓名", "分组", "题目", "提交次数", "最高分", "通过状态"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int rowIdx = 1;
            for (Map<String, Object> row : data) {
                Row r = sheet.createRow(rowIdx++);
                r.createCell(0).setCellValue((String) row.getOrDefault("studentNo", ""));
                r.createCell(1).setCellValue((String) row.getOrDefault("realName", ""));
                r.createCell(2).setCellValue((String) row.getOrDefault("groupName", ""));
                r.createCell(3).setCellValue((String) row.getOrDefault("problemTitle", ""));
                r.createCell(4).setCellValue(row.get("submitCount") == null ? 0 : ((Number) row.get("submitCount")).intValue());
                r.createCell(5).setCellValue(row.get("bestScore") == null ? 0 : ((Number) row.get("bestScore")).intValue());
                int passed = row.get("passed") == null ? 0 : ((Number) row.get("passed")).intValue();
                r.createCell(6).setCellValue(passed == 1 ? "通过" : "未通过");
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "导出失败");
        }
    }

    public void createGroup(GroupRequest request) {
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分组名称不能为空");
        }
        teacherMapper.insertGroup(request.name().trim(),
                request.teacherName() == null ? "" : request.teacherName().trim(),
                request.description() == null ? "" : request.description().trim());
    }

    public void updateGroup(Long id, GroupRequest request) {
        if (id == null || teacherMapper.selectGroupById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        if (request == null || request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "分组名称不能为空");
        }
        teacherMapper.updateGroup(id, request.name().trim(),
                request.teacherName() == null ? "" : request.teacherName().trim(),
                request.description() == null ? "" : request.description().trim());
    }

    public void deleteGroup(Long id) {
        if (id == null || teacherMapper.selectGroupById(id) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        // Clear group_id for all students in this group
        List<Map<String, Object>> members = teacherMapper.selectGroupMembers(id);
        for (Map<String, Object> m : members) {
            teacherMapper.removeUserFromGroup(((Number) m.get("id")).longValue());
        }
        teacherMapper.deleteGroup(id);
    }

    public List<StudentResponse> getGroupMembers(Long groupId) {
        if (groupId == null || teacherMapper.selectGroupById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        return teacherMapper.selectGroupMembers(groupId).stream().map(this::toStudent).toList();
    }

    public void addGroupMembers(Long groupId, List<Long> studentIds) {
        if (groupId == null || teacherMapper.selectGroupById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        if (studentIds == null) return;
        for (Long studentId : studentIds) {
            teacherMapper.assignUserToGroup(studentId, groupId);
        }
    }

    public void removeGroupMembers(Long groupId, List<Long> studentIds) {
        if (groupId == null || teacherMapper.selectGroupById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        if (studentIds == null) return;
        for (Long studentId : studentIds) {
            teacherMapper.removeUserFromGroup(studentId);
        }
    }

    public byte[] exportStudentTemplate() {
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("学生导入模板");
            Row header = sheet.createRow(0);
            String[] cols = {"学号", "姓名", "账号", "密码", "分组"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            // Example row
            Row example = sheet.createRow(1);
            example.createCell(0).setCellValue("20260001");
            example.createCell(1).setCellValue("张三");
            example.createCell(2).setCellValue("zhangsan");
            example.createCell(3).setCellValue("123456");
            example.createCell(4).setCellValue("数据库 1 班");
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "生成模板失败");
        }
    }

    public int importStudents(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请上传文件");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            // Skip header row
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String studentNo = getCellStr(row.getCell(0));
                String realName = getCellStr(row.getCell(1));
                String username = getCellStr(row.getCell(2));
                String password = getCellStr(row.getCell(3));
                String groupName = getCellStr(row.getCell(4));
                if (username.isEmpty()) continue;
                Long groupId = groupName.isEmpty() ? null : teacherMapper.selectGroupIdByName(groupName);
                String passwordHash = password.isEmpty() ? "" : encoder.encode(password);
                try {
                    teacherMapper.insertStudent(username, passwordHash, realName, studentNo, groupId);
                    count++;
                } catch (Exception e) {
                    log.warn("Skip student {}: {}", username, e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件解析失败");
        }
        return count;
    }

    public byte[] exportGroupMembers(Long groupId) {
        if (groupId == null || teacherMapper.selectGroupById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        List<Map<String, Object>> members = teacherMapper.selectGroupMembers(groupId);
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("分组成员");
            Row header = sheet.createRow(0);
            String[] cols = {"学号", "姓名", "账号", "状态"};
            for (int i = 0; i < cols.length; i++) {
                header.createCell(i).setCellValue(cols[i]);
            }
            int rowIdx = 1;
            for (Map<String, Object> m : members) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue((String) m.getOrDefault("studentNo", ""));
                row.createCell(1).setCellValue((String) m.getOrDefault("realName", ""));
                row.createCell(2).setCellValue((String) m.getOrDefault("username", ""));
                row.createCell(3).setCellValue((String) m.getOrDefault("status", ""));
            }
            for (int i = 0; i < cols.length; i++) {
                sheet.autoSizeColumn(i);
            }
            wb.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "导出失败");
        }
    }

    public int importGroupMembers(Long groupId, MultipartFile file) {
        if (groupId == null || teacherMapper.selectGroupById(groupId) == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "分组不存在");
        }
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "请上传文件");
        }
        int count = 0;
        try (InputStream is = file.getInputStream(); Workbook wb = new XSSFWorkbook(is)) {
            Sheet sheet = wb.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                String studentNo = getCellStr(row.getCell(0));
                // Try to find student by student_no
                if (studentNo.isEmpty()) continue;
                // Find user ID from student_no - query all students and match
                List<Map<String, Object>> allStudents = teacherMapper.selectStudents();
                for (Map<String, Object> s : allStudents) {
                    if (studentNo.equals(s.get("studentNo"))) {
                        Long userId = ((Number) s.get("id")).longValue();
                        teacherMapper.assignUserToGroup(userId, groupId);
                        count++;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "文件解析失败");
        }
        return count;
    }

    private String getCellStr(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.STRING) return cell.getStringCellValue().trim();
        if (cell.getCellType() == CellType.NUMERIC) return String.valueOf((long) cell.getNumericCellValue());
        return "";
    }

    private StudentResponse toStudent(Map<String, Object> row) {
        return new StudentResponse(
                ((Number) row.get("id")).longValue(),
                (String) row.get("username"),
                (String) row.get("studentNo"),
                (String) row.get("realName"),
                (String) row.get("groupName"),
                (String) row.get("status")
        );
    }

    private ClassGroupResponse toGroup(Map<String, Object> row) {
        Object count = row.get("studentCount");
        return new ClassGroupResponse(
                ((Number) row.get("id")).longValue(),
                (String) row.get("name"),
                (String) row.get("teacherName"),
                count == null ? 0 : ((Number) count).intValue(),
                (String) row.get("description")
        );
    }
}
