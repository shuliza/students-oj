package com.studentoj.common.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HtmlSanitizerTests {
    @Test
    void removesExecutableMarkupAndKeepsProblemTables() {
        String cleaned = HtmlSanitizer.cleanDescription(
                "<table><tr><td>score</td></tr></table><img src=x onerror=alert(1)><script>alert(2)</script>");

        assertTrue(cleaned.contains("<table>"));
        assertFalse(cleaned.contains("onerror"));
        assertFalse(cleaned.contains("<script"));
    }
}
