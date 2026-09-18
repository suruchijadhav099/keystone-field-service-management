package com.zidio.keystone.controller;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        return Map.<String, Object>of(
                "email", authentication.getName(),
                "message", "User authenticated successfully"
        );
    }
}