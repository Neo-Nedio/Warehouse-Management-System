package com.example.edmo.pojo.DTO;


import com.example.edmo.pojo.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryUser {

    private Integer id;

    private String name;

    private String email;

    private String password;

    private Integer sex;

    private Integer age;

    private Integer roleId;

    public QueryUser(User queryUser) {
        this.name = queryUser.getName();
        this.email = queryUser.getEmail();
        this.password = queryUser.getPassword();
        this.sex=queryUser.getSex();
        this.age=queryUser.getAge();
        this.roleId=queryUser.getRoleId();
    }
}
