package com.studentoj.teacher.dto;

public record ExportRequest(Long groupId, String startDate, String endDate, String format) {
}
