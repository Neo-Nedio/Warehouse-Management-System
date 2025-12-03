package com.example.edmo.listener;

import com.example.edmo.pojo.DTO.GoodsDTO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.service.Interface.OperationLogService;
import com.example.edmo.util.Constant.MqConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class LogListener {

    @Resource
    private OperationLogService operationLogService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.ADD_QUEUE),
                    exchange = @Exchange(name = MqConstant.LOG_EXCHANGE_DIRECT,type = ExchangeTypes.DIRECT),
                    key = {MqConstant.ADD_ROUTING_KEY}
            )
    )
    public void addLog(GoodsDTO goodsDTO) {
        try {
            operationLogService.addLog(goodsDTO);
        } catch (Exception e) {
            log.error("处理添加商品日志失败，商品信息: {}, 错误信息: {}", goodsDTO, e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.BATCH_ADD_QUEUE),
                    exchange = @Exchange(name = MqConstant.LOG_EXCHANGE_DIRECT,type = ExchangeTypes.DIRECT),
                    key = {MqConstant.BATCH_ADD_ROUTING_KEY}
            )
    )
    public void batchAddLog(List<GoodsDTO> goodsDTOList) {
        try {
            operationLogService.batchAddLog(goodsDTOList);
        } catch (Exception e) {
            log.error("处理批量添加商品日志失败，商品数量: {}, 错误信息: {}", 
                    goodsDTOList != null ? goodsDTOList.size() : 0, e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.MOD_MESSAGE_QUEUE),
                    exchange = @Exchange(name = MqConstant.LOG_EXCHANGE_DIRECT,type = ExchangeTypes.DIRECT),
                    key = {MqConstant.MOD_MESSAGE_ROUTING_KEY}
            )
    )
    public void modMessageLog(GoodsDTO goodsDTO) {
        try {
            operationLogService.modMessage(goodsDTO);
        } catch (Exception e) {
            log.error("处理修改商品信息日志失败，商品信息: {}, 错误信息: {}", goodsDTO, e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.MOD_WAREHOUSE_QUEUE),
                    exchange = @Exchange(name = MqConstant.LOG_EXCHANGE_DIRECT,type = ExchangeTypes.DIRECT),
                    key = {MqConstant.MOD_WAREHOUSE_ROUTING_KEY}
            )
    )
    public void modWarehouseLog(Map<String,Object> message) {
        try {
            Goods goods = (Goods) message.get("goods");
            GoodsDTO goodsDTO = (GoodsDTO) message.get("goodsDTO");
            operationLogService.modWarehouse(goods, goodsDTO);
        } catch (Exception e) {
            log.error("处理修改商品仓库日志失败，消息内容: {}, 错误信息: {}", message, e.getMessage(), e);
            throw e;
        }
    }

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.DELETE_QUEUE),
                    exchange = @Exchange(name = MqConstant.LOG_EXCHANGE_DIRECT,type = ExchangeTypes.DIRECT),
                    key = {MqConstant.DELETE_ROUTING_KEY}
            )
    )
    public void deleteLog(Map<String,Object> message) {
        try {
            Goods goods = (Goods) message.get("goods");
            GoodsDTO goodsDTO = (GoodsDTO) message.get("goodsDTO");
            operationLogService.delete(goods, goodsDTO);
        } catch (Exception e) {
            log.error("处理删除商品日志失败，消息内容: {}, 错误信息: {}", message, e.getMessage(), e);
            throw e;
        }
    }

}
