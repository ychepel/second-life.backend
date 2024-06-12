package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Admin;

import javax.security.auth.login.CredentialException;

public interface AdminService extends AuthenticatedUserService {

    Admin getAuthenticatedAdmin() throws CredentialException;
}
