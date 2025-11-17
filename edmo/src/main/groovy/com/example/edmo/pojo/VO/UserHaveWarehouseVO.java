package com.example.edmo.pojo.VO;

import com.example.edmo.pojo.entity.User;
import com.example.edmo.pojo.entity.Warehouse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UserHaveWarehouseVO {
    private Integer id;

    private String name;

    private String email;

    private String phone;

    @JsonIgnore  // 输出时忽略（对象→JSON）
    private String password;

    private Integer sex;

    private Integer age;

    private Integer roleId;

    private List<Warehouse> managedWarehouse;

    public UserHaveWarehouseVO(User user, List<Warehouse> managedWarehouse) {
        this.id = user.getId();
        this.name = user.getName();
        this.email = user.getEmail();
        this.phone = user.getPhone();
        this.password = user.getPassword();
        this.sex = user.getSex();
        this.age = user.getAge();
        this.roleId = user.getRoleId();
        this.managedWarehouse = managedWarehouse;
    }
}
