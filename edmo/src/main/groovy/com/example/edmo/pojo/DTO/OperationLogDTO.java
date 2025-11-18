package com.example.edmo.pojo.DTO;

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
public class OperationLogDTO {
    @Positive(message = ValidationConstant.ID)
    Integer id;

    //不用检验，控制层已检验
    String operateType;

    @Positive(message = ValidationConstant.ID)
    Integer goodsId;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    String goodsName;

    @Positive(message = ValidationConstant.ID)
    Integer formerWarehouseId;

    @Positive(message = ValidationConstant.ID)
    Integer newWarehouseId;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime updateTime;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String updateUser;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime startTime;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime endTime;
}
