package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Admin;

import javax.security.auth.login.CredentialException;

import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AdminService extends UserDetailsService {

    Admin getDefaultAdmin();

    AuthenticatedUser findById(Long userId);
}
