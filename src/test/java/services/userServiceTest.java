package services;

import com.models.User;
import com.service.UserService;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class userServiceTest {
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = UserService.getInstance();
    }

    @Test
    void testRegisterUser() {
        var result = userService.registerUser("Test User", "test@test.com", "password123", "1234567890", "Test Address");
        assertNotNull(result);
    }

    @Test
    void testRegisterUserWithEmptyFields() {
        var result = userService.registerUser("", "test@test.com", "password", "123", "Address");
        assertFalse(result.isSuccess());
        assertEquals("All fields are required.", result.getMessage());
    }

    @Test
    void testAuthenticate() {
        userService.registerUser("Auth User", "auth@test.com", "pass123", "123", "Address");
        User user = userService.authenticate("auth@test.com", "pass123");
        assertNotNull(user);
    }

    @Test
    void testAuthenticateInvalidPassword() {
        userService.registerUser("User", "user@test.com", "correct", "123", "Address");
        User user = userService.authenticate("user@test.com", "wrong");
        assertNull(user);
    }

    @Test
    void testGetUserByEmail() {
        userService.registerUser("Email User", "email@test.com", "pass", "123", "Address");
        User user = userService.getUserByEmail("email@test.com");
        assertNotNull(user);
    }

    @Test
    void testSetAndGetCurrentUser() {
        User user = new User("Current", "current@test.com", "pass", "123", "Address");
        userService.setCurrentUser(user);
        assertEquals(user, userService.getCurrentUser());
    }
}
