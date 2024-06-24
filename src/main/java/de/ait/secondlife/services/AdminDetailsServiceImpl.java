package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.Admin;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service implementation for loading Admin details based on username.(Version 1.0)
 * This service is used by Spring Security to authenticate and authorize Admin users.
 *
 * <p>
 * This service interacts with the AdminRepository to retrieve Admin details from the database.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link UsernameNotFoundException} - if the user with the given username is not found</li>
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
@RequiredArgsConstructor
@Service
public class AdminDetailsServiceImpl implements UserDetailsService {

    private final AdminRepository adminRepository;

    /**
     * Loads the user details for the admin with the specified username.
     *
     * @param username the username of the admin to be loaded.
     * @return the UserDetails object containing the admin's details.
     * @throws UsernameNotFoundException if the admin with the specified username is not found.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UserNotFoundException {
        Admin admin = adminRepository.findByEmail(username);
        if (admin == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return admin;
    }
}
