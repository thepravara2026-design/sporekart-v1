package com.sporekart.modules.security.application.dto;

import com.sporekart.modules.security.domain.UserAccount;

public class UserProfileDto {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String status;

    public UserProfileDto() {}

    public UserProfileDto(UserAccount user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole().name();
        this.status = user.getStatus().name();
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getRole() { return role; }
    public String getStatus() { return status; }
}
