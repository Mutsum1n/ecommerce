package com.github.mutsum1n.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    @Value("${app.mail.from:noreply@ecommerce.com}")
    private String fromEmail;

    @Value("${app.name:电商网站}")
    private String appName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(String toEmail, String orderNumber,
                                      String customerName, String totalAmount, String orderDate) {
        try {
            sendRealEmail(toEmail, orderNumber, customerName, totalAmount, orderDate);
        } catch (Exception e) {
        }
    }

    private void sendRealEmail(String toEmail, String orderNumber,
                               String customerName, String totalAmount, String orderDate) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("订单确认通知 - 订单号: " + orderNumber);

            String text = String.format(
                    "亲爱的 %s，\n\n" +
                            "感谢您在黄瓜网的购物！\n" +
                            "您的订单已支付成功，订单信息如下：\n\n" +
                            "订单编号: %s\n" +
                            "订单金额: %s\n" +
                            "下单时间: %s\n" +
                            "支付状态: 已支付\n\n" +
                            "您可以在网站上查看您的订单。\n" +
                            "如有任何问题，请联系客服。\n\n" +
                            "感谢您的信任与支持！\n" +
                            "黄瓜网团队",
                    customerName, orderNumber, totalAmount, orderDate
            );

            message.setText(text);

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}