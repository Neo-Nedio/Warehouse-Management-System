package com.example.edmo.pojo.VO;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GoodsExportVO {
    @ExcelProperty(value = "商品ID", index = 0)
    private Integer id;

    @ExcelProperty(value = "商品名称", index = 1)
    private String name;

    @ExcelProperty(value = "价格", index = 2)
    private Integer price;

    @ExcelProperty(value = "数量", index = 3)
    private Integer number;

    @ExcelProperty(value = "仓库ID", index = 4)
    private Integer warehouseId;

    @ExcelProperty(value = "创建时间", index = 5)
    private LocalDateTime createTime;

    @ExcelProperty(value = "创建人", index = 6)
    private String createUser;

    @ExcelProperty(value = "更新时间", index = 7)
    private LocalDateTime updateTime;

    @ExcelProperty(value = "更新人", index = 8)
    private String updateUser;
}




