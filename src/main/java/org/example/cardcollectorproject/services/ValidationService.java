package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.exceptions.ValidationException;

public class ValidationService {
    public boolean isValidUsername(String username) {
        return username != null && !username.trim().isEmpty();
    }

    public boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    public boolean doPasswordsMatch(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    // Keep the original method for backward compatibility if needed
    public void validateSignUpData(String username, String password, String confirmPassword, String email)
            throws ValidationException {
        if (!isValidUsername(username)) {
            throw new ValidationException("Username is required");
        }
        if (!isValidPassword(password)) {
            throw new ValidationException("Password must be at least 8 characters long");
        }
        if (!doPasswordsMatch(password, confirmPassword)) {
            throw new ValidationException("Passwords do not match");
        }
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format");
        }
    }
}