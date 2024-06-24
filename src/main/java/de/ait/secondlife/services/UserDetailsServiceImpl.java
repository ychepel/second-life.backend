package de.ait.secondlife.services;

import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.services.interfaces.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements CustomUserDetails {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UserNotFoundException(username);
        }
        return user;
    }

    public void updateLastActive(User user) {
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }
}
