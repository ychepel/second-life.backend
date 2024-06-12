package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthenticatedException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.repositories.AdminRepository;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.services.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = repository.findByEmail(username);
        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found");
        }
        return admin;
    }

    @Override
    public Admin getAuthenticatedAdmin() throws CredentialException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal().equals("anonymousUser")) {
            throw new UserIsNotAuthenticatedException();
        }
        if (!authentication.getAuthorities().contains(Role.ROLE_ADMIN)) {
            throw new UserIsNotAuthorizedException();
        }
        String username = authentication.getName();
        return (Admin) loadUserByUsername(username);
    }
}
