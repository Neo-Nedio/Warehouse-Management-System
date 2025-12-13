package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.annotation.Phone;
import com.example.edmo.pojo.DTO.UserDTO;
import com.example.edmo.util.Constant.ValidationConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@TableName("user")  // MyBatis-Plus 表名注解
public class User {

    @TableId(type = IdType.AUTO)// MyBatis-Plus 主键注解
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @TableField("name")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @TableField("email")
    @Email(message = ValidationConstant.EMAIL)
    private String email;

    @TableField("phone")
    @Phone
    private String phone;

    @TableField("password")
    @JsonIgnore  // 输出时忽略（对象→JSON）
    @Size(min =2,max = 10,message = ValidationConstant.PASSWORD)
    private String password;

    @TableField("sex")
    @Min(value = 0,message = ValidationConstant.SEX)
    @Max(value = 1,message = ValidationConstant.SEX)
    private Integer sex;

    @TableField("age")
    @Min(value = 16,message = ValidationConstant.AGE_MIN)
    @Max(value = 120,message = ValidationConstant.AGE_MAX)
    private Integer age;

    @TableField("roleId")
    @Min(value = 1,message = ValidationConstant.ROLE_MIN)
    @Max(value = 3,message = ValidationConstant.ROLE_MAX)
    private Integer roleId;

    @TableField(exist = false)
    private List<Integer> managedWarehouseIds =  new ArrayList<>();

    //开始时装配，防止出现空指针问题
    public User() {
        this.managedWarehouseIds = new ArrayList<>();
    }

    //queryUser 封装来把密码放入User中，直接传User会导致密码没有被实例化
    public User(UserDTO userDTO) {
        this.name = userDTO.getName();
        this.email = userDTO.getEmail();
        this.phone = userDTO.getPhone();
        this.password = userDTO.getPassword();
        this.sex= userDTO.getSex();
        this.age= userDTO.getAge();
        this.roleId= userDTO.getRoleId();
    }

    //用于需要传入id
    public User(UserDTO userDTO, Integer id) {
        this.name = userDTO.getName();
        this.email = userDTO.getEmail();
        this.phone = userDTO.getPhone();
        this.password = userDTO.getPassword();
        this.sex= userDTO.getSex();
        this.age= userDTO.getAge();
        this.roleId= userDTO.getRoleId();
        this.id=id;
    }
}
