package de.ait.secondlife.domain.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticatedUser extends UserDetails {

    String getEmail();
    Long getId();
}
