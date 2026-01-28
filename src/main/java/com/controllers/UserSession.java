package com.controllers;

import com.ecommerce.models.User;

public class UserSession {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static int getCurrentUserId() {
        return currentUser != null ? currentUser.getUserId() : 0;
    }

    public static String getCurrentUserName() {
        return currentUser != null ? currentUser.getName() : "Guest";
    }

    public static void logout() {
        currentUser = null;
    }
}
