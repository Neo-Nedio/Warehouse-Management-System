package com.example.edmo.controller;

import com.example.edmo.exception.OperationLogException;
import com.example.edmo.pojo.DTO.OperationLogDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.VO.UserHavaOperationVO;
import com.example.edmo.pojo.entity.OperationLog;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.service.Interface.OperationLogService;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.OperateTypeConstant;
import com.example.edmo.util.Constant.ValidationConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Tag(name = "操作日志管理", description = "操作日志相关接口，包括日志查询、按条件筛选等（需要管理员权限）。操作类型：1-入库、2-修改信息、3-更换仓库、4-出库")
@Validated
@RestController
@RequestMapping("/log")
public class LogAdminController {
    @Resource
    OperationLogService operationLogService;

    @Resource
    UserService userService;

    @Operation(summary = "查询所有日志", description = "获取所有操作日志列表，按id降序排列。返回字段：id、operateType（操作类型）、goodsId（商品ID）、goodsName（商品名称，2-10字符）、formerWarehouseId（原仓库ID）、newWarehouseId（新仓库ID）、updateTime（操作时间）、updateUser（操作人，2-10字符）")
    @GetMapping("/findAll")
    public Result findAll(){
        return Result.success(operationLogService.list());
    }

    @Operation(summary = "根据类型查询日志", description = "根据操作类型查询日志，按id降序排列。操作类型：1-入库、2-修改信息、3-更换仓库、4-出库。类型值必须在1-4之间")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "操作类型错误（不在1-4范围内）")
    })
    @GetMapping("/findAllByType/{type}")
    public Result findAllByType(@Parameter(description = "操作类型（1-4）：1-入库、2-修改信息、3-更换仓库、4-出库", required = true, example = "1")
                                @PathVariable Integer type){
        String typename = validateAndConvertType(type);
        return Result.success(operationLogService.findByType(typename));

    }

    @Operation(summary = "分页查询指定类型日志", description = "根据操作类型分页查询日志，按id降序排列。PageDTO参数：pageSize（默认20）、pageNum（默认1）、param（HashMap，必须包含type字段，值为1-4）。操作类型：1-入库、2-修改信息、3-更换仓库、4-出库")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功，返回日志列表"),
            @ApiResponse(responseCode = "400", description = "操作类型错误（不在1-4范围内）")
    })
    @PostMapping("/findAllByTypeAndPage")
    public Result findAllByTypeAndPage(@RequestBody PageDTO pageDTO){
        Integer type=(Integer) pageDTO.getParam().get("type");
        String typename = validateAndConvertType(type);
        return Result.success(operationLogService.findByType(typename,pageDTO).getRecords());
    }

    @Operation(summary = "根据类型和仓库ID查询日志", description = "根据操作类型和仓库ID查询日志。查询条件：operate_type匹配且（former_warehouse_id或new_warehouse_id匹配仓库ID）。操作类型：1-入库、2-修改信息、3-更换仓库、4-出库。类型值必须在1-4之间")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "400", description = "操作类型错误（不在1-4范围内）或仓库ID无效")
    })
    @GetMapping("/findAllByTypeAndWarehouseId")
    public Result findAllByTypeAndWarehouseId(@Parameter(description = "操作类型（1-4）：1-入库、2-修改信息、3-更换仓库、4-出库", example = "1")
                                               @RequestParam Integer type,
                                               @Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                                               @Positive(message = ValidationConstant.ID) Integer warehouseId ){
        String typename = validateAndConvertType(type);
        return Result.success(operationLogService.findAllByTypeAndWarehouseId(typename,warehouseId));
    }


    @Operation(summary = "根据仓库ID查询日志", description = "查询指定仓库的所有操作日志。查询条件：former_warehouse_id或new_warehouse_id匹配仓库ID（即该仓库作为原仓库或新仓库的所有操作记录）")
    @GetMapping("/findByWarehouseId/{warehouseId}")
    public Result findByWarehouseId(@Parameter(description = "仓库ID（必须大于0）", required = true, example = "1")
                                   @PathVariable @Positive(message = ValidationConstant.ID) Integer warehouseId){
        return Result.success(operationLogService.findByWarehouseId(warehouseId));
    }

    @Operation(summary = "根据用户名查询日志", description = "根据操作人姓名查询操作日志。先根据用户名模糊查询用户列表，然后查询每个用户的所有操作日志。返回List<UserHavaOperationVO>，每个元素包含用户信息（id、name）和该用户的操作日志列表。操作人名称长度2-10字符")
    @GetMapping("/findByUpdateName/{name}")
    public Result findByUpdateName(@Parameter(description = "用户名（支持模糊匹配）", required = true, example = "张三")
                                   @PathVariable String name){
        List<User> users = userService.findUsersByNameLike(name);

        List<UserHavaOperationVO> userHavaOperationVOS = new ArrayList<>();
        for(User user:users){
            List<OperationLog> operationLogs = operationLogService.findByUpdateName(user.getName());
            userHavaOperationVOS.add(new UserHavaOperationVO(user,operationLogs));
        }
        return Result.success(userHavaOperationVOS);
    }

    @Operation(summary = "根据商品名称查询日志", description = "根据商品名称模糊查询相关操作日志。商品名称长度2-10字符")
    @GetMapping("/findByGoodsName")
    public Result findByGoodsName(@Parameter(description = "商品名称（支持模糊匹配）", required = true, example = "商品A")
                                  @RequestParam String name){
        return Result.success(operationLogService.findByGoodsName(name));
    }

    @Operation(summary = "根据商品ID查询日志", description = "根据商品ID精确查询相关操作日志，返回该商品的所有操作记录")
    @GetMapping("/findByGoodsId/{goodsId}")
    public Result findByGoodsId(@Parameter(description = "商品ID（必须大于0）", required = true, example = "1")
                                @PathVariable @Positive(message = ValidationConstant.ID) Integer goodsId){
        return Result.success(operationLogService.findByGoodsId(goodsId));
    }

    @Operation(summary = "任意条件查询日志", description = "根据多个条件组合查询操作日志，按id降序排列。OperationLogDTO查询条件：id（精确匹配）、operateType（精确匹配，支持数字1-4或类型名称：入库、修改信息、更换仓库、出库）、goodsId（精确匹配）、goodsName（模糊匹配，2-10字符）、formerWarehouseId（精确匹配）、newWarehouseId（精确匹配）、updateUser（模糊匹配，2-10字符）、startTime/endTime（update_time时间范围，必须都是过去时间）。所有条件都是可选的，可以任意组合。operateType如果为空字符串或null，则忽略该条件")
    @PostMapping("/findByAnyCondition")
    public Result findByAnyCondition(@Valid @RequestBody OperationLogDTO operationLogDTO) {
        //todo 检查 operateType 是否为空或空字符串
        if (operationLogDTO.getOperateType() == null || operationLogDTO.getOperateType().trim().isEmpty()) {
            // 如果没有操作类型，直接使用 DTO 中的其他条件查询
            return Result.success(operationLogService.findByAnyCondition(operationLogDTO));
        }
        
        // 如果有操作类型，转换为对应的类型名称
        try {
            int type = Integer.parseInt(operationLogDTO.getOperateType());
            String typename = validateAndConvertType(type);
            operationLogDTO.setOperateType(typename);
        } catch (NumberFormatException e) {
            // 如果无法解析为整数，可能是已经是类型名称（如 "ADD"），直接使用
            // 不做转换
        }

        return Result.success(operationLogService.findByAnyCondition(operationLogDTO));
    }

    @Operation(summary = "根据时间范围查询日志", description = "根据开始时间和结束时间查询操作日志（基于update_time字段）。查询条件：update_time >= startTime 且 update_time <= endTime。startTime和endTime都必须是过去时间")
    @GetMapping("/findByTime")
    public Result findByTime(@Parameter(description = "开始时间（必须是过去时间）", required = true, example = "2024-01-01T00:00:00")
                             @RequestParam @Past(message = ValidationConstant.TIME) LocalDateTime startTime,
                             @Parameter(description = "结束时间（必须是过去时间）", required = true, example = "2024-12-31T23:59:59")
                             @Past(message = ValidationConstant.TIME) LocalDateTime endTime){
        return Result.success(operationLogService.findByTime(startTime,endTime));
    }

    /**
     * 验证操作类型并转换为类型名称
     * @param type 操作类型（1-4）
     * @return 操作类型名称
     * @throws OperationLogException 如果类型不在1-4范围内
     */
    private String validateAndConvertType(Integer type) {
        if (type == null || type < 1 || type > 4) {
            throw new OperationLogException(CodeConstant.operationLog, OperateTypeConstant.FALSE_TYPE);
        }
        return getOperateTypeByNumber(type);
    }

    public String getOperateTypeByNumber(int number) {
        return switch (number) {
            case 1 -> OperateTypeConstant.ADD;
            case 2 -> OperateTypeConstant.MOD_MESSAGE;
            case 3 -> OperateTypeConstant.MOD_WAREHOUSE;
            case 4 -> OperateTypeConstant.OPERATE_TYPE_DELETE;
            default -> null;
        };
    }
}
