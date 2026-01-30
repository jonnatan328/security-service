package com.company.security.authentication.infrastructure.adapter.output.directory;

import com.company.security.authentication.domain.model.AuthenticatedUser;
import org.springframework.ldap.core.DirContextOperations;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting LDAP/AD directory context to AuthenticatedUser.
 */
public class DirectoryUserMapper {

    // LDAP attribute names
    private static final String ATTR_UID = "uid";
    private static final String ATTR_MAIL = "mail";
    private static final String ATTR_GIVEN_NAME = "givenName";
    private static final String ATTR_SN = "sn";
    private static final String ATTR_MEMBER_OF = "memberOf";
    private static final String ATTR_USER_ACCOUNT_CONTROL = "userAccountControl";

    // Active Directory attribute names
    private static final String ATTR_SAM_ACCOUNT_NAME = "sAMAccountName";
    private static final String ATTR_USER_PRINCIPAL_NAME = "userPrincipalName";

    // AD userAccountControl flags
    private static final int AD_ACCOUNT_DISABLED = 0x0002;

    public AuthenticatedUser mapFromLdapContext(DirContextOperations ctx, String username) {
        String userId = getStringAttribute(ctx, ATTR_UID, username);
        String email = getStringAttribute(ctx, ATTR_MAIL, null);
        String firstName = getStringAttribute(ctx, ATTR_GIVEN_NAME, null);
        String lastName = getStringAttribute(ctx, ATTR_SN, null);
        Set<String> groups = getGroups(ctx);
        Set<String> roles = extractRolesFromGroups(groups);

        return AuthenticatedUser.builder()
                .userId(userId)
                .username(username)
                .email(email != null ? email : username + "@unknown.local")
                .firstName(firstName)
                .lastName(lastName)
                .groups(groups)
                .roles(roles)
                .enabled(true)
                .build();
    }

    public AuthenticatedUser mapFromActiveDirectoryContext(DirContextOperations ctx) {
        String username = getStringAttribute(ctx, ATTR_SAM_ACCOUNT_NAME, null);
        if (username == null) {
            username = getStringAttribute(ctx, ATTR_USER_PRINCIPAL_NAME, "");
            if (username.contains("@")) {
                username = username.substring(0, username.indexOf('@'));
            }
        }

        String userId = getStringAttribute(ctx, ATTR_UID, username);
        String email = getStringAttribute(ctx, ATTR_MAIL, null);
        String firstName = getStringAttribute(ctx, ATTR_GIVEN_NAME, null);
        String lastName = getStringAttribute(ctx, ATTR_SN, null);
        Set<String> groups = getGroups(ctx);
        Set<String> roles = extractRolesFromGroups(groups);
        boolean enabled = isAccountEnabled(ctx);

        return AuthenticatedUser.builder()
                .userId(userId)
                .username(username)
                .email(email != null ? email : username + "@unknown.local")
                .firstName(firstName)
                .lastName(lastName)
                .groups(groups)
                .roles(roles)
                .enabled(enabled)
                .build();
    }

    private String getStringAttribute(DirContextOperations ctx, String attributeName, String defaultValue) {
        String value = ctx.getStringAttribute(attributeName);
        return value != null ? value : defaultValue;
    }

    private Set<String> getGroups(DirContextOperations ctx) {
        String[] memberOf = ctx.getStringAttributes(ATTR_MEMBER_OF);
        if (memberOf == null) {
            return new HashSet<>();
        }
        return Arrays.stream(memberOf)
                .map(this::extractGroupName)
                .collect(Collectors.toSet());
    }

    private String extractGroupName(String dn) {
        // Extract CN from DN like "CN=GroupName,OU=Groups,DC=example,DC=com"
        if (dn.startsWith("CN=") || dn.startsWith("cn=")) {
            int commaIndex = dn.indexOf(',');
            if (commaIndex > 3) {
                return dn.substring(3, commaIndex);
            }
            return dn.substring(3);
        }
        return dn;
    }

    private Set<String> extractRolesFromGroups(Set<String> groups) {
        // Map groups to roles based on naming convention
        // e.g., "APP_ADMIN" -> "ROLE_ADMIN", "APP_USER" -> "ROLE_USER"
        return groups.stream()
                .filter(group -> group.startsWith("APP_") || group.startsWith("ROLE_"))
                .map(group -> {
                    if (group.startsWith("APP_")) {
                        return "ROLE_" + group.substring(4);
                    }
                    return group;
                })
                .collect(Collectors.toSet());
    }

    private boolean isAccountEnabled(DirContextOperations ctx) {
        String uac = ctx.getStringAttribute(ATTR_USER_ACCOUNT_CONTROL);
        if (uac == null) {
            return true;
        }
        try {
            int flags = Integer.parseInt(uac);
            return (flags & AD_ACCOUNT_DISABLED) == 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }
}
