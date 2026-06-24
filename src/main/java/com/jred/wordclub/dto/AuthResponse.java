package com.jred.wordclub.dto;

import com.jred.wordclub.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;

    @Data
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String avatarUrl;
        private String role;

        public static UserInfo from(User user) {
            return new UserInfo(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getNickname(),
                    user.getAvatarUrl(),
                    user.getRole()
            );
        }
    }
}
