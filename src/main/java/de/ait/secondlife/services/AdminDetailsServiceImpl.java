package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.AdminNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.AdminRepository;
import de.ait.secondlife.services.interfaces.CustomAdminDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AdminDetailsServiceImpl implements CustomAdminDetails {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        Admin admin = adminRepository.findByEmail(username);
        if (admin == null) {
            throw new UserNotFoundException(username);
        }
        return admin;
    }

    @Override
    public AuthenticatedUser findById(Long adminId) {
        return adminRepository.findById(adminId).orElseThrow(()-> new AdminNotFoundException(adminId));
    }

    @Override
    public Admin getDefaultAdmin() {
        return adminRepository.findAll().get(0);
    }
}
