package com.sporekart.modules.security.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Valid email address required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    private String deviceInfo;

    public LoginRequestDto() {}

    public LoginRequestDto(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginRequestDto(String email, String password, String deviceInfo) {
        this.email = email;
        this.password = password;
        this.deviceInfo = deviceInfo;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
}
