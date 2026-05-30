package com.studentoj.problem.controller;

import com.studentoj.common.auth.RequireRole;
import com.studentoj.common.auth.Role;
import com.studentoj.problem.dto.ProblemAdminResponse;
import com.studentoj.problem.dto.ProblemSaveRequest;
import com.studentoj.problem.service.ProblemAdminService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
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
@RequestMapping("/api/problem/admin")
@RequireRole(Role.TEACHER)
public class ProblemAdminController {

    private final ProblemAdminService adminService;

    public ProblemAdminController(ProblemAdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/list")
    public List<ProblemAdminResponse> list() {
        return adminService.list();
    }

    @GetMapping("/{id}")
    public ProblemAdminResponse detail(@PathVariable Long id) {
        return adminService.detail(id);
    }

    @PostMapping
    public ProblemAdminResponse create(@RequestBody ProblemSaveRequest request) {
        return adminService.create(request);
    }

    @PutMapping("/{id}")
    public ProblemAdminResponse update(@PathVariable Long id, @RequestBody ProblemSaveRequest request) {
        return adminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        adminService.delete(id);
    }

    @GetMapping(value = "/template", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> template() {
        byte[] bytes = adminService.exportTemplate();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=problem-template.xlsx")
                .body(bytes);
    }

    @PostMapping("/import")
    public Map<String, Object> importProblems(@RequestParam("file") MultipartFile file) {
        return Map.of("imported", adminService.importProblems(file));
    }
}
