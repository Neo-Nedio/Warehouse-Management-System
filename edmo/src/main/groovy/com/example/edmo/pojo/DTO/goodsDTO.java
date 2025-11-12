package com.example.edmo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class goodsDTO {
    private Integer id;

    private String name;

    private Integer price;

    private Integer number;

    private Integer warehouseId;

    private LocalDateTime createTime;

    private String createUser;

    private LocalDateTime updateTime;

    private String updateUser;
}
