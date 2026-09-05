package com.studentoj.sandbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studentoj.sandbox.service.SandboxService;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SandboxServiceTests {
    private SandboxService service;

    @BeforeEach
    void setUp() throws Exception {
        service = new SandboxService(null);
        setIntField("maxRows", 5000);
        setIntField("queryTimeoutSeconds", 5);
    }

    @Test
    void duplicateColumnLabelsDoNotHideWrongValues() throws Exception {
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:duplicate-labels")) {
            Object actual = invoke("runQuery", connection, "SELECT 999 AS x, 2 AS x");
            Object expected = invoke("runQuery", connection, "SELECT 1 AS x, 2 AS x");

            assertFalse((boolean) invoke("compare", actual, expected, false));
        }
    }

    @Test
    void detectsResultsBeyondConfiguredRowLimit() throws Exception {
        setIntField("maxRows", 3);
        try (Connection connection = DriverManager.getConnection("jdbc:h2:mem:row-limit")) {
            Object result = invoke("runQuery", connection, "SELECT X FROM SYSTEM_RANGE(1, 4)");
            Method truncated = result.getClass().getDeclaredMethod("truncated");
            truncated.setAccessible(true);

            assertTrue((boolean) truncated.invoke(result));
            assertFalse((boolean) invoke("compare", result, result, true));
        }
    }

    @Test
    void onlyTopLevelOrderByMakesResultOrderSensitive() throws Exception {
        assertFalse((boolean) invoke("hasOrderBy",
                "SELECT name, RANK() OVER (ORDER BY score DESC) AS r FROM score"));
        assertTrue((boolean) invoke("hasOrderBy", "SELECT name FROM score ORDER BY name"));
    }

    @Test
    void allowsDangerousWordsInsideStringLiteralsButBlocksDangerousFunctions() throws Exception {
        assertNull(invoke("validateStudentSql", "SELECT 'delete' AS word"));
        assertEquals("Dangerous SQL function or output clause detected.",
                invoke("validateStudentSql", "SELECT SLEEP(1)"));
    }

    private Object invoke(String name, Object... args) throws Exception {
        for (Method method : SandboxService.class.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                method.setAccessible(true);
                return method.invoke(service, args);
            }
        }
        throw new NoSuchMethodException(name);
    }

    private void setIntField(String name, int value) throws Exception {
        Field field = SandboxService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.setInt(service, value);
    }
}
