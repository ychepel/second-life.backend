package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.AdminNotFoundException;
import de.ait.secondlife.repositories.AdminRepository;
import de.ait.secondlife.services.interfaces.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for managing Admin operations.(Version 1.0)
 * This service provides methods for loading admin user details, retrieving the default admin,
 * and finding admin by ID.
 *
 * <p>
 * This service interacts with the AdminRepository to perform CRUD operations on Admin entities.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link UsernameNotFoundException} - if the admin with the given username is not found</li>
 *     <li>{@link AdminNotFoundException} - if the admin with the given ID is not found</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @version 1.0
 * @author: Second Life Team
 */
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository repository;
    private final AdminRepository adminRepository;

    /**
     * Loads the user details for the admin with the specified username.
     *
     * @param username the username of the admin to be loaded.
     * @return the UserDetails object containing the admin's details.
     * @throws UsernameNotFoundException if the admin with the specified username is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = repository.findByEmail(username);
        if (admin == null) {
            throw new UsernameNotFoundException("Admin not found");
        }
        return admin;
    }

    /**
     * Retrieves the default admin from the repository.
     *
     * @return the default Admin object.
     */
    @Override
    public Admin getDefaultAdmin() {
        return adminRepository.findAll().get(0);
    }

    /**
     * Finds the admin by their ID.
     *
     * @param adminId the ID of the admin to be found.
     * @return the AuthenticatedUser object representing the admin.
     * @throws AdminNotFoundException if the admin with the specified ID is not found.
     */
    @Override
    public AuthenticatedUser findById(Long adminId) {
        return adminRepository.findById(adminId).orElseThrow(() -> new AdminNotFoundException(adminId));
    }

}
