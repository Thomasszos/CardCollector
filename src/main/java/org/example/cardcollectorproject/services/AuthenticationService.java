// AuthenticationService.java
package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.exceptions.AuthenticationException;
import org.example.cardcollectorproject.models.User;

public class AuthenticationService {
    private final CosmosDbService dbService;
    private final UserSession userSession;

    public AuthenticationService() {
        this.dbService = new CosmosDbService();
        this.userSession = UserSession.getInstance();
    }

    public void login(String username, String password) throws AuthenticationException {
        if (username == null || username.trim().isEmpty()) {
            throw new AuthenticationException("Username cannot be empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new AuthenticationException("Password cannot be empty");
        }

        // Use the username to fetch the user document
        User user = dbService.getUserByUsername(username); // Query using username
        if (user == null) {
            throw new AuthenticationException("User not found");
        }

        // Verify that the password matches the stored passwordHash
        if (!user.verifyPassword(password)) {
            throw new AuthenticationException("Invalid password");
        }

        // Set the current user in the session
        userSession.setCurrentUser(user);
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

        // Check if user already exists by username
        if (dbService.getUserByUsername(username) != null) {
            throw new AuthenticationException("Username already taken");
        }

        // Create and save the new user with a unique ID
        String uniqueId = java.util.UUID.randomUUID().toString();
        User newUser = new User(username, email, password);
        newUser.setId(uniqueId);
        newUser.setUserId(uniqueId);

        // Insert the user into Cosmos DB
        dbService.createUser(newUser);

        // Set the current user in the session (optional)
        userSession.setCurrentUser(newUser);
    }

//    public static void main(String[] args) {
//        AuthenticationService authService = new AuthenticationService();
//
//        // Sign Up a Test User
//        try {
//            authService.signUp("testUser@1am", "testPassword", "test@example.com");
//            System.out.println("Sign Up Successful!");
//        } catch (AuthenticationException e) {
//            System.err.println("Sign Up Failed: " + e.getMessage());
//        }
//
//        // Login the Test User
//        try {
//            authService.login("testUser", "testPassword");
//            System.out.println("Login Successful!");
//        } catch (AuthenticationException e) {
//            System.err.println("Login Failed: " + e.getMessage());
//        }
//    }
}
