package com.studentoj.auth.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.studentoj.auth.dto.LoginResponse;
import org.junit.jupiter.api.Test;

class TokenStoreTests {
    @Test
    void revokingUserInvalidatesCurrentToken() {
        TokenStore store = new TokenStore(null, 3600);
        LoginResponse user = new LoginResponse(null, 42L, "student42", "STUDENT", "Student", null,
                null, null, "ACTIVE");
        String token = store.issue(user);

        assertNotNull(store.resolve(token));
        store.revokeUser(42L);
        assertNull(store.resolve(token));
    }
}
