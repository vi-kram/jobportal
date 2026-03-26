package com.capg.userservice.service;

import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;
import com.capg.userservice.entity.Role;
import com.capg.userservice.entity.User;
import com.capg.userservice.exception.EmailAlreadyExistsException;
import com.capg.userservice.exception.UserNotFoundException;
import com.capg.userservice.repository.UserRepository;
import com.capg.userservice.service.impl.UserServiceImpl;
import com.capg.userservice.util.JwtUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private UserServiceImpl userService;

    // registerUser tests

    @Test
    void registerUser_success() {
        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "John Doe", "john@example.com", "password123", Role.JOB_SEEKER
        );

        User savedUser = new User(
                1L, "John Doe", "john@example.com", "encodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        UserResponse response = userService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.JOB_SEEKER, response.getRole());

        verify(userRepository).existsByEmail("john@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_emailAlreadyExists_throwsException() {
        // Arrange
        UserRegisterRequest request = new UserRegisterRequest(
                "John Doe", "john@example.com", "password123", Role.JOB_SEEKER
        );

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        EmailAlreadyExistsException ex = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.registerUser(request)
        );

        assertEquals("Email already exists", ex.getMessage());
        verify(userRepository).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
    }

    // loginUser tests

    @Test
    void loginUser_success() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("john@example.com", "password123");

        User user = new User(
                1L, "John Doe", "john@example.com", "encodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com", "JOB_SEEKER")).thenReturn("mocked.jwt.token");

        // Act
        String token = userService.loginUser(request);

        // Assert
        assertEquals("mocked.jwt.token", token);
        verify(userRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "encodedPassword");
        verify(jwtUtil).generateToken("john@example.com", "JOB_SEEKER");
    }

    @Test
    void loginUser_userNotFound_throwsException() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("unknown@example.com", "password123");

        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(userRepository).findByEmail("unknown@example.com");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginUser_wrongPassword_throwsException() {
        // Arrange
        UserLoginRequest request = new UserLoginRequest("john@example.com", "wrongPassword");

        User user = new User(
                1L, "John Doe", "john@example.com", "encodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.loginUser(request)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(passwordEncoder).matches("wrongPassword", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    // getUserById tests

    @Test
    void getUserById_success() {
        // Arrange
        User user = new User(
                1L, "John Doe", "john@example.com", "encodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_notFound_throwsException() {
        // Arrange
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(99L)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userRepository).findById(99L);
    }

    // updateUser tests

    @Test
    void updateUser_success() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setPassword("newPassword123");

        User existingUser = new User(
                1L, "John Doe", "john@example.com", "encodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        User updatedUser = new User(
                1L, "Updated Name", "john@example.com", "newEncodedPassword",
                Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now()
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // Act
        UserResponse response = userService.updateUser(1L, request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Name", response.getName());
        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_userNotFound_throwsException() {
        // Arrange
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException ex = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(99L, request)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userRepository).findById(99L);
        verify(userRepository, never()).save(any(User.class));
    }
}
