package de.ait.secondlife.services.interfaces;

import de.ait.secondlife.domain.dto.NewUserDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService,CheckEntityExistsService {
    UserDto register(NewUserDto userDto);

    void updateLastActive(User user);

    UserDto setLocation(Long userId, Long locationId);
}
