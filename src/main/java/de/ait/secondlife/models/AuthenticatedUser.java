package de.ait.secondlife.models;

import org.springframework.security.core.userdetails.UserDetails;

public interface AuthenticatedUser extends UserDetails {

    String getEmail();
}
