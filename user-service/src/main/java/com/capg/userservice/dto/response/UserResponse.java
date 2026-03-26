//This class is a DTO (Data Transfer Object) used to send user data from your backend to the client.
// Why it exists
//You don’t send your actual database entity directly to the client.
//Instead, you:
// Convert entity → UserResponse → send to client
package com.capg.userservice.dto.response;

import com.capg.userservice.entity.Role;

public class UserResponse {

    private Long id;
    private String name;
    private String email;
    private Role role;

    public UserResponse() {}

    public UserResponse(Long id, String name, String email, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}