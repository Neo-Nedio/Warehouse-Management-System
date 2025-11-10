package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.UserDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")  // MyBatis-Plus 表名注解
public class User {

    @TableId(type = IdType.AUTO)  // MyBatis-Plus 主键注解
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("email")
    private String email;

    @TableField("password")
    @JsonIgnore  // 输出时忽略（对象→JSON）
    private String password;

    @TableField("sex")
    private Integer sex;

    @TableField("age")
    private Integer age;

    @TableField("roleId")
    private Integer roleId;

    @TableField(exist = false)
    @JsonIgnore
    private List<Integer> managedWarehouseIds;

    //queryUser 封装来把密码放入User中，直接传User会导致密码没有被实例化
    public User(UserDTO userDTO) {
        this.name = userDTO.getName();
        this.email = userDTO.getEmail();
        this.password = userDTO.getPassword();
        this.sex= userDTO.getSex();
        this.age= userDTO.getAge();
        this.roleId= userDTO.getRoleId();
    }

    //用于需要传入id
    public User(UserDTO userDTO, Integer id) {
        this.name = userDTO.getName();
        this.email = userDTO.getEmail();
        this.password = userDTO.getPassword();
        this.sex= userDTO.getSex();
        this.age= userDTO.getAge();
        this.roleId= userDTO.getRoleId();
        this.id=id;
    }
}
