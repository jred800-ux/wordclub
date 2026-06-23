package com.jred.wordclub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 邮箱验证码服务（Redis 存储）
 */
@Service
@RequiredArgsConstructor
public class VerificationService {

    private final StringRedisTemplate redisTemplate;
    private static final String PREFIX = "email:code:";
    private static final int CODE_TTL = 5; // 分钟

    /** 生成 6 位随机数字验证码 */
    public String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /** 将验证码存入 Redis */
    public void saveCode(String email, String code) {
        redisTemplate.opsForValue().set(PREFIX + email, code, CODE_TTL, TimeUnit.MINUTES);
    }

    /** 校验验证码，无论成功与否都删除 */
    public boolean verify(String email, String inputCode) {
        String key = PREFIX + email;
        String savedCode = redisTemplate.opsForValue().get(key);
        if (savedCode == null) {
            return false;
        }
        redisTemplate.delete(key);
        return savedCode.equals(inputCode);
    }
}
