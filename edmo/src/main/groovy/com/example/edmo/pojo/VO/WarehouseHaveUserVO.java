package com.example.edmo.pojo.VO;


import com.example.edmo.pojo.entity.User;
import com.example.edmo.pojo.entity.Warehouse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseHaveUserVO {
    private Integer id;

    private String name;

    private String description;

    private List<User> users;

    public WarehouseHaveUserVO(Warehouse warehouse, List<User> users) {
        this.id = warehouse.getId();
        this.name = warehouse.getName();
        this.description = warehouse.getDescription();
        this.users = users;
    }
}
