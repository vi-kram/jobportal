package com.capg.userservice.service;

import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;

public interface UserService {

    UserResponse registerUser(UserRegisterRequest request);

    String loginUser(UserLoginRequest request);

    UserResponse getUserById(Long id, String email, String role);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(Long id, UpdateUserRequest request, String email, String role);
}