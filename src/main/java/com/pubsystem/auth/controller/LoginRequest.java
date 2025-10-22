package com.pubsystem.auth.controller;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
class LoginRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}