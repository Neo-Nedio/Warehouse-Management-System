package com.example.edmo.pojo.DTO;


import com.example.edmo.annotation.Phone;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Email(message = ValidationConstant.EMAIL)
    private String email;

    @Phone
    private String phone;

    @Size(min =2,max = 10,message = ValidationConstant.PASSWORD)
    private String password;

    @Min(value = 0,message = ValidationConstant.SEX)
    @Max(value = 1,message = ValidationConstant.SEX)
    private Integer sex;

    @Min(value = 16,message = ValidationConstant.AGE_MIN)
    @Max(value = 120,message = ValidationConstant.AGE_MAX)
    private Integer age;

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
