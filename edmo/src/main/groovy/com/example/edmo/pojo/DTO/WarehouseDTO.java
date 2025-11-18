package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDTO {
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Size(min =4,max = 30,message = ValidationConstant.DESCRIPTION)
    private String description;
}
