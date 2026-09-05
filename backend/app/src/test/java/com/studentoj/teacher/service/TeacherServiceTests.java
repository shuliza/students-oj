package com.studentoj.teacher.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.studentoj.auth.service.TokenStore;
import com.studentoj.teacher.mapper.TeacherMapper;
import org.junit.jupiter.api.Test;

class TeacherServiceTests {
    @Test
    void disablingStudentRevokesSession() {
        TeacherMapper mapper = mock(TeacherMapper.class);
        TokenStore tokens = mock(TokenStore.class);
        when(mapper.selectStudentId(7L)).thenReturn(7L);
        TeacherService service = new TeacherService(mapper, tokens);

        service.updateStudentStatus(7L, "disabled");

        verify(mapper).updateStudentStatus(7L, "DISABLED");
        verify(tokens).revokeUser(7L);
    }

    @Test
    void resettingTeacherPasswordRevokesSession() {
        TeacherMapper mapper = mock(TeacherMapper.class);
        TokenStore tokens = mock(TokenStore.class);
        when(mapper.selectTeacherId(9L)).thenReturn(9L);
        TeacherService service = new TeacherService(mapper, tokens);

        service.resetTeacherPassword(9L, "new-password");

        verify(tokens).revokeUser(9L);
    }
}
