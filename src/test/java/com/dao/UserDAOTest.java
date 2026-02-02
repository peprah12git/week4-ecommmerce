package com.dao;

import com.models.User;
import org.junit.jupiter.api.*;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTest {
    private UserDAO userDAO;

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
    }

    @Test
    void testAddUser() {
        String uniqueEmail = "test" + UUID.randomUUID() + "@test.com";
        User user = new User("Test User", uniqueEmail, "password123", "1234567890", "Test Address");
        boolean result = userDAO.addUser(user);
        assertTrue(result);
        assertTrue(user.getUserId() > 0);
    }
// test retrieving inventory by poduct ID
    @Test
    void testGetUserById() {
        String uniqueEmail = "john" + UUID.randomUUID() + "@test.com";
        User user = new User("John Doe", uniqueEmail, "pass123", "9876543210", "123 Main St");
        userDAO.addUser(user);
        User retrieved = userDAO.getUserById(user.getUserId());
        assertNotNull(retrieved);
        assertEquals(user.getName(), retrieved.getName());
        assertEquals(user.getEmail(), retrieved.getEmail());
    }

    @Test
    void testGetUserByEmail() {
        String uniqueEmail = "jane" + UUID.randomUUID() + "@test.com";
        User user = new User("Jane Smith", uniqueEmail, "pass456", "5551234567", "456 Oak Ave");
        userDAO.addUser(user);
        User retrieved = userDAO.getUserByEmail(uniqueEmail);
        assertNotNull(retrieved);
        assertEquals(user.getName(), retrieved.getName());
    }

    @Test
    void testGetAllUsers() {
        var users = userDAO.getAllUsers();
        assertNotNull(users);
        assertTrue(users.size() >= 0);
    }

    @Test
    void testUpdateUser() {
        String uniqueEmail = "bob" + UUID.randomUUID() + "@test.com";
        User user = new User("Bob Johnson", uniqueEmail, "pass789", "5559876543", "789 Pine Rd");
        user.setRole("user");
        userDAO.addUser(user);
        user.setName("Robert Johnson");
        user.setPhone("5551111111");
        boolean updated = userDAO.updateUser(user);
        assertTrue(updated);
        User retrieved = userDAO.getUserById(user.getUserId());
        assertEquals("Robert Johnson", retrieved.getName());
        assertEquals("5551111111", retrieved.getPhone());
    }

    @Test
    void testDeleteUser() {
        String uniqueEmail = "temp" + UUID.randomUUID() + "@test.com";
        User user = new User("Temp User", uniqueEmail, "temp123", "5550000000", "Temp Address");
        userDAO.addUser(user);
        boolean deleted = userDAO.deleteUser(user.getUserId());
        assertTrue(deleted);
        assertNull(userDAO.getUserById(user.getUserId()));
    }

    @Test
    void testGetUserByEmailNotFound() {
        User user = userDAO.getUserByEmail("nonexistent@test.com");
        assertNull(user);
    }

    @Test
    void testGetUserByIdNotFound() {
        User user = userDAO.getUserById(999999);
        assertNull(user);
    }
}
