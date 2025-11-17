package com.example.edmo.pojo.DTO;


import com.example.edmo.annotation.Phone;
import com.example.edmo.pojo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    private Integer id;

    private String name;

    private String email;

    @Phone
    private String phone;

    private String password;

    private Integer sex;

    private Integer age;

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
