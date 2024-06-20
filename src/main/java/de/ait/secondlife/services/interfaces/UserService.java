package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;

import javax.security.auth.login.CredentialException;

public interface UserService extends CheckEntityExistsService {
    UserDto register(UserCreationDto userDto);

    void updateLastActive(User user);

    UserDto setLocation(Long userId, Long locationId);

    UserDto getCurrentUser() throws CredentialException;

    UserDto setActive(Long userId, String code);

    AuthenticatedUser findById(Long userId);
}
