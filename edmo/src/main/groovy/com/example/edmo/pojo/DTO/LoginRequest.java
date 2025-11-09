package com.example.edmo.pojo.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
public class LoginRequest {
    private String email;
    private String password;
    private int code;
}
