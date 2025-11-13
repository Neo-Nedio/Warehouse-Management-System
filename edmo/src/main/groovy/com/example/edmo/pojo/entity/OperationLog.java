package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.util.Constant.OperateTypeConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    Integer id;

    @TableField("operate_type")
    String operateType;

    @TableField("goods_id")
    Integer goodsId;

    @TableField("goods_name")
    String goodsName;

    @TableField("former_warehouse_id")
    Integer formerWarehouseId;

    @TableField("new_warehouse_id")
    Integer newWarehouseId;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("update_user")
    private String updateUser;

    public void addLog(GoodsDTO goodsDTO) {
        operateType = OperateTypeConstant.ADD;
        goodsId = goodsDTO.getId();
        goodsName = goodsDTO.getName();
        formerWarehouseId = goodsDTO.getWarehouseId();
        newWarehouseId = goodsDTO.getWarehouseId();
        updateTime = goodsDTO.getUpdateTime();
        updateUser = goodsDTO.getUpdateUser();
    }

    public void modMessage(GoodsDTO goodsDTO) {
        operateType = OperateTypeConstant.MOD_MESSAGE;
        goodsId = goodsDTO.getId();
        goodsName = goodsDTO.getName();
        formerWarehouseId = goodsDTO.getWarehouseId();
        newWarehouseId = goodsDTO.getWarehouseId();
        updateTime = goodsDTO.getUpdateTime();
        updateUser = goodsDTO.getUpdateUser();
    }

    public void modWarehouse(Goods goods,GoodsDTO goodsDTO) {
        operateType = OperateTypeConstant.MOD_WAREHOUSE;
        goodsId = goods.getId();
        goodsName = goods.getName();
        formerWarehouseId = goods.getWarehouseId();
        newWarehouseId = goodsDTO.getWarehouseId();
        updateTime = goodsDTO.getUpdateTime();
        updateUser = goodsDTO.getUpdateUser();
    }

    public void delete(Goods goods,GoodsDTO goodsDTO) {
        operateType = OperateTypeConstant.OPERATE_TYPE_DELETE;
        goodsId = goods.getId();
        goodsName = goods.getName();
        formerWarehouseId = goods.getWarehouseId();
        newWarehouseId = null;
        updateTime = goodsDTO.getUpdateTime();
        updateUser = goodsDTO.getUpdateUser();
    }
}
