package de.ait.secondlife.security;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {

    ROLE_USER("user"),
    ROLE_ADMIN("admin");

    private final String shortName;

    Role(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public String getAuthority() {
        return this.name();
    }

    @Override
    public String toString() {
        return this.shortName;
    }
}
