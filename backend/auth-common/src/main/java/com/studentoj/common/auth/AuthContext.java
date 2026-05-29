package com.studentoj.common.auth;

public final class AuthContext {
    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser currentUser() {
        CurrentUser user = HOLDER.get();
        if (user == null) {
            throw new IllegalStateException("No authenticated user in current request");
        }
        return user;
    }

    public static Long userId() {
        return currentUser().userId();
    }

    public static String role() {
        return currentUser().role();
    }

    public static boolean isTeacher() {
        String role = role();
        return "TEACHER".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
