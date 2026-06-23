package com.jred.wordclub.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.jred.wordclub.common.Result;
import com.jred.wordclub.dto.AuthResponse;
import com.jred.wordclub.dto.LoginRequest;
import com.jred.wordclub.dto.RegisterRequest;
import com.jred.wordclub.entity.User;
import com.jred.wordclub.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        User user = userService.findByUsername(req.getUsername());
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

    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterRequest req) {
        userService.register(req.getUsername(), req.getEmail(), req.getPassword());
        return Result.ok();
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
}
