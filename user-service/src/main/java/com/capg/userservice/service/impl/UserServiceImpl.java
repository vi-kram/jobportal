package com.capg.userservice.service.impl;

import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;
import com.capg.userservice.entity.User;
import com.capg.userservice.exception.EmailAlreadyExistsException;
import com.capg.userservice.exception.InvalidCredentialsException;
import com.capg.userservice.exception.UnauthorizedException;
import com.capg.userservice.exception.UserNotFoundException;
import com.capg.userservice.repository.UserRepository;
import com.capg.userservice.service.UserService;
import com.capg.userservice.util.JwtUtil;

import com.capg.userservice.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final String USER_NOT_FOUND = "User not found";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public UserResponse registerUser(UserRegisterRequest request) {

        log.info("Registering new user role={}", request.getRole());

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Registration failed - email already exists");
            throw new EmailAlreadyExistsException("Email already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("User registered successfully userId={}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    public String loginUser(UserLoginRequest request) {

        log.info("Login attempt");

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed - user not found");
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!user.isActive()) {
            log.warn("Login failed - account inactive");
            throw new InvalidCredentialsException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - wrong password");
            throw new InvalidCredentialsException("Invalid email or password");
        }

        log.info("Login successful role={}", user.getRole());
        return jwtUtil.generateToken(user.getEmail(), user.getRole().name());
    }
    
    

    @Override
    public UserResponse getUserByEmail(String email) {
        log.debug("Fetching user by email");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse getUserById(Long id, String email, String role) {
        log.debug("Fetching user by id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("User not found id={}", id);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (!user.getEmail().equals(email) && !role.equals("ADMIN")) {
            log.warn("Unauthorized profile access id={}", id);
            throw new UnauthorizedException("You can only view your own profile");
        }

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request, String email, String role) {

        log.info("Updating user id={}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed - user not found id={}", id);
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        if (!user.getEmail().equals(email) && !role.equals("ADMIN")) {
            log.warn("Unauthorized update attempt id={}", id);
            throw new UnauthorizedException("You can only update your own profile");
        }

        user.setName(request.getName());

        if (request.getMobile() != null) user.setMobile(request.getMobile());
        if (request.getSkills() != null) user.setSkills(request.getSkills());
        if (request.getHeadline() != null) user.setHeadline(request.getHeadline());

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user.setUpdatedAt(LocalDateTime.now());

        User updated = userRepository.save(user);
        log.info("User updated successfully userId={}", updated.getId());

        return userMapper.toResponse(updated);
    }
}