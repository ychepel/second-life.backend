package de.ait.secondlife.services.validator;

import de.ait.secondlife.exception_handling.exeptions.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
@Component
@RequiredArgsConstructor
public class EntityValidator {

    private final Validator validator;

    public  <T>void validateEntity(T entity) {
        Set<ConstraintViolation<T>> violations = validator.validate(entity);
        if (!violations.isEmpty()) throw new ValidationException(violations);
    }

}
