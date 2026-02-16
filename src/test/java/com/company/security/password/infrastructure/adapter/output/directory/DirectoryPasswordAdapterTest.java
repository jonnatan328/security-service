package com.company.security.password.infrastructure.adapter.output.directory;

import com.company.security.shared.infrastructure.properties.LdapProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import reactor.test.StepVerifier;

import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectoryPasswordAdapterTest {

    @Mock
    private LdapTemplate ldapTemplate;

    @Mock
    private LdapContextSource ldapContextSource;

    private DirectoryPasswordAdapter adapter;

    @BeforeEach
    void setUp() {
        LdapProperties ldapProperties = new LdapProperties();
        ldapProperties.setUserDnAttribute("uid");
        ldapProperties.setUserSearchBase("ou=people,dc=company,dc=com");
        adapter = new DirectoryPasswordAdapter(ldapTemplate, ldapContextSource, ldapProperties);
    }

    @Test
    void verifyPassword_withCorrectPassword_returnsTrue() {
        DirContext dirContext = mock(DirContext.class);
        when(ldapContextSource.getContext(
                "uid=user-123,ou=people,dc=company,dc=com",
                "correct-password"))
                .thenReturn(dirContext);

        StepVerifier.create(adapter.verifyPassword("user-123", "correct-password"))
                .assertNext(result -> assertThat(result).isTrue())
                .verifyComplete();
    }

    @Test
    void verifyPassword_withWrongPassword_returnsFalse() {
        when(ldapContextSource.getContext(anyString(), anyString()))
                .thenThrow(new RuntimeException("Auth failed"));

        StepVerifier.create(adapter.verifyPassword("user-123", "wrong-password"))
                .assertNext(result -> assertThat(result).isFalse())
                .verifyComplete();
    }

    @Test
    void changePassword_withValidInput_completes() {
        doNothing().when(ldapTemplate).modifyAttributes(anyString(), any(ModificationItem[].class));

        StepVerifier.create(adapter.changePassword("user-123", "newPassword123!"))
                .verifyComplete();
    }

    @Test
    void changePassword_withLdapError_throwsException() {
        doThrow(new RuntimeException("LDAP error"))
                .when(ldapTemplate).modifyAttributes(anyString(), any(ModificationItem[].class));

        StepVerifier.create(adapter.changePassword("user-123", "newPassword123!"))
                .expectError(IllegalStateException.class)
                .verify();
    }

    @Test
    void resetPassword_delegatesToChangePassword() {
        doNothing().when(ldapTemplate).modifyAttributes(anyString(), any(ModificationItem[].class));

        StepVerifier.create(adapter.resetPassword("user-123", "newPassword123!"))
                .verifyComplete();
    }
}
