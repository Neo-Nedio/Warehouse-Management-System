package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "仓库与用户关系数据传输对象，用于仓库与用户关系的创建和修改（需要管理员权限）")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseAndUserDTO {
    @Schema(description = "关系ID（修改时必填，创建时不需要提供）", example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer Id;

    @Schema(description = "仓库ID（必须是已存在的仓库）", required = true, example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @Schema(description = "用户ID（必须是已存在的用户）", required = true, example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer userId;
}
