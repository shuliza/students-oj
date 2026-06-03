package com.studentoj.auth.service;

import com.studentoj.auth.dto.LoginResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenStoreTests {

    @Test
    void issueRevokesPreviousTokenForSameUser() {
        TokenStore tokenStore = new TokenStore(null, 3600);
        LoginResponse user = user(1L, "alice");

        String firstToken = tokenStore.issue(user);
        String secondToken = tokenStore.issue(user);

        assertThat(tokenStore.resolve(firstToken)).isNull();
        LoginResponse current = tokenStore.resolve(secondToken);
        assertThat(current).isNotNull();
        assertThat(current.token()).isEqualTo(secondToken);
        assertThat(current.userId()).isEqualTo(1L);
    }

    @Test
    void issueKeepsOtherUsersTokenValid() {
        TokenStore tokenStore = new TokenStore(null, 3600);

        String firstUserToken = tokenStore.issue(user(1L, "alice"));
        String secondUserToken = tokenStore.issue(user(2L, "bob"));

        assertThat(tokenStore.resolve(firstUserToken)).isNotNull();
        assertThat(tokenStore.resolve(secondUserToken)).isNotNull();
    }

    private LoginResponse user(Long userId, String username) {
        return new LoginResponse(
                null,
                userId,
                username,
                "STUDENT",
                username,
                null,
                null,
                null,
                "ACTIVE"
        );
    }
}
