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

@Schema(description = "商品数据传输对象，用于商品创建、修改和查询。创建时：name、price、number、warehouseId必填；修改时：id必填；查询时：所有字段都是可选的查询条件")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoodsDTO {
    @Schema(description = "商品ID（修改和查询时使用，创建时不需要提供）", example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer id;

    @Schema(description = "商品名称（2-10字符），创建和修改时必填，查询时支持模糊匹配", required = true, example = "商品A")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String name;

    @Schema(description = "商品价格（正整数），创建和修改时必填，查询时精确匹配", required = true, example = "100")
    @Positive(message = ValidationConstant.PRICE)
    private Integer price;

    @Schema(description = "商品数量（正整数），创建和修改时必填，查询时精确匹配", required = true, example = "50")
    @Positive(message = ValidationConstant.number)
    private Integer number;

    @Schema(description = "仓库ID（正整数），创建时必填（必须是已存在的仓库且用户有管理权限），修改信息时不能提供（需要通过mod/warehouse接口修改），修改仓库时必填，查询时精确匹配", required = true, example = "1")
    @Positive(message = ValidationConstant.ID)
    private Integer warehouseId;

    @Schema(description = "创建时间（由后端自动填充，不需要提供）", example = "2024-01-01T00:00:00")
    // 这些字段由后端自动填充，不需要校验
    private LocalDateTime createTime;

    @Schema(description = "创建人（2-10字符，由后端自动填充，不需要提供）", example = "张三")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String createUser;

    @Schema(description = "更新时间（由后端自动填充，不需要提供）", example = "2024-01-01T00:00:00")
    // 这些字段由后端自动填充，不需要校验
    private LocalDateTime updateTime;

    @Schema(description = "更新人（2-10字符，由后端自动填充，不需要提供）", example = "张三")
    @Size(min =2,max = 10,message = ValidationConstant.NAME)
    private String updateUser;

    @Schema(description = "创建时间范围查询-开始时间（查询条件，允许查询未来时间范围）", example = "2024-01-01T00:00:00")
    // 查询条件字段，不需要@Past校验（允许查询未来时间范围）
    private LocalDateTime startCreateTime;

    @Schema(description = "创建时间范围查询-结束时间（查询条件，允许查询未来时间范围）", example = "2024-12-31T23:59:59")
    private LocalDateTime endCreateTime;

    @Schema(description = "更新时间范围查询-开始时间（查询条件，允许查询未来时间范围）", example = "2024-01-01T00:00:00")
    // 查询条件字段，不需要@Past校验（允许查询未来时间范围）
    private LocalDateTime startUpdateTime;

    @Schema(description = "更新时间范围查询-结束时间（查询条件，允许查询未来时间范围）", example = "2024-12-31T23:59:59")
    private LocalDateTime endUpdateTime;
}
