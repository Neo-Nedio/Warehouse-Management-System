package com.example.edmo.listener;

import com.example.edmo.util.Constant.MqConstant;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class MailListener {
    @Resource
    JavaMailSender sender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(name = MqConstant.EMAIL_QUEUE),
                    exchange = @Exchange(name = MqConstant.EMAIL_EXCHANGE_FANOUT, type = ExchangeTypes.FANOUT)
            )
    )
    public void sendEmail(Map<String, Object> data) {
        String email = "未知";
        try {
            int code = (int)data.get("code");
            email = (String)data.get("email");
            SimpleMailMessage message = new SimpleMailMessage();
            //设置邮件标题
            message.setSubject("验证码");
            //设置邮件内容
            message.setText("验证码是" + code + "，有效期为2分钟，请尽快使用");
            //设置邮件发送给谁
            message.setTo(email);
            //邮件发送者，从配置文件读取
            message.setFrom(mailFrom);
            sender.send(message);
        } catch (Exception e) {
            log.error("发送邮件失败，收件人: {}, 错误信息: {}", email, e.getMessage(), e);
            throw e;
        }
    }
}
