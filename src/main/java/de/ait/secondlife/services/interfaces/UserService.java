package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;

import javax.security.auth.login.CredentialException;

public interface UserService extends CheckEntityExistsService {

    UserDto register(UserCreationDto userDto);

    UserDto setLocation(Long userId, Long locationId);

    UserDto getCurrentUser() throws CredentialException;
}
