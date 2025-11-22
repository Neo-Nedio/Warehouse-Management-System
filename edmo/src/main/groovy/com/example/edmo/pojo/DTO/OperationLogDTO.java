package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Schema(description = "操作日志查询条件对象，用于任意条件查询操作日志（需要管理员权限）。所有字段都是可选的查询条件，可以任意组合")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogDTO {
    @Schema(description = "日志ID（精确匹配）", example = "1")
    @Positive(message = ValidationConstant.ID)
    Integer id;

    @Schema(description = "操作类型（精确匹配）。支持数字1-4（1-入库、2-修改信息、3-更换仓库、4-出库）或类型名称（入库、修改信息、更换仓库、出库）。如果为空字符串或null，则忽略该条件", example = "1")
    //不用检验，控制层已检验
    String operateType;

    @Schema(description = "商品ID（精确匹配）", example = "1")
    @Positive(message = ValidationConstant.ID)
    Integer goodsId;

    @Schema(description = "商品名称（模糊匹配，2-10字符）", example = "商品A")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    String goodsName;

    @Schema(description = "原仓库ID（精确匹配）", example = "1")
    @Positive(message = ValidationConstant.ID)
    Integer formerWarehouseId;

    @Schema(description = "新仓库ID（精确匹配）", example = "2")
    @Positive(message = ValidationConstant.ID)
    Integer newWarehouseId;

    @Schema(description = "操作时间（精确匹配，必须是过去时间）", example = "2024-01-01T00:00:00")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime updateTime;

    @Schema(description = "操作人（模糊匹配，2-10字符）", example = "张三")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String updateUser;

    @Schema(description = "操作时间范围查询-开始时间（必须是过去时间，基于update_time字段）", example = "2024-01-01T00:00:00")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime startTime;

    @Schema(description = "操作时间范围查询-结束时间（必须是过去时间，基于update_time字段）", example = "2024-12-31T23:59:59")
    @Past(message = ValidationConstant.TIME)
    private LocalDateTime endTime;
}
