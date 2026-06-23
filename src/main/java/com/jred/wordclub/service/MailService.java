package com.jred.wordclub.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 邮件发送服务（QQ 邮箱 SMTP）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendVerifyCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2407633568@qq.com");
        message.setTo(to);
        message.setSubject("WordClub 邮箱验证码");
        message.setText("您的验证码是：" + code + "，5 分钟内有效。\n\n"
                + "如非本人操作请忽略此邮件。\n\nWordClub 团队");
        try {
            mailSender.send(message);
            log.info("验证码已发送至 {}", to);
        } catch (Exception e) {
            log.error("邮件发送失败: {}", e.getMessage());
        }
    }
}
