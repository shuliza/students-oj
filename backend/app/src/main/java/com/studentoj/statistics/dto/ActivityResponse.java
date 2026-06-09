package com.studentoj.statistics.dto;

import java.time.LocalDate;

public record ActivityResponse(LocalDate date, Integer count) {
}
