package com.inspections.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validador para la anotación @ValidPassword.
 *
 * Política de contraseña:
 * - Mínimo 8 caracteres
 * - Al menos una mayúscula [A-Z]
 * - Al menos una minúscula [a-z]
 * - Al menos un dígito [0-9]
 * - Al menos un carácter especial [@#$%^&+=!.*_-]
 */
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            // @NotBlank se encarga de este caso
            return true;
        }

        StringBuilder errors = new StringBuilder();

        if (password.length() < 8) {
            errors.append("Debe tener al menos 8 caracteres. ");
        }
        if (!password.matches(".*[A-Z].*")) {
            errors.append("Debe incluir al menos una letra mayúscula. ");
        }
        if (!password.matches(".*[a-z].*")) {
            errors.append("Debe incluir al menos una letra minúscula. ");
        }
        if (!password.matches(".*\\d.*")) {
            errors.append("Debe incluir al menos un número. ");
        }
        if (!password.matches(".*[@#$%^&+=!.*_\\-].*")) {
            errors.append("Debe incluir al menos un carácter especial (@#$%^&+=!.*_-).");
        }

        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errors.toString().trim())
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
