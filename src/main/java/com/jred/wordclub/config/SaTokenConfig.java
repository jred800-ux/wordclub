package com.jred.wordclub.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 放行认证相关接口
                    SaRouter.match("/api/auth/**").stop();
                    // 其余 /api/** 需登录
                    SaRouter.match("/api/**", r -> StpUtil.checkLogin());
                }))
                .addPathPatterns("/api/**");
    }
}
