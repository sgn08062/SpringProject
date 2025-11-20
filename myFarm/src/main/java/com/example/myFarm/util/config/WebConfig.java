package com.example.myFarm.util.config;

import com.example.myFarm.util.interceptor.LoginCheckInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${project.upload.path}")   // C:/Image
    private String uploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                // 로그인 필요 구간(보호 대상)
                .addPathPatterns("/admin/**", "/user/**")
                // 누구나 접근 가능(로그인/회원가입/정적/공개 API 등)
                .excludePathPatterns(
                        "/", "/account/**", "/common/**",
                        "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error"
                )
                .order(1);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:///" + uploadPath + "/");

    }
}
