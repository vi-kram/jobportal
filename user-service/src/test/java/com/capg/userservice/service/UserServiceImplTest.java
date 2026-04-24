package com.capg.userservice.service;

import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;
import com.capg.userservice.entity.Role;
import com.capg.userservice.entity.User;
import com.capg.userservice.exception.EmailAlreadyExistsException;
import com.capg.userservice.exception.InvalidCredentialsException;
import com.capg.userservice.exception.UnauthorizedException;
import com.capg.userservice.exception.UserNotFoundException;
import com.capg.userservice.mapper.UserMapper;
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

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User buildUser(Long id, String email, Role role, boolean active) {
        return new User(id, "John Doe", email, "encodedPassword", role, active,
                LocalDateTime.now(), LocalDateTime.now());
    }

    private UserResponse buildResponse(Long id, String email, Role role) {
        return new UserResponse(id, "John Doe", email, role);
    }

    // registerUser tests

    @Test
    void registerUser_success() {
        UserRegisterRequest request = new UserRegisterRequest(
                "John Doe", "john@example.com", "password123", Role.JOB_SEEKER);

        User savedUser = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        UserResponse expected = buildResponse(1L, "john@example.com", Role.JOB_SEEKER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userMapper.toEntity(request)).thenReturn(savedUser);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(expected);

        UserResponse response = userService.registerUser(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(Role.JOB_SEEKER, response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_emailAlreadyExists_throwsException() {
        UserRegisterRequest request = new UserRegisterRequest(
                "John Doe", "john@example.com", "password123", Role.JOB_SEEKER);

        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> userService.registerUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // loginUser tests

    @Test
    void loginUser_success() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "password123");
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("john@example.com", "JOB_SEEKER")).thenReturn("mocked.jwt.token");

        String token = userService.loginUser(request);

        assertEquals("mocked.jwt.token", token);
        verify(jwtUtil).generateToken("john@example.com", "JOB_SEEKER");
    }

    @Test
    void loginUser_inactiveAccount_throwsException() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "password123");
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, false);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(request));
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginUser_wrongPassword_throwsException() {
        UserLoginRequest request = new UserLoginRequest("john@example.com", "wrongPassword");
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(request));
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    @Test
    void loginUser_userNotFound_throwsException() {
        UserLoginRequest request = new UserLoginRequest("unknown@example.com", "password123");
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> userService.loginUser(request));
        verify(jwtUtil, never()).generateToken(anyString(), anyString());
    }

    // getUserById tests

    @Test
    void getUserById_ownProfile_success() {
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        UserResponse expected = buildResponse(1L, "john@example.com", Role.JOB_SEEKER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse response = userService.getUserById(1L, "john@example.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getId());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_adminCanViewAny_success() {
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        UserResponse expected = buildResponse(1L, "john@example.com", Role.JOB_SEEKER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(expected);

        UserResponse response = userService.getUserById(1L, "admin@example.com", "ADMIN");

        assertNotNull(response);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_otherUserProfile_throwsUnauthorized() {
        User user = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class,
                () -> userService.getUserById(1L, "other@example.com", "JOB_SEEKER"));
    }

    @Test
    void getUserById_notFound_throwsException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(99L, "john@example.com", "JOB_SEEKER"));
    }

    // updateUser tests

    @Test
    void updateUser_ownProfile_success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");
        request.setPassword("newPassword123");

        User existingUser = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        User updatedUser = new User(1L, "Updated Name", "john@example.com",
                "newEncodedPassword", Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now());
        UserResponse expected = buildResponse(1L, "john@example.com", Role.JOB_SEEKER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expected);

        UserResponse response = userService.updateUser(1L, request, "john@example.com", "JOB_SEEKER");

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_otherUserProfile_throwsUnauthorized() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        User existingUser = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));

        assertThrows(UnauthorizedException.class,
                () -> userService.updateUser(1L, request, "other@example.com", "JOB_SEEKER"));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_adminCanUpdateAny_success() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        User existingUser = buildUser(1L, "john@example.com", Role.JOB_SEEKER, true);
        User updatedUser = new User(1L, "Updated Name", "john@example.com",
                "encodedPassword", Role.JOB_SEEKER, true, LocalDateTime.now(), LocalDateTime.now());
        UserResponse expected = buildResponse(1L, "john@example.com", Role.JOB_SEEKER);

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        when(userMapper.toResponse(updatedUser)).thenReturn(expected);

        UserResponse response = userService.updateUser(1L, request, "admin@example.com", "ADMIN");

        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_notFound_throwsException() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated Name");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.updateUser(99L, request, "john@example.com", "JOB_SEEKER"));

        verify(userRepository, never()).save(any(User.class));
    }
}
