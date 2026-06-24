package com.jred.wordclub.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.jred.wordclub.common.Result;
import com.jred.wordclub.entity.User;
import com.jred.wordclub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@SaCheckRole("ADMIN")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public Result<Map<String, Object>> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Page<User> result;
        if (search != null && !search.isBlank()) {
            result = userService.searchUsers(search, PageRequest.of(page, size));
        } else {
            result = userService.listUsers(PageRequest.of(page, size));
        }
        Map<String, Object> data = new HashMap<>();
        data.put("content", result.getContent().stream()
                .map(u -> Map.of(
                        "id", u.getId(),
                        "username", u.getUsername(),
                        "email", u.getEmail(),
                        "nickname", u.getNickname(),
                        "enabled", u.getEnabled(),
                        "role", u.getRole(),
                        "createdAt", u.getCreatedAt()))
                .toList());
        data.put("totalElements", result.getTotalElements());
        data.put("totalPages", result.getTotalPages());
        data.put("number", result.getNumber());
        return Result.ok(data);
    }

    @PutMapping("/users/{id}/status")
    public Result<?> toggleUserStatus(@PathVariable Long id) {
        User user = userService.toggleUserStatus(id);
        return Result.ok(Map.of("enabled", user.getEnabled()));
    }

    @DeleteMapping("/users/{id}")
    public Result<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.ok();
    }

    @GetMapping("/users/{id}")
    public Result<Map<String, Object>> getUserDetail(@PathVariable Long id) {
        return Result.ok(userService.getUserDetail(id));
    }
}
