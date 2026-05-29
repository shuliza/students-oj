package com.studentoj.common.auth;

public enum Role {
    STUDENT,
    TEACHER,
    ADMIN;

    public boolean allows(String actualRole) {
        if (actualRole == null) {
            return false;
        }
        if (this == ADMIN) {
            return "ADMIN".equalsIgnoreCase(actualRole);
        }
        if (this == TEACHER) {
            return "TEACHER".equalsIgnoreCase(actualRole) || "ADMIN".equalsIgnoreCase(actualRole);
        }
        return "STUDENT".equalsIgnoreCase(actualRole);
    }
}
