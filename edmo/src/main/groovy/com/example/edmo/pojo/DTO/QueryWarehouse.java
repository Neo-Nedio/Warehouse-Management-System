package com.example.edmo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QueryWarehouse {
    private Integer id;

    private String name;

    private String description;
}
