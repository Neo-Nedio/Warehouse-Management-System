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
public class GoodsDTO {
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Positive(message = ValidationConstant.PRICE)
    private Integer price;

    @Positive(message = ValidationConstant.number)
    private Integer number;

    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime createTime;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)

    private String createUser;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime updateTime;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String updateUser;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime startCreateTime;
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime endCreateTime;

    @Past(message = ValidationConstant.TIME)
    private LocalDateTime startUpdateTime;
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime endUpdateTime;
}
