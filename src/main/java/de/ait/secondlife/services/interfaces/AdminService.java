package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Admin;

import javax.security.auth.login.CredentialException;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AdminService extends UserDetailsService {

    Admin getAuthenticatedAdmin() throws CredentialException;
    Admin getDefaultAdmin();
}
