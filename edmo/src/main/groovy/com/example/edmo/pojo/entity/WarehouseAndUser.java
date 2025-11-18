package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.WarehouseAndUserDTO;
import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("warehouse_and_user")
public class WarehouseAndUser {
    @TableId(type = IdType.AUTO)
    @Positive(message = ValidationConstant.ID)
    private Integer Id;

    @TableField("warehouse_id")
    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @TableField("user_id")
    @Positive(message = ValidationConstant.ID)
    private Integer userId;

    public WarehouseAndUser(WarehouseAndUserDTO warehouseAndUserDTO) {
        this.warehouseId = warehouseAndUserDTO.getWarehouseId();
        this.userId = warehouseAndUserDTO.getUserId();
        this.Id = warehouseAndUserDTO.getId();
    }
}
