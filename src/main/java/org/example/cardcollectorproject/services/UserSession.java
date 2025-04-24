package org.example.cardcollectorproject.services;

import org.example.cardcollectorproject.models.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        this.currentUser = null;
    }
//
//    public boolean isLoggedIn() {
//        return currentUser != null;
//    }
}
