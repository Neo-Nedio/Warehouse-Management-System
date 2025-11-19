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
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Validated
@RestController
@RequestMapping("/log")
public class LogAdminController {
    @Resource
    OperationLogService operationLogService;

    @Resource
    UserService userService;

    @GetMapping("/findAll")
    public Result findAll(){
        return Result.success(operationLogService.list());
    }

    @GetMapping("/findAllByType/{type}")
    public Result findAllByType(@PathVariable Integer type){
        String typename;
        if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
        else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);
        return Result.success(operationLogService.findByType(typename));

    }

    @PostMapping("/findAllByTypeAndPage")
    public Result findAllByTypeAndPage(@RequestBody PageDTO pageDTO){
        Integer type=(Integer) pageDTO.getParam().get("type");
        String typename;
        if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
        else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);
        return Result.success(operationLogService.findByType(typename,pageDTO).getRecords());
    }

    @GetMapping("/findAllByTypeAndWarehouseId")
    public Result findAllByTypeAndWarehouseId(@RequestParam Integer type,
                                      @Positive(message = ValidationConstant.ID) Integer warehouseId ){
        String typename;
        if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
        else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);

        return Result.success(operationLogService.findAllByTypeAndWarehouseId(typename,warehouseId));
    }


    @GetMapping("/findByWarehouseId/{warehouseId}")
    public Result findByWarehouseId(@PathVariable @Positive(message = ValidationConstant.ID) Integer warehouseId){
        return Result.success(operationLogService.findByWarehouseId(warehouseId));
    }

    @GetMapping("/findByUpdateName/{name}")
    public Result findByUpdateName(@PathVariable String name){
        List<User> users = userService.findUsersByNameLike(name);

        List<UserHavaOperationVO> userHavaOperationVOS = new ArrayList<>();
        for(User user:users){
            List<OperationLog> operationLogs = operationLogService.findByUpdateName(user.getName());
            userHavaOperationVOS.add(new UserHavaOperationVO(user,operationLogs));
        }
        return Result.success(userHavaOperationVOS);
    }

    @GetMapping("/findByGoodsName")
    public Result findByGoodsName(@RequestParam String name){
        return Result.success(operationLogService.findByGoodsName(name));
    }

    @GetMapping("/findByGoodsId/{goodsId}")
    public Result findByGoodsId(@PathVariable @Positive(message = ValidationConstant.ID) Integer goodsId){
        return Result.success(operationLogService.findByGoodsId(goodsId));
    }

    @PostMapping("/findByAnyCondition")
    public Result findByAnyCondition(@Valid @RequestBody OperationLogDTO  operationLogDTO) {
        //todo 检查 operateType 是否为空或空字符串
        if (operationLogDTO.getOperateType() == null || operationLogDTO.getOperateType().trim().isEmpty()) {
            // 如果没有操作类型，直接使用 DTO 中的其他条件查询
            return Result.success(operationLogService.findByAnyCondition(operationLogDTO));
        }
        
        // 如果有操作类型，转换为对应的类型名称
        try {
            int type = Integer.parseInt(operationLogDTO.getOperateType());
            String typename;
            if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
            else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);
            operationLogDTO.setOperateType(typename);
        } catch (NumberFormatException e) {
            // 如果无法解析为整数，可能是已经是类型名称（如 "ADD"），直接使用
            // 不做转换
        }

        return Result.success(operationLogService.findByAnyCondition(operationLogDTO));
    }

    @GetMapping("/findByTime")
    public Result findByTime(@RequestParam @Past(message = ValidationConstant.TIME) LocalDateTime startTime,
                                           @Past(message = ValidationConstant.TIME) LocalDateTime endTime){
        return Result.success(operationLogService.findByTime(startTime,endTime));
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
