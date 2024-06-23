package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import org.springframework.security.core.userdetails.UserDetailsService;


public interface AdminService extends UserDetailsService {

    Admin getDefaultAdmin();

    AuthenticatedUser findById(Long userId);
}
