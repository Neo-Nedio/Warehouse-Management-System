package com.example.edmo.controller;

import com.example.edmo.exception.OperationLogException;
import com.example.edmo.pojo.DTO.OperationLogDTO;
import com.example.edmo.pojo.DTO.PageDTO;
import com.example.edmo.pojo.DTO.TimeDTO;
import com.example.edmo.pojo.VO.UserHavaOperationVO;
import com.example.edmo.pojo.entity.OperationLog;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.service.Interface.OperationLogService;
import com.example.edmo.service.Interface.UserService;
import com.example.edmo.util.Constant.CodeConstant;
import com.example.edmo.util.Constant.OperateTypeConstant;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @PostMapping("/findAllByTypeAndWarehouseId")
    public Result findAllByTypeAndWarehouseId(@RequestParam Integer type,
                                       Integer warehouseId ){
        String typename;
        if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
        else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);

        return Result.success(operationLogService.findAllByTypeAndWarehouseId(typename,warehouseId));
    }


    @GetMapping("/findByWarehouseId/{warehouseId}")
    public Result findByWarehouseId(@PathVariable Integer warehouseId){
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

    @PostMapping("/findByGoodsName")
    public Result findByGoodsName(@RequestParam String name){
        return Result.success(operationLogService.findByGoodsName(name));
    }

    @GetMapping("/findByGoodsId/{goodsId}")
    public Result findByGoodsId(@PathVariable Integer goodsId){
        return Result.success(operationLogService.findByGoodsId(goodsId));
    }

    @PostMapping("/findByAnyCondition")
    public Result findByAnyCondition(@RequestBody OperationLogDTO  operationLogDTO) {
        int type = Integer.parseInt(operationLogDTO.getOperateType());
        String typename;
        if(type>=1 && type<=4)  typename=getOperateTypeByNumber(type);
        else throw new OperationLogException(CodeConstant.operationLog,OperateTypeConstant.FALSE_TYPE);
        operationLogDTO.setOperateType(typename);

        return Result.success(operationLogService.findByAnyCondition(operationLogDTO));
    }

    @PostMapping("/findByTime")
    public Result findByTime(@RequestBody TimeDTO timeDTO){
        return Result.success(operationLogService.findByTime(timeDTO));
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
