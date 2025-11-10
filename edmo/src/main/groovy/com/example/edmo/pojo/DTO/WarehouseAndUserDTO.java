package com.example.edmo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseAndUserDTO {
    private Integer Id;

    private Integer warehouseId;

    private Integer userId;
}
