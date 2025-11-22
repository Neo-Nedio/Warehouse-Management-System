package com.example.edmo.pojo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Schema(description = "分页查询参数，用于各种分页查询接口")
@Data
@AllArgsConstructor
public class PageDTO {
    static int page_Size = 20;
    static int page_Num = 1;

    @Schema(description = "每页大小（默认20）", example = "20")
    int pageSize = page_Size;

    @Schema(description = "当前页码（默认1）", example = "1")
    int pageNum = page_Num;

    @Schema(description = "查询条件参数（HashMap），可包含name等字段用于模糊查询。不同接口支持的查询字段不同：用户查询支持name、仓库查询支持name、商品查询支持name等", example = "{\"name\":\"关键字\"}")
    private HashMap param;


    public PageDTO() {
        this.param = new HashMap<>();
    }

}
