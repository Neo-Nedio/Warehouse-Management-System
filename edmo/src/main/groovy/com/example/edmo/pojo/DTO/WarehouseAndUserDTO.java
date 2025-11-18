package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseAndUserDTO {
    @Positive(message = ValidationConstant.ID)
    private Integer Id;

    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @Positive(message = ValidationConstant.ID)
    private Integer userId;
}
