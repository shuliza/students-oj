package com.studentoj.teacher.controller;

import com.studentoj.common.auth.RequireRole;
import com.studentoj.common.auth.Role;
import com.studentoj.teacher.dto.ClassGroupResponse;
import com.studentoj.teacher.dto.ExportRequest;
import com.studentoj.teacher.dto.GroupRequest;
import com.studentoj.teacher.dto.StudentResponse;
import com.studentoj.teacher.service.TeacherService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/teacher")
@RequireRole(Role.TEACHER)
public class TeacherController {
    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping("/students")
    public List<StudentResponse> students() {
        return teacherService.students();
    }

    @GetMapping("/groups")
    public List<ClassGroupResponse> groups() {
        return teacherService.groups();
    }

    @PostMapping(value = "/grades/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportGrades(@RequestBody ExportRequest request) {
        byte[] bytes = teacherService.exportGrades(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-grades.xlsx")
                .body(bytes);
    }

    @GetMapping(value = "/grades/export/student/{studentId}", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportStudentGrades(@PathVariable Long studentId) {
        byte[] bytes = teacherService.exportStudentGrades(studentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-grades.xlsx")
                .body(bytes);
    }

    @PostMapping("/groups")
    public void createGroup(@RequestBody GroupRequest request) {
        teacherService.createGroup(request);
    }

    @PutMapping("/groups/{id}")
    public void updateGroup(@PathVariable Long id, @RequestBody GroupRequest request) {
        teacherService.updateGroup(id, request);
    }

    @DeleteMapping("/groups/{id}")
    public void deleteGroup(@PathVariable Long id) {
        teacherService.deleteGroup(id);
    }

    @GetMapping("/groups/{id}/members")
    public List<StudentResponse> getGroupMembers(@PathVariable Long id) {
        return teacherService.getGroupMembers(id);
    }

    @PostMapping("/groups/{id}/members")
    public void addGroupMembers(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        teacherService.addGroupMembers(id, studentIds);
    }

    @DeleteMapping("/groups/{id}/members")
    public void removeGroupMembers(@PathVariable Long id, @RequestBody List<Long> studentIds) {
        teacherService.removeGroupMembers(id, studentIds);
    }

    @GetMapping(value = "/students/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportStudentTemplate() {
        byte[] bytes = teacherService.exportStudentTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=student-template.xlsx")
                .body(bytes);
    }

    @PostMapping("/students/import")
    public Map<String, Object> importStudents(@RequestParam("file") MultipartFile file) {
        int count = teacherService.importStudents(file);
        return Map.of("imported", count);
    }

    @GetMapping(value = "/groups/{id}/members/export", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> exportGroupMembers(@PathVariable Long id) {
        byte[] bytes = teacherService.exportGroupMembers(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=group-members.xlsx")
                .body(bytes);
    }

    @PostMapping("/groups/{id}/members/import")
    public Map<String, Object> importGroupMembers(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        int count = teacherService.importGroupMembers(id, file);
        return Map.of("imported", count);
    }
}
