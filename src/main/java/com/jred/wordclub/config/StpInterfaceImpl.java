package com.jred.wordclub.config;

import cn.dev33.satoken.stp.StpInterface;
import com.jred.wordclub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserRepository userRepository;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Long userId = Long.valueOf(loginId.toString());
        return userRepository.findById(userId)
                .map(u -> {
                    List<String> roles = new ArrayList<>();
                    roles.add(u.getRole());
                    return roles;
                })
                .orElse(Collections.emptyList());
    }
}
