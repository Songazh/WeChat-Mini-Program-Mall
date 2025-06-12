package com.example.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.constant.SystemConstant;
import com.example.entity.Admin;
import com.example.entity.R;
import com.example.entity.WxUserInfo;
import com.example.service.IAdminService;
import com.example.service.IWxUserInfoService;
import com.example.util.JwtSecurityUtils;
import com.example.util.JwtUtils;
import com.example.util.OAuth2Manager;
import com.example.util.StringUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 认证授权控制器
 * 整合JWT和OAuth2.0功能，提供统一的认证接口
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAdminService adminService;
    
    @Autowired
    private IWxUserInfoService wxUserInfoService;
    
    @Autowired
    private JwtSecurityUtils jwtSecurityUtils;
    
    @Autowired
    private OAuth2Manager oAuth2Manager;

    /**
     * 管理员登录 - 增强版JWT认证
     */
    @PostMapping("/admin/login")
    public R adminLoginEnhanced(@RequestBody Admin admin) {
        if (admin == null) {
            return R.error("请求参数不能为空");
        }
        if (StringUtil.isEmpty(admin.getUserName())) {
            return R.error("用户名不能为空");
        }
        if (StringUtil.isEmpty(admin.getPassword())) {
            return R.error("密码不能为空");
        }

        // 验证用户凭据
        Admin resultAdmin = adminService.getOne(new QueryWrapper<Admin>().eq("userName", admin.getUserName()));
        if (resultAdmin == null) {
            return R.error("用户名不存在");
        }
        if (!resultAdmin.getPassword().trim().equals(admin.getPassword())) {
            return R.error("用户名或者密码错误");
        }

        try {
            // 准备JWT声明
            Map<String, Object> claims = new HashMap<>();
            claims.put("user_type", "admin");
            claims.put("user_name", admin.getUserName());
            claims.put("login_time", System.currentTimeMillis());
            claims.put("permissions", Arrays.asList("admin:read", "admin:write", "admin:delete"));
            
            // 生成增强版JWT令牌
            String token = jwtSecurityUtils.createEnhancedJWT(
                "-1", 
                "admin", 
                SystemConstant.JWT_TTL, 
                claims
            );
            
            // 生成刷新令牌
            OAuth2Manager.TokenRequest tokenRequest = new OAuth2Manager.TokenRequest();
            tokenRequest.setGrantType("authorization_code");
            tokenRequest.setClientId("admin_client");
            tokenRequest.setClientSecret("admin_secret_2024");
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", token);
            resultMap.put("user_type", "admin");
            resultMap.put("user_name", admin.getUserName());
            resultMap.put("expires_in", SystemConstant.JWT_TTL / 1000);
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("管理员登录失败: " + e.getMessage());
            return R.error("登录失败，请稍后重试");
        }
    }

    /**
     * 微信用户登录 - OAuth2.0 + JWT融合
     */
    @PostMapping("/wx/login")
    public R wxLoginEnhanced(@RequestBody WxUserInfo wxUserInfo) {
        System.out.println("接收到微信登录请求: " + wxUserInfo);
        
        try {
            // 使用OAuth2.0处理微信授权
            OAuth2Manager.WxAuthResult authResult = oAuth2Manager.handleWxAuthorization(
                wxUserInfo.getCode(), 
                wxUserInfo
            );
            
            // 检查用户是否存在
            String openid = authResult.getOpenid();
            WxUserInfo resultWxUserInfo = wxUserInfoService.getOne(
                new QueryWrapper<WxUserInfo>().eq("openid", openid)
            );
            
            if (resultWxUserInfo == null) {
                System.out.println("用户不存在，创建新用户");
                wxUserInfo.setOpenid(openid);
                wxUserInfo.setRegisterDate(new Date());
                wxUserInfo.setLastLoginDate(new Date());
                wxUserInfoService.save(wxUserInfo);
                resultWxUserInfo = wxUserInfo;
            } else {
                System.out.println("用户已存在，更新用户信息");
                resultWxUserInfo.setNickName(wxUserInfo.getNickName());
                resultWxUserInfo.setAvatarUrl(wxUserInfo.getAvatarUrl());
                resultWxUserInfo.setLastLoginDate(new Date());
                wxUserInfoService.updateById(resultWxUserInfo);
            }
            
            // 构建响应数据
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", authResult.getAccessToken());
            resultMap.put("refresh_token", authResult.getRefreshToken());
            resultMap.put("openid", openid);
            resultMap.put("session_key", authResult.getSessionKey());
            resultMap.put("userInfo", resultWxUserInfo);
            resultMap.put("user_type", "wxuser");
            resultMap.put("expires_in", SystemConstant.JWT_TTL / 1000);
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("微信登录处理异常: " + e.getMessage());
            e.printStackTrace();
            return R.error("微信登录失败: " + e.getMessage());
        }
    }

    /**
     * 令牌刷新
     */
    @PostMapping("/token/refresh")
    public R refreshToken(@RequestHeader("Authorization") String authorization,
                          @RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refresh_token");
            if (StringUtil.isEmpty(refreshToken)) {
                return R.error("刷新令牌不能为空");
            }
            
            // 使用OAuth2.0刷新令牌
            OAuth2Manager.TokenRequest tokenRequest = new OAuth2Manager.TokenRequest();
            tokenRequest.setGrantType("refresh_token");
            tokenRequest.setRefreshToken(refreshToken);
            tokenRequest.setClientId("miniprogram_client");
            tokenRequest.setClientSecret("miniprogram_secret_2024");
            
            OAuth2Manager.TokenResponse tokenResponse = oAuth2Manager.handleTokenRequest(tokenRequest);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("token", tokenResponse.getAccessToken());
            resultMap.put("refresh_token", tokenResponse.getRefreshToken());
            resultMap.put("expires_in", tokenResponse.getExpiresIn());
            resultMap.put("scope", tokenResponse.getScope());
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("令牌刷新失败: " + e.getMessage());
            return R.error("令牌刷新失败");
        }
    }

    /**
     * 令牌验证
     */
    @PostMapping("/token/validate")
    public R validateToken(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            // 使用增强版JWT验证
            Claims claims = jwtSecurityUtils.validateEnhancedJWT(token);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("valid", true);
            resultMap.put("user_id", claims.getSubject());
            resultMap.put("user_type", claims.get("user_type"));
            resultMap.put("expires_at", claims.getExpiration());
            resultMap.put("issued_at", claims.getIssuedAt());
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("令牌验证失败: " + e.getMessage());
            return R.error("令牌验证失败");
        }
    }

    /**
     * OAuth2.0授权端点
     */
    @GetMapping("/oauth/authorize")
    public R authorize(@RequestParam String client_id,
                       @RequestParam String redirect_uri,
                       @RequestParam String response_type,
                       @RequestParam String scope,
                       @RequestParam(required = false) String state,
                       @RequestParam String user_id) {
        try {
            OAuth2Manager.AuthorizationRequest request = new OAuth2Manager.AuthorizationRequest();
            request.setClientId(client_id);
            request.setRedirectUri(redirect_uri);
            request.setResponseType(response_type);
            request.setScope(Arrays.asList(scope.split(" ")));
            request.setState(state);
            request.setUserId(user_id);
            
            OAuth2Manager.AuthorizationResponse response = oAuth2Manager.handleAuthorizationRequest(request);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("code", response.getCode());
            resultMap.put("state", response.getState());
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("OAuth2.0授权失败: " + e.getMessage());
            return R.error("授权失败: " + e.getMessage());
        }
    }

    /**
     * OAuth2.0令牌端点
     */
    @PostMapping("/oauth/token")
    public R token(@RequestBody OAuth2Manager.TokenRequest request) {
        try {
            OAuth2Manager.TokenResponse response = oAuth2Manager.handleTokenRequest(request);
            
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("access_token", response.getAccessToken());
            resultMap.put("token_type", response.getTokenType());
            resultMap.put("expires_in", response.getExpiresIn());
            resultMap.put("refresh_token", response.getRefreshToken());
            resultMap.put("scope", response.getScope());
            
            return R.ok(resultMap);
            
        } catch (Exception e) {
            System.err.println("获取令牌失败: " + e.getMessage());
            return R.error("获取令牌失败: " + e.getMessage());
        }
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public R logout(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            // 验证令牌
            Claims claims = jwtSecurityUtils.validateEnhancedJWT(token);
            
            // 这里可以将令牌加入黑名单
            // 由于当前使用内存存储，暂时记录日志
            System.out.println("用户注销: " + claims.getSubject());
            
            return R.ok("注销成功");
            
        } catch (Exception e) {
            System.err.println("注销失败: " + e.getMessage());
            return R.error("注销失败");
        }
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/user/info")
    public R getUserInfo(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            // 验证令牌
            Claims claims = jwtSecurityUtils.validateEnhancedJWT(token);
            
            String userType = (String) claims.get("user_type");
            String userId = claims.getSubject();
            
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("user_id", userId);
            userInfo.put("user_type", userType);
            userInfo.put("issued_at", claims.getIssuedAt());
            userInfo.put("expires_at", claims.getExpiration());
            
            if ("admin".equals(userType)) {
                userInfo.put("user_name", claims.get("user_name"));
                userInfo.put("permissions", claims.get("permissions"));
            } else if ("wxuser".equals(userType)) {
                // 查询微信用户详细信息
                WxUserInfo wxUser = wxUserInfoService.getOne(
                    new QueryWrapper<WxUserInfo>().eq("openid", userId)
                );
                if (wxUser != null) {
                    userInfo.put("nick_name", wxUser.getNickName());
                    userInfo.put("avatar_url", wxUser.getAvatarUrl());
                }
            }
            
            return R.ok(userInfo);
            
        } catch (Exception e) {
            System.err.println("获取用户信息失败: " + e.getMessage());
            return R.error("获取用户信息失败");
        }
    }

    /**
     * 密钥轮换（管理员接口）
     */
    @PostMapping("/admin/rotate-key")
    public R rotateKey(@RequestHeader("Authorization") String authorization) {
        try {
            String token = authorization.replace("Bearer ", "");
            
            // 验证管理员权限
            Claims claims = jwtSecurityUtils.validateEnhancedJWT(token);
            String userType = (String) claims.get("user_type");
            
            if (!"admin".equals(userType)) {
                return R.error("权限不足");
            }
            
            // 执行密钥轮换
            JwtSecurityUtils.rotateKey();
            
            return R.ok("密钥轮换成功");
            
        } catch (Exception e) {
            System.err.println("密钥轮换失败: " + e.getMessage());
            return R.error("密钥轮换失败");
        }
    }
} 