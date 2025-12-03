package com.example.edmo.util.Constant;

public class MqConstant {
    //日志相关
    public static final String LOG_EXCHANGE_DIRECT = "log.direct";
    // 队列名称
    public static final String ADD_QUEUE = "add.queue";
    public static final String BATCH_ADD_QUEUE = "BATCH.add.queue";
    public static final String MOD_MESSAGE_QUEUE = "mod.message.queue";
    public static final String MOD_WAREHOUSE_QUEUE = "mod.warehouse.queue";
    public static final String DELETE_QUEUE = "delete.queue";
    // Routing Key（用于消息路由）
    public static final String ADD_ROUTING_KEY = "add";
    public static final String BATCH_ADD_ROUTING_KEY = "batchAdd";
    public static final String MOD_MESSAGE_ROUTING_KEY = "modMessage";
    public static final String MOD_WAREHOUSE_ROUTING_KEY = "modWarehouse";
    public static final String DELETE_ROUTING_KEY = "delete";



    //邮箱相关
    public static final String EMAIL_EXCHANGE_FANOUT = "email.fanout";
    public static final String EMAIL_QUEUE = "email.queue";
}
