package com.jred.wordclub.service;

import com.jred.wordclub.exception.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 IP 频率限制
 */
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final StringRedisTemplate redisTemplate;

    /**
     * 检查是否被限流，未超限则计数+1。
     *
     * @param action 操作类型标识（如 "register"）
     * @param ip     客户端 IP
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口（秒）
     * @throws RateLimitException 超过限制时抛出
     */
    private void check(String action, String ip, int maxRequests, int windowSeconds) {
        String key = "ratelimit:" + action + ":" + ip;
        String countStr = redisTemplate.opsForValue().get(key);

        if (countStr == null) {
            // 首次请求，设置计数为 1，带过期时间
            redisTemplate.opsForValue().set(key, "1", windowSeconds, TimeUnit.SECONDS);
            return;
        }

        int count = Integer.parseInt(countStr);
        if (count >= maxRequests) {
            long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            throw new RateLimitException("操作太频繁，请 " + ttl + " 秒后再试");
        }

        // 计数 +1，不重置过期时间
        redisTemplate.opsForValue().increment(key);
    }

    /**
     * 注册接口专用限流：
     * - 60 秒内最多 1 次
     * - 3600 秒内最多 5 次
     */
    public void checkRegister(String ip) {
        check("register:min", ip, 1, 60);    // 每分钟 1 次
        check("register:hour", ip, 5, 3600);  // 每小时 5 次
    }
}
