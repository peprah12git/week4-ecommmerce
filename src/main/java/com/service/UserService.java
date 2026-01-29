package com.service;

import com.dao.UserDAO;
import com.models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static UserService instance;
    private UserDAO userDAO;
    private Map<Integer, User> userCache;
    private List<User> allUsersCache;
    private long lastCacheUpdate;
    private static final long CACHE_VALIDITY = 300000; // 5 minutes
    private User currentUser;

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public UserService() {
        this.userDAO = new UserDAO();
        this.userCache = new HashMap<>();
        this.allUsersCache = new ArrayList<>();
        this.lastCacheUpdate = 0;
    }

    public boolean addUser(User user) {
        boolean success = userDAO.addUser(user);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public List<User> getAllUsers() {
        long now = System.currentTimeMillis();

        if (!allUsersCache.isEmpty() && (now - lastCacheUpdate) < CACHE_VALIDITY) {
            System.out.println("✓ Users from cache");
            return new ArrayList<>(allUsersCache);
        }

        System.out.println("✗ Fetching users from database");
        allUsersCache = userDAO.getAllUsers();
        lastCacheUpdate = now;

        for (User u : allUsersCache) {
            userCache.put(u.getUserId(), u);
        }

        return new ArrayList<>(allUsersCache);
    }

    public User getUserById(int id) {
        if (userCache.containsKey(id)) {
            return userCache.get(id);
        }

        User user = userDAO.getUserById(id);
        if (user != null) {
            userCache.put(id, user);
        }
        return user;
    }

    public User getUserByEmail(String email) {
        return userDAO.getUserByEmail(email);
    }

    public boolean updateUser(User user) {
        boolean success = userDAO.updateUser(user);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    public boolean deleteUser(int id) {
        boolean success = userDAO.deleteUser(id);
        if (success) {
            invalidateCache();
        }
        return success;
    }

    // Returns the authenticated user or null
    public User authenticate(String email, String password) {
        User user = userDAO.getUserByEmail(email);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // Registration result wrapper
    public static class RegisterResult {
        private final boolean success;
        private final String message;
        public RegisterResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }

    // Register a new user
    public RegisterResult registerUser(String name, String email, String password, String phone, String address) {
        if (name == null || name.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            return new RegisterResult(false, "All fields are required.");
        }
        if (userDAO.getUserByEmail(email) != null) {
            return new RegisterResult(false, "Email already registered.");
        }
        User user = new User(name, email, password, phone, address);
        boolean added = userDAO.addUser(user);
        if (added) {
            return new RegisterResult(true, "Registration successful.");
        } else {
            return new RegisterResult(false, "Registration failed. Please try again.");
        }
    }

    public User authenticateAdmin(String email, String password) {
        User user = authenticate(email, password);
        if (user != null && "admin".equals(user.getRole())) {
            return user;
        }
        return null;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    private void invalidateCache() {
        userCache.clear();
        allUsersCache.clear();
        lastCacheUpdate = 0;
    }
}
