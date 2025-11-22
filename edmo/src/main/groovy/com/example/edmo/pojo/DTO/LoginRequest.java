package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Configuration;

@Schema(description = "登录请求参数，用于密码登录、验证码登录和更新密码")
@Data
@Configuration
@NoArgsConstructor
public class LoginRequest {
    @Schema(description = "用户邮箱（必须是已注册的邮箱）", required = true, example = "user@example.com")
    @Email(message = ValidationConstant.EMAIL)
    private String email;

    @Schema(description = "用户密码（2-10字符），用于密码登录和更新密码", example = "123456")
    @Size(min =2,max = 10,message = ValidationConstant.PASSWORD)
    private String password;

    @Schema(description = "验证码（整数），用于验证码登录和更新密码。验证码有效期为2分钟", example = "123456")
//    @Min(value = 100000,message =  ValidationConstant.CODE)
//    @Max(value = 999999,message = ValidationConstant.CODE)
    private int code;
}
