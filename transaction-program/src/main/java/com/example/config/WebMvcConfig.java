package com.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置图片静态资源处理
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:C:/Users/SH/Desktop/cshi11/image/");
        
        // 配置轮播图片访问路径
        registry.addResourceHandler("/swiperImgs/**")
                .addResourceLocations("file:C:/Users/SH/Desktop/cshi11/image/swiperImgs/");
        
        // 配置商品大类图片访问路径
        registry.addResourceHandler("/bigTypeImgs/**")
                .addResourceLocations("file:C:/Users/SH/Desktop/cshi11/image/bigTypeImgs/");
        
        // 配置商品图片访问路径
        registry.addResourceHandler("/productImgs/**")
                .addResourceLocations("file:C:/Users/SH/Desktop/cshi11/image/productImgs/");
        
        // 配置商品轮播图片访问路径
        registry.addResourceHandler("/productSwiperImgs/**")
                .addResourceLocations("file:C:/Users/SH/Desktop/cshi11/image/productSwiperImgs/");
                
        System.out.println("静态资源图片配置已加载");
    }
} 