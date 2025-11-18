package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@NoArgsConstructor
public class LoginRequest {
    @Email(message = ValidationConstant.EMAIL)
    private String email;

    @Size(min =2,max = 10,message = ValidationConstant.PASSWORD)
    private String password;

//    @Min(value = 100000,message =  ValidationConstant.CODE)
//    @Max(value = 999999,message = ValidationConstant.CODE)
    private int code;
}
