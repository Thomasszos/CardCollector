// AuthenticationService.java
package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.exceptions.AuthenticationException;

public class AuthenticationService {
    public void login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }
        // Add actual authentication logic here
    }

    public void signUp(String username, String password, String email) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("Email cannot be empty");
        }
        // Add actual signup logic here
    }
}
