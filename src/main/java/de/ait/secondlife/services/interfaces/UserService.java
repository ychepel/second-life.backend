package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;

import javax.security.auth.login.CredentialException;

public interface UserService extends UserDetailsService,CheckEntityExistsService {
    UserDto register(NewUserDto userDto);

    void updateLastActive(User user);

    UserDto setLocation(Long userId, Long locationId);

    User getAuthenticatedUser() throws CredentialException;

    UserDto getCurrentUser() throws CredentialException;
}
