package com.jred.wordclub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jred.wordclub.common.Result;
import com.jred.wordclub.dto.AuthResponse;
import com.jred.wordclub.dto.DeleteAccountRequest;
import com.jred.wordclub.dto.LoginRequest;
import com.jred.wordclub.dto.RegisterRequest;
import com.jred.wordclub.entity.User;
import com.jred.wordclub.dto.SendCodeRequest;
import com.jred.wordclub.service.MailService;
import com.jred.wordclub.service.RateLimitService;
import com.jred.wordclub.service.UserService;
import com.jred.wordclub.service.VerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final RateLimitService rateLimitService;
    private final VerificationService verificationService;
    private final MailService mailService;
    private final HttpServletRequest request;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        rateLimitService.checkLogin(getClientIp());
        User user = userService.findByEmail(req.getEmail());
        if (!user.getEnabled()) {
            return Result.error(403, "账户已被禁用");
        }
        if (!userService.verifyPassword(req.getPassword(), user.getPassword())) {
            return Result.error(401, "密码错误");
        }
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();
        return Result.ok(new AuthResponse(token, AuthResponse.UserInfo.from(user)));
    }

    @PostMapping("/send-code")
    public Result<?> sendCode(@Valid @RequestBody SendCodeRequest req) {
        rateLimitService.checkSendCode(getClientIp());
        String code = verificationService.generateCode();
        verificationService.saveCode(req.getEmail(), code);
        mailService.sendVerifyCode(req.getEmail(), code);
        return Result.ok();
    }

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        rateLimitService.checkRegister(getClientIp());
        if (!verificationService.verify(req.getEmail(), req.getCode())) {
            return Result.error(400, "验证码错误或已过期");
        }
        userService.register(req.getUsername(), req.getEmail(), req.getPassword());
        return Result.ok();
    }

    /** 获取客户端真实 IP（优先取反向代理头） */
    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @PostMapping("/logout")
    @SaCheckLogin
    public Result<?> logout() {
        StpUtil.logout();
        return Result.ok();
    }

    @GetMapping("/me")
    @SaCheckLogin
    public Result<AuthResponse.UserInfo> me() {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId);
        return Result.ok(AuthResponse.UserInfo.from(user));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refresh() {
        if (!StpUtil.isLogin()) {
            return Result.error(401, "未登录");
        }
        StpUtil.renewTimeout(7200);
        String token = StpUtil.getTokenValue();
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId);
        return Result.ok(new AuthResponse(token, AuthResponse.UserInfo.from(user)));
    }

    @DeleteMapping("/account")
    @SaCheckLogin
    public Result<?> deleteAccount(@Valid @RequestBody DeleteAccountRequest req) {
        long userId = StpUtil.getLoginIdAsLong();
        User user = userService.findById(userId);
        if (!userService.verifyPassword(req.getPassword(), user.getPassword())) {
            return Result.error(400, "密码错误，无法注销账号");
        }
        userService.deleteAccount(userId);
        StpUtil.logout();
        return Result.ok();
    }
}
