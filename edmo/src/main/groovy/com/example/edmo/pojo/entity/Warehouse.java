package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.WarehouseDTO;
import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("warehouse")
public class Warehouse {
    @TableId(type = IdType.AUTO)// MyBatis-Plus 主键注解
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @TableField("name")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @TableField("description")
    @Size(min =4,max = 30,message = ValidationConstant.DESCRIPTION)
    private String description;

    public Warehouse(WarehouseDTO warehouseDTO) {
        this.id = warehouseDTO.getId();
        this.name = warehouseDTO.getName();
        this.description = warehouseDTO.getDescription();
    }
}
