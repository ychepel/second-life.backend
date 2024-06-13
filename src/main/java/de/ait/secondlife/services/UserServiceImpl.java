package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthenticatedException;
import de.ait.secondlife.exception_handling.exceptions.UserIsNotAuthorizedException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.LocationNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.interfaces.LocationService;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.services.interfaces.UserService;
import de.ait.secondlife.services.mapping.NewUserMappingService;
import de.ait.secondlife.services.mapping.UserMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NewUserMappingService newUserMappingService;
    private final UserMappingService userMappingService;
    private final BCryptPasswordEncoder encoder;
    private final LocationService locationService;
    private final ImageService imageService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    @Override
    public UserDto register(UserCreationDto newUserDto) {
        String userEmail = newUserDto.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new DuplicateUserEmailException(userEmail);
        }

        User user = newUserMappingService.toEntity(newUserDto);
        user.setActive(true);
        user.setPassword(encoder.encode(newUserDto.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        String message = "";
        try {
            User newUser = userRepository.save(user);
            message = imageService
                    .connectTempImagesToEntity(
                            newUserDto.getBaseNameOfImages(),
                            EntityTypeWithImages.USER.getType(),
                            newUser.getId());
        } catch (Exception e) {
            throw new UserSavingException("User saving failed", e);
        }
        UserDto userDto = userMappingService.toDto(user);
        userDto.setImageUploadInfo(message);
        return userDto;
    }

    @Override
    public void updateLastActive(User user) {
        user.setLastActive(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public UserDto setLocation(Long userId, Long locationId) {

        if (userId == null || userId < 1) {
            throw new UserNotFoundException(userId);
        }

        if (locationId == null || locationId < 1) {
            throw new LocationNotFoundException(userId);
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        user.setLocation(locationService.getLocationById(locationId));

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new UserSavingException("User saving failed", e);
        }

        return userMappingService.toDto(user);
    }

    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return userRepository.existsByIdAndActiveTrue(id);
    }

    @Override
    public User getAuthenticatedUser() throws CredentialException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal().equals("anonymousUser")) {
            throw new UserIsNotAuthenticatedException();
        }
        if (!authentication.getAuthorities().contains(Role.ROLE_USER)) {
            throw new UserIsNotAuthorizedException();
        }
        String username = authentication.getName();
        return (User) loadUserByUsername(username);
    }

    @Override
    public UserDto getCurrentUser() throws CredentialException {
        return userMappingService.toDto(getAuthenticatedUser());
    }
}
