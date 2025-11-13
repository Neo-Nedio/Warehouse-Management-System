package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.GoodsDTO;
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
    private Integer id;

    @TableField("name")
    private String name;

    @TableField("price")
    private Integer price;

    @TableField("number")
    private Integer number;

    @TableField("warehouse_id")
    private Integer warehouseId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("create_user")
    private String createUser;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_user")
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
