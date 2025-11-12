package com.example.edmo.pojo.VO;


import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsInWarehouseVO {
    private Integer id;

    private String name;

    private String description;

    List<Goods> goods;

}
