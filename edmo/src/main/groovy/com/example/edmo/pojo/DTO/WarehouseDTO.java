package com.example.edmo.pojo.DTO;

import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "仓库数据传输对象，用于仓库创建和修改（需要管理员权限）")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseDTO {
    @Schema(description = "仓库ID（修改时必填，创建时不需要提供）", example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Schema(description = "仓库名称（2-10字符）", required = true, example = "仓库A")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Schema(description = "仓库描述（4-30字符）", required = true, example = "这是仓库A的描述信息")
    @Size(min =4,max = 30,message = ValidationConstant.DESCRIPTION)
    private String description;
}
