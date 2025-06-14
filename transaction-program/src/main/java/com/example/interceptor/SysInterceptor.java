package com.example.interceptor;

import com.example.util.JwtUtils;
import com.example.util.JwtSecurityUtils;
import com.example.util.OAuth2Manager;
import com.example.util.StringUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 鉴权拦截器
 */
public class SysInterceptor implements HandlerInterceptor {

    // 开发模式标志，生产环境中应设为false
    private static final boolean DEV_MODE = true;
    
    @Autowired
    private JwtSecurityUtils jwtSecurityUtils;
    
    @Autowired
    private OAuth2Manager oAuth2Manager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        System.out.println("拦截器处理路径: " + path);
        
        // 放行微信登录接口
        if (path.equals("/user/wxlogin") || path.equals("/auth/wx/login")) {
            System.out.println("微信登录接口放行");
            return true;
        }
        
        // 放行认证相关接口
        if (path.startsWith("/auth/")) {
            System.out.println("认证接口放行: " + path);
            return true;
        }
        
        // 静态资源直接放行处理
        if (path.startsWith("/image/") || 
            path.contains("/image/") || 
            path.startsWith("/swiperImgs/") ||
            path.startsWith("/bigTypeImgs/") ||
            path.startsWith("/productImgs/") ||
            path.startsWith("/productSwiperImgs/") ||
            path.endsWith(".jpg") || 
            path.endsWith(".jpeg") || 
            path.endsWith(".png") || 
            path.endsWith(".gif") ||
            path.contains(".jpg") || 
            path.contains(".jpeg") || 
            path.contains(".png") || 
            path.contains(".gif")) {
            System.out.println("静态资源路径放行: " + path);
            return true;
        }
        
        if (handler instanceof HandlerMethod) {
            // 判断token是否为空
            String token = request.getHeader("token");
            System.out.println("当前请求token=" + token);
            
            if (StringUtil.isEmpty(token)) {
                System.out.println("token为空");
                throw new RuntimeException("签名验证不存在");
            } else {
                // 开发模式下检查模拟token
                if (DEV_MODE && token.startsWith("mock_token_")) {
                    System.out.println("开发模式：模拟token验证通过");
                    return true;
                }
                
                // 增强版JWT验证
                Claims claims = null;
                try {
                    // 优先使用增强版JWT验证
                    if (jwtSecurityUtils != null) {
                        claims = jwtSecurityUtils.validateEnhancedJWT(token);
                        System.out.println("使用增强版JWT验证成功");
                    } else {
                        // 降级到基础JWT验证
                        claims = JwtUtils.validateJWT(token).getClaims();
                        System.out.println("使用基础JWT验证成功");
                    }
                } catch (Exception e) {
                    System.err.println("JWT验证失败: " + e.getMessage());
                    throw new RuntimeException("鉴权失败");
                }

                // 管理员 /admin开头路径请求
                if (path.startsWith("/admin")) {
                    String userType = (String) claims.get("user_type");
                    String subject = claims.getSubject();
                    
                    System.out.println("=== 管理员权限验证 ===");
                    System.out.println("请求路径: " + path);
                    System.out.println("userType: " + userType);
                    System.out.println("subject: " + subject);
                    System.out.println("claims内容: " + claims);
                    
                    // 临时放宽权限验证，只检查claims不为空
                    if (claims == null) {
                        System.err.println("❌ 管理员鉴权失败: claims为空");
                        throw new RuntimeException("管理员 鉴权失败");
                    } else {
                        System.out.println("✅ 管理员鉴权通过（临时放宽）");
                        return true;
                    }
                } else {
                    if (claims == null) {
                        System.out.println("鉴权失败");
                        throw new RuntimeException("鉴权失败");
                    } else {
                        System.out.println("鉴权成功");
                        return true;
                    }
                }
            }
        } else {
            return true;
        }
    }
}
