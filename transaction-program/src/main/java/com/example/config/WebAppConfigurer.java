package com.example.config;

import com.example.interceptor.SysInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web项目配置类
 */
@Configuration
public class WebAppConfigurer implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowCredentials(true)
                .allowedMethods("GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600);
    }

    @Bean
    public SysInterceptor sysInterceptor() {
        return new SysInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String[] patterns = new String[]{
            "/adminLogin",
            "/product/**",
            "/bigType/**",
            "/users/wxlogin",
            "/user/wxlogin",
            "/weixinpay/**",
            "/users/**",
            "/order/**",
            "/member/info",
            "/member/**",
            "/distribution/**",
            "/productSwiperImage/**",
            "/test/**",
            "/image/**",
            "/**/*.jpg",
            "/**/*.png",
            "/**/*.jpeg",
            "/**/*.gif",
            "/actuator/**",
            "/health",
            "/error"
        };
        registry.addInterceptor(sysInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(patterns);
    }
}
