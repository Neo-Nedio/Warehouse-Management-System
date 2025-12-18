package com.example.edmo.job;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.edmo.mapper.GoodsMapper;
import com.example.edmo.mapper.UserMapper;
import com.example.edmo.mapper.WarehouseAdminMapper;
import com.example.edmo.mapper.WarehouseUserMapper;
import com.example.edmo.pojo.VO.GoodsExportVO;
import com.example.edmo.pojo.entity.Goods;
import com.example.edmo.pojo.entity.User;
import com.example.edmo.pojo.entity.Warehouse;
import com.example.edmo.pojo.entity.WarehouseAndUser;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

//每周一定时发送仓库商品报表给对应管理员
@Slf4j
@Component
public class WeeklyReportJob implements Job {

    @Resource
    private WarehouseAdminMapper warehouseAdminMapper;

    @Resource
    private WarehouseUserMapper warehouseUserMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        try {
            //获取所有仓库
            List<Warehouse> warehouses = warehouseAdminMapper.selectList(null);

            for (Warehouse warehouse : warehouses) {
                //获取该仓库的管理员
                List<WarehouseAndUser> warehouseUsers = warehouseUserMapper.selectList(
                        Wrappers.<WarehouseAndUser>query().eq("warehouse_id", warehouse.getId())
                );

                if (warehouseUsers.isEmpty()) {
                    //仓库没有管理员，跳过
                    continue;
                }

                //查询该仓库的所有商品
                List<Goods> goodsList = goodsMapper.selectList(
                        Wrappers.<Goods>query()
                                .eq("warehouse_id", warehouse.getId())
                                .eq("status", 1)
                );

                //转换为导出VO
                List<GoodsExportVO> exportList = goodsList.stream().map(goods -> {
                    GoodsExportVO vo = new GoodsExportVO();
                    BeanUtils.copyProperties(goods, vo);
                    return vo;
                }).collect(Collectors.toList());

                //生成Excel文件
                String fileName = generateExcelFile(warehouse.getName(), exportList);

                //发送邮件给所有管理员
                for (WarehouseAndUser wu : warehouseUsers) {
                    User user = userMapper.selectById(wu.getUserId());
                    if (user != null && user.getEmail() != null) {
                        sendReportEmail(user.getEmail(), warehouse.getName(), fileName);
                    }
                }

                // 都发完后，删除临时Excel文件，避免占用磁盘空间
                new File(fileName).delete();
            }

        } catch (Exception e) {
            log.error("周报定时任务执行失败", e);
            throw new JobExecutionException(e);
        }
    }

    //生成Excel文件
    private String generateExcelFile(String warehouseName, List<GoodsExportVO> data) {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String fileName = System.getProperty("java.io.tmpdir") + File.separator 
                + warehouseName + "_周报_" + date + ".xlsx";

        // 拼接文件路径: C:\Users\%{用户名}\AppData\Local\Temp\${warehouseName}_周报_${date}.xlsx
        // File.separator 是跨平台的路径分隔符（Windows是\，Linux是/）

        EasyExcel.write(fileName, GoodsExportVO.class)  // 指定输出路径和数据类型
                .sheet("商品列表")             // Excel的Sheet名称
                .doWrite(data);                         // 把List<GoodsExportVO>写入Excel
        // EasyExcel会根据GoodsExportVO的@ExcelProperty注解自动生成表头

        log.info("生成Excel文件: {}", fileName);
        return fileName;
    }


    // 发送带附件的邮件
    private void sendReportEmail(String to, String warehouseName, String filePath) {
        try {
            // MimeMessage支持复杂邮件（HTML、附件等）
            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart模式，支持附件
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);
            helper.setTo(to);
            helper.setSubject("【周报】" + warehouseName + " 商品库存报表");
            helper.setText("您好，\n\n附件是 " + warehouseName + " 的本周商品库存报表，请查收。\n\n此邮件由系统自动发送，请勿回复。");

            // 把文件路径包装成Spring的Resource对象
            FileSystemResource file = new FileSystemResource(new File(filePath));
            // 添加附件，第一个参数是附件显示名称
            helper.addAttachment(file.getFilename(), file);

            mailSender.send(message);
            log.info("周报邮件已发送至: {}", to);
        } catch (MessagingException e) {
            log.error("发送周报邮件失败，收件人: {}", to, e);
        }
    }
}

