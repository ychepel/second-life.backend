package de.ait.secondlife.services;

import de.ait.secondlife.constants.EntityTypeWithImages;
import de.ait.secondlife.constants.NotificationType;
import de.ait.secondlife.domain.dto.UserCreationDto;
import de.ait.secondlife.domain.dto.UserDto;
import de.ait.secondlife.domain.entity.User;
import de.ait.secondlife.domain.interfaces.AuthenticatedUser;
import de.ait.secondlife.exception_handling.exceptions.ConfirmationEmailCodeNotValidException;
import de.ait.secondlife.exception_handling.exceptions.DuplicateUserEmailException;
import de.ait.secondlife.exception_handling.exceptions.UserSavingException;
import de.ait.secondlife.exception_handling.exceptions.bad_request_exception.is_null_exceptions.IdIsNullException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.LocationNotFoundException;
import de.ait.secondlife.exception_handling.exceptions.not_found_exception.UserNotFoundException;
import de.ait.secondlife.repositories.UserRepository;
import de.ait.secondlife.security.services.AuthService;
import de.ait.secondlife.services.interfaces.*;
import de.ait.secondlife.services.mapping.NewUserMappingService;
import de.ait.secondlife.services.mapping.UserMappingService;
import de.ait.secondlife.services.utilities.UserPermissionsUtilities;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.CredentialException;
import java.time.LocalDateTime;

/**
 * Implementation of the UserService interface for managing user operations.(Version 1.0)
 *
 * <p>
 * This service class provides methods for user registration, setting location, checking entity existence by ID,
 * getting the current authenticated user, activating a user account, and finding a user by ID.
 * It interacts with UserRepository for CRUD operations, NewUserMappingService and UserMappingService for mapping,
 * BCryptPasswordEncoder for password encoding, LocationService for managing user location,
 * EmailService for sending notifications, ConfirmationService for managing user activation codes,
 * UserPermissionsUtilities for permission checks, and ImageService for handling user images.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#register(UserCreationDto)} method registers a new user based on the provided DTO.
 * It validates the uniqueness of the email address and encrypts the password before saving the user.
 * It also sends a registration email notification and connects temporary images to the user entity.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#setLocation(Long, Long)} method sets the location for a user identified by userId.
 * It validates the existence of the user and location based on their IDs and saves the updated user.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#checkEntityExistsById(Long)} method checks if a user entity exists by its ID.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#getCurrentUser()} method retrieves the current authenticated user.
 * It throws a {@link CredentialException} if the user is not authenticated.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#setActive(Long, String)} method activates a user account using a confirmation code.
 * It verifies the code against the one stored in the database and activates the user if they match.
 * </p>
 *
 * <p>
 * The {@link UserServiceImpl#findById(Long)} method finds a user by their ID and returns an AuthenticatedUser instance.
 * It throws a {@link UserNotFoundException} if no user with the specified ID is found.
 * </p>
 *
 * <p>
 * Exceptions that may be thrown by this class include:
 * <ul>
 *     <li>{@link DuplicateUserEmailException} - if the email address already exists during registration</li>
 *     <li>{@link UserNotFoundException} - if the user with the specified ID is not found</li>
 *     <li>{@link LocationNotFoundException} - if the location with the specified ID is not found</li>
 *     <li>{@link UserSavingException} - if there is an issue with saving user data</li>
 *     <li>{@link ConfirmationEmailCodeNotValidException} - if the confirmation code provided is not valid</li>
 *     <li>{@link IdIsNullException} - if a required ID parameter is null</li>
 *     <li>{@link CredentialException} - if there is an issue with user credentials</li>
 *     <li>{@link NullPointerException} - if the confirmation code is null</li>
 * </ul>
 * </p>
 *
 * <p>
 * Author: Second Life Team
 * </p>
 *
 * @author Second Life Team
 * @version 1.0
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final NewUserMappingService newUserMappingService;
    private final UserMappingService userMappingService;
    private final BCryptPasswordEncoder encoder;
    private final LocationService locationService;
    private final EmailService emailService;
    private final ConfirmationService confirmationService;
    private final UserPermissionsUtilities utilities;
    @Lazy
    @Autowired
    private ImageService imageService;

    /**
     * Registers a new user based on the provided UserCreationDto.
     *
     * @param newUserDto the UserCreationDto containing user registration information
     * @return UserDto representing the registered user
     * @throws DuplicateUserEmailException if the email address already exists
     * @throws UserSavingException         if there is an issue with saving the user data
     */
    @Override
    public UserDto register(UserCreationDto newUserDto) {
        if (newUserDto.getBaseNameOfImages() != null)
            utilities.checkUserPermissionsForImageByBaseName(newUserDto.getBaseNameOfImages());
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

    /**
     * Sets the location for a user identified by userId.
     *
     * @param userId     the ID of the user
     * @param locationId the ID of the location to set for the user
     * @return UserDto representing the updated user with the new location
     * @throws UserNotFoundException     if the user with the specified ID is not found
     * @throws LocationNotFoundException if the location with the specified ID is not found
     * @throws UserSavingException       if there is an issue with saving the user data
     */
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

    /**
     * Checks if a user entity exists by its ID.
     *
     * @param id the ID of the user entity
     * @return true if the user entity exists and is active, false otherwise
     * @throws IdIsNullException if the ID parameter is null
     */
    @Override
    public boolean checkEntityExistsById(Long id) {
        if (id == null) throw new IdIsNullException();
        return userRepository.existsByIdAndActiveTrue(id);
    }

    /**
     * Retrieves the current authenticated user.
     *
     * @return UserDto representing the current authenticated user
     * @throws CredentialException if there is an issue with user credentials
     */
    @Override
    public UserDto getCurrentUser() throws CredentialException {
        return userMappingService.toDto(AuthService.getCurrentUser());
    }

    /**
     * Activates a user account using a confirmation code.
     *
     * @param userId the ID of the user account to activate
     * @param code   the confirmation code for activating the user account
     * @return UserDto representing the activated user
     * @throws UserNotFoundException                  if the user with the specified ID is not found
     * @throws NullPointerException                   if the confirmation code is null
     * @throws ConfirmationEmailCodeNotValidException if the confirmation code provided is not valid
     * @throws UserSavingException                    if there is an issue with saving the user data
     */
    @Override
    @Transactional
    public UserDto setActive(Long userId, String code) {
        if (userId == null || userId < 1) {
            throw new UserNotFoundException(userId);
        }
        if (code == null) {
            throw new NullPointerException("Confirmation code cannot be null");
        }
        String codeFromDB = confirmationService.getConfirmationCodeByUserId(userId);

        if (code.equals(codeFromDB)) {
            User user = userRepository.getReferenceById(userId);
            user.setActive(true);
            try {
                userRepository.save(user);
            } catch (Exception e) {
                throw new UserSavingException("User saving failed", e);
            }
            return userMappingService.toDto(user);
        } else {
            throw new ConfirmationEmailCodeNotValidException(code);
        }
    }

    /**
     * Finds a user by their ID and returns an AuthenticatedUser instance.
     *
     * @param userId the ID of the user to find
     * @return AuthenticatedUser representing the found user
     * @throws UserNotFoundException if no user with the specified ID is found
     */
    @Override
    public AuthenticatedUser findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
}
