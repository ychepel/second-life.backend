package de.ait.secondlife.services;

import de.ait.secondlife.domain.dto.NewUserDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.services.interfaces.UserService;
import de.ait.secondlife.services.mapping.NewUserMappingService;
import de.ait.secondlife.services.mapping.UserMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final NewUserMappingService newUserMappingService;
    private final UserMappingService userMappingService;
    private final BCryptPasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Override
    public UserDto register(NewUserDto newUserDto) {
        String userEmail = newUserDto.getEmail();
        if (repository.existsByEmail(userEmail)) {
            throw new DuplicateUserEmailException(userEmail);
        }

        User user = newUserMappingService.toEntity(newUserDto);
        user.setActive(true);
        user.setPassword(encoder.encode(newUserDto.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        try {
            repository.save(user);
        } catch (Exception e) {
            throw new UserSavingException("User saving failed", e);
        }

        return userMappingService.toDto(user);
    }

    @Override
    public void updateLastActive(User user) {
        user.setLastActive(LocalDateTime.now());
        repository.save(user);
    }
}
