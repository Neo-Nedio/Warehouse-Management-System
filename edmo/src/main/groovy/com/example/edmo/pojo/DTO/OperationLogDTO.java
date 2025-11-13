package com.example.edmo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogDTO {
    Integer id;

    String operateType;

    Integer goodsId;

    String goodsName;

    Integer formerWarehouseId;

    Integer newWarehouseId;

    private LocalDateTime updateTime;

    private String updateUser;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
