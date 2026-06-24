package com.jred.wordclub.service;

import com.jred.wordclub.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 IP 频率限制（原子操作，无 TOCTOU 竞态）
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 检查是否被限流。使用先原子递增再判断的模式，避免 TOCTOU 竞态条件。
     *
     * @param action 操作类型标识（如 "register"）
     * @param ip     客户端 IP
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @throws RateLimitException 超过限制时抛出
     */
    private void check(String action, String ip, int maxRequests, int windowSeconds) {
        String key = "ratelimit:" + action + ":" + ip;

        // 先原子递增（首次调用时 key 不存在，INCR 返回 1）
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) count = 1L;

        // 首次调用时设置过期时间
        if (count == 1L) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        if (count > maxRequests) {
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RateLimitException("操作太频繁，请 " + Math.max(ttl, 0) + " 秒后再试");
        }
    }

    /**
     * 注册接口专用限流：
     * - 60 秒内最多 1 次
     * - 3600 秒内最多 5 次
     */
    public void checkRegister(String ip) {
        check("register:min", ip, 1, 60);
        check("register:hour", ip, 5, 3600);
    }

    /**
     * 发送验证码接口专用限流：
     * - 60 秒内最多 1 次（防轰炸）
     * - 3600 秒内最多 10 次
     */
    public void checkSendCode(String ip) {
        check("sendcode:min", ip, 1, 60);
        check("sendcode:hour", ip, 10, 3600);
    }

    /**
     * 登录接口限流（防暴力破解）：
     * - 60 秒内最多 5 次
     * - 3600 秒内最多 30 次
     */
    public void checkLogin(String ip) {
        check("login:min", ip, 5, 60);
        check("login:hour", ip, 30, 3600);
    }
}
