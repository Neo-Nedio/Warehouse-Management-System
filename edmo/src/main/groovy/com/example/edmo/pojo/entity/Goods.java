package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("goods")
public class Goods {
    @TableId(type = IdType.AUTO)
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @TableField("name")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @TableField("price")
    @Positive(message = ValidationConstant.PRICE)
    private Integer price;

    @TableField("number")
    @Positive(message = ValidationConstant.number)
    private Integer number;

    @TableField("warehouse_id")
    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @TableField("create_time")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime createTime;

    @TableField("create_user")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String createUser;

    @TableField("update_time")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime updateTime;

    @TableField("update_user")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String updateUser;

    @TableField("status")
    private Integer status;

    public Goods(GoodsDTO goodsDTO) {
        this.id = goodsDTO.getId();
        this.name = goodsDTO.getName();
        this.price = goodsDTO.getPrice();
        this.number = goodsDTO.getNumber();
        this.warehouseId = goodsDTO.getWarehouseId();
        this.createTime = goodsDTO.getCreateTime();
        this.createUser = goodsDTO.getCreateUser();
        this.updateTime = goodsDTO.getUpdateTime();
        this.updateUser = goodsDTO.getUpdateUser();
    }
}
