package com.example.edmo.pojo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
public class PageDTO {
    static int page_Size = 20;
    static int page_Num = 1;

    int pageSize = page_Size;
    int pageNum = page_Num;

    private HashMap param;


    public PageDTO() {
        this.param = new HashMap<>();
    }

}
