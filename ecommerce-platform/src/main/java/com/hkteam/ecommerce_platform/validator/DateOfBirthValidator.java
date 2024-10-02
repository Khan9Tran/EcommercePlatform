package com.hkteam.ecommerce_platform.validator;

import java.time.LocalDate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateOfBirthValidator implements ConstraintValidator<ValidDateOfBirth, LocalDate> {

    @Override
    public void initialize(ValidDateOfBirth constraintAnnotation) {
        // not handled
    }

    @Override
    public boolean isValid(LocalDate dateOfBirth, ConstraintValidatorContext context) {
        if (dateOfBirth == null) {
            return true; // Allow null values, use @NotNull for non-null validation
        }
        return dateOfBirth.isBefore(LocalDate.now());
    }
}
