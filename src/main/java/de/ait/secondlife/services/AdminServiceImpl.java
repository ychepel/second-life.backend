package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthenticatedException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.AdminNotFoundException;
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
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = repository.findByEmail(username);
        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found");
        }
        return admin;
    }

    @Override
    public Admin getDefaultAdmin() {
        return adminRepository.findAll().get(0);
    }

    @Override
    public AuthenticatedUser findById(Long adminId) {
        return adminRepository.findById(adminId).orElseThrow(()-> new AdminNotFoundException(adminId));
    }

}
