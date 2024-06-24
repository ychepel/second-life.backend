package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface CustomAdminDetails extends UserDetailsService {

    UserDetails loadUserByUsername(String username);

    AuthenticatedUser findById(Long adminId);

    Admin getDefaultAdmin();
}