package com.example.edmo.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.util.Constant.OperateTypeConstant;
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
@TableName("operation_log")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    @Positive(message = ValidationConstant.ID)
    Integer id;

    @TableField("operate_type")
    String operateType;

    @TableField("goods_id")
    @Positive(message = ValidationConstant.ID)
    Integer goodsId;

    @TableField("goods_name")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    String goodsName;

    @TableField("former_warehouse_id")
    @Positive(message = ValidationConstant.ID)
    Integer formerWarehouseId;

    @TableField("new_warehouse_id")
    @Positive(message = ValidationConstant.ID)
    Integer newWarehouseId;

    @TableField("update_time")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime updateTime;

    @TableField("update_user")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
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
