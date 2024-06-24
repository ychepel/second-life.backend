package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.services.interfaces.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of the UserDetailsService interface for loading user details by username.(Version 1.0)
 *
 * <p>
 * This service class provides methods to load user details from the UserRepository based on a username.
 * It implements Spring Security's UserDetailsService interface.
 * </p>
 *
 * <p>
 * The {@link UserDetailsServiceImpl#loadUserByUsername(String)} method loads a user entity by username.
 * It throws a {@link UsernameNotFoundException} if no user with the specified username is found.
 * </p>
 *
 * <p>
 * The {@link UserDetailsServiceImpl#updateLastActive(User)} method updates the last active timestamp of a user.
 * It saves the updated user entity using the UserRepository.
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @author Second Life Team
 * @version 1.0
 */
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements CustomUserDetails {

    private final UserRepository userRepository;

    /**
     * Loads user details by username.
     *
     * @param username the username (email) of the user to load
     * @return UserDetails object representing the loaded user
     * @throws UsernameNotFoundException if no user with the specified username is found
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }

    /**
     * Updates the last active timestamp of the user.
     *
     * @param user the user entity for which to update the last active timestamp
     */
    public void updateLastActive(User user) {
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }
}
