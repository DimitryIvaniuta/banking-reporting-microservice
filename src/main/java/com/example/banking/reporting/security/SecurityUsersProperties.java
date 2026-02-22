package com.example.banking.reporting.security;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized security configuration for local/basic-auth users.
 * <p>
 * Production deployments should replace this with an enterprise identity provider (OIDC/OAuth2) and map
 * claims/groups to the same application roles.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.security")
public class SecurityUsersProperties {

    /**
     * In-memory users enabled for local development and test environments.
     */
    @Valid
    @NotEmpty
    private List<UserRecord> users = new ArrayList<>();

    /** User entry for local basic authentication. */
    @Getter
    @Setter
    public static class UserRecord {
        /** Login name. */
        @NotBlank
        private String username;
        /** Plain-text password from config; encoded at startup. */
        @NotBlank
        private String password;
        /** Application roles without ROLE_ prefix (e.g., REPORT_VIEWER). */
        @NotEmpty
        private List<@NotBlank String> roles = new ArrayList<>();
    }
}
