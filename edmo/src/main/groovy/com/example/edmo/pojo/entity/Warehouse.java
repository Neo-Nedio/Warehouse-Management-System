package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.QueryWarehouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)  // MyBatis-Plus 主键注解
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    public Warehouse(QueryWarehouse queryWarehouse) {
        this.id = queryWarehouse.getId();
        this.name = queryWarehouse.getName();
        this.description = queryWarehouse.getDescription();
    }
}
