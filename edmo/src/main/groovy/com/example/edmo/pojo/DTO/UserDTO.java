package com.example.edmo.pojo.DTO;


import com.example.edmo.annotation.Phone;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "用户数据传输对象，用于用户创建和修改（需要管理员权限）")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @Schema(description = "用户ID（修改时必填，创建时不需要提供）", example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Schema(description = "用户名（2-10字符）", required = true, example = "张三")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Schema(description = "用户邮箱（邮箱格式）", required = true, example = "user@example.com")
    @Email(message = ValidationConstant.EMAIL)
    private String email;

    @Schema(description = "用户手机号（手机号格式）", example = "13800138000")
    @Phone
    private String phone;

    @Schema(description = "用户密码（2-10字符，创建时必填，修改时如果提供会自动BCrypt加密，如果为空则不更新密码）", example = "123456")
    @Size(min =2,max = 10,message = ValidationConstant.PASSWORD)
    private String password;

    @Schema(description = "性别（0-女，1-男）", example = "1")
    @Min(value = 0,message = ValidationConstant.SEX)
    @Max(value = 1,message = ValidationConstant.SEX)
    private Integer sex;

    @Schema(description = "年龄（16-120）", example = "25")
    @Min(value = 16,message = ValidationConstant.AGE_MIN)
    @Max(value = 120,message = ValidationConstant.AGE_MAX)
    private Integer age;

    @Schema(description = "角色ID（1-3）", example = "2")
    @Min(value = 1,message = ValidationConstant.ROLE_MIN)
    @Max(value = 3,message = ValidationConstant.ROLE_MAX)
    private Integer roleId;

    public UserDTO(User queryUser) {
        this.id = queryUser.getId();
        this.name = queryUser.getName();
        this.email = queryUser.getEmail();
        this.phone = queryUser.getPhone();
        this.password = queryUser.getPassword();
        this.sex=queryUser.getSex();
        this.age=queryUser.getAge();
        this.roleId=queryUser.getRoleId();
    }
}
