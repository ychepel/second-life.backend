package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.*;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.LocationNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.ConfirmationEmailCodeNotValidException;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.ImageService;
import de.ait.secondlife.services.interfaces.LocationService;
import de.ait.secondlife.security.Role;
import de.ait.secondlife.services.interfaces.ConfirmationService;
import de.ait.secondlife.services.interfaces.EmailService;
import de.ait.secondlife.services.interfaces.UserService;
import de.ait.secondlife.services.mapping.NewUserMappingService;
import de.ait.secondlife.services.mapping.UserMappingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
    private final EmailService emailService;
    private final ConfirmationService confirmationService;

    @Override
    public UserDto register(UserCreationDto newUserDto) {
        String userEmail = newUserDto.getEmail();
        if (userRepository.existsByEmail(userEmail)) {
            throw new DuplicateUserEmailException(userEmail);
        }

        User user = newUserMappingService.toEntity(newUserDto);
        user.setActive(false);
        user.setPassword(encoder.encode(newUserDto.getPassword()));
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        User newUser;
        try {
            newUser = userRepository.save(user);
            imageService.connectTempImagesToEntity(
                    newUserDto.getBaseNameOfImages(),
                    EntityTypeWithImages.USER.getType(),
                    newUser.getId());
        } catch (Exception e) {
            throw new UserSavingException("User saving failed", e);
        }

        emailService.createNotification(newUser, NotificationType.REGISTRATION_EMAIL);
        return userMappingService.toDto(user);
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
    public UserDto getCurrentUser() throws CredentialException {
        return userMappingService.toDto(AuthService.getCurrentUser());
    }

    @Override
    @Transactional
    public UserDto setActive(Long userId, String code) {
        if (userId == null || userId < 1){
            throw new UserNotFoundException(userId);
        }

        if (code == null){
            throw new NullPointerException("Confirmation code cannot be null");
        }

        String codeFromDB = confirmationService.getConfirmationCodeByUserId(userId);

        if (code.equals(codeFromDB)){
            User user = userRepository.getReferenceById(userId);
            user.setActive(true);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new UserSavingException("User saving failed", e);
            }
            return userMappingService.toDto(user);
        }else {
            throw new ConfirmationEmailCodeNotValidException(code);
        }
    }

    @Override
    public AuthenticatedUser findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }
}
