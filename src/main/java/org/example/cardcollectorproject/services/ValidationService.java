// ValidationService.java
package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.exceptions.ValidationException;

public class ValidationService {
    public void validateSignUpData(String username, String password, String confirmPassword, String email)
            throws ValidationException {
        validateEmail(email);
        validatePassword(password, confirmPassword);
        validateUsername(username);
    }

    private void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email is required");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        if (!email.matches(emailRegex)) {
            throw new ValidationException("Invalid email format");
        }
    }

    private void validatePassword(String password, String confirmPassword) throws ValidationException {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Password is required");
        }
        if (!password.equals(confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
    }

    private void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username is required");
        }
    }
}
