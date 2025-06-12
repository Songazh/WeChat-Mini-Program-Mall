package com.example.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.example.constant.SystemConstant;
import com.example.entity.WxUserInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * OAuth2.0授权管理器
 * 实现授权码模式、令牌交换、微信授权登录流程等功能
 */
@Component
public class OAuth2Manager {
    
    @Autowired
    private JwtSecurityUtils jwtSecurityUtils;
    
    // 授权码存储（生产环境建议使用Redis）
    private static final Map<String, AuthorizationCode> authorizationCodes = new ConcurrentHashMap<>();
    
    // 刷新令牌存储
    private static final Map<String, RefreshTokenInfo> refreshTokens = new ConcurrentHashMap<>();
    
    // 客户端信息
    private static final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();
    
    // 授权码有效期（10分钟）
    private static final long AUTH_CODE_TTL = 10 * 60 * 1000;
    
    // 刷新令牌有效期（30天）
    private static final long REFRESH_TOKEN_TTL = 30 * 24 * 60 * 60 * 1000L;
    
    static {
        // 初始化默认客户端
        initializeDefaultClients();
    }
    
    /**
     * 初始化默认客户端
     */
    private static void initializeDefaultClients() {
        // 微信小程序客户端
        clients.put("miniprogram_client", new ClientInfo(
            "miniprogram_client",
            "miniprogram_secret_2024",
            "http://localhost/callback",
            Arrays.asList("read", "write", "user_info")
        ));
        
        // Vue管理后台客户端
        clients.put("admin_client", new ClientInfo(
            "admin_client", 
            "admin_secret_2024",
            "http://localhost:8080/callback",
            Arrays.asList("admin", "read", "write")
        ));
    }
    
    /**
     * 处理OAuth2.0授权请求（授权码模式）
     */
    public AuthorizationResponse handleAuthorizationRequest(AuthorizationRequest request) {
        // 验证客户端
        ClientInfo client = validateClient(request.getClientId(), request.getRedirectUri());
        if (client == null) {
            throw new SecurityException("无效的客户端");
        }
        
        // 验证授权范围
        if (!validateScope(client, request.getScope())) {
            throw new SecurityException("请求的授权范围超出客户端权限");
        }
        
        // 生成授权码
        String authCode = generateAuthorizationCode();
        
        // 存储授权码信息
        AuthorizationCode codeInfo = new AuthorizationCode(
            authCode,
            request.getClientId(),
            request.getUserId(),
            request.getScope(),
            System.currentTimeMillis() + AUTH_CODE_TTL
        );
        authorizationCodes.put(authCode, codeInfo);
        
        // 清理过期的授权码
        cleanupExpiredAuthCodes();
        
        return new AuthorizationResponse(authCode, request.getState());
    }
    
    /**
     * 处理令牌请求
     */
    public TokenResponse handleTokenRequest(TokenRequest request) {
        if ("authorization_code".equals(request.getGrantType())) {
            return handleAuthorizationCodeGrant(request);
        } else if ("refresh_token".equals(request.getGrantType())) {
            return handleRefreshTokenGrant(request);
        } else {
            throw new SecurityException("不支持的授权类型: " + request.getGrantType());
        }
    }
    
    /**
     * 处理授权码授权
     */
    private TokenResponse handleAuthorizationCodeGrant(TokenRequest request) {
        // 验证客户端凭据
        if (!validateClientCredentials(request.getClientId(), request.getClientSecret())) {
            throw new SecurityException("客户端认证失败");
        }
        
        // 验证授权码
        AuthorizationCode authCode = authorizationCodes.get(request.getCode());
        if (authCode == null || !authCode.getClientId().equals(request.getClientId())) {
            throw new SecurityException("无效的授权码");
        }
        
        // 检查授权码是否过期
        if (System.currentTimeMillis() > authCode.getExpiresAt()) {
            authorizationCodes.remove(request.getCode());
            throw new SecurityException("授权码已过期");
        }
        
        // 使用后立即删除授权码（一次性使用）
        authorizationCodes.remove(request.getCode());
        
        // 生成访问令牌和刷新令牌
        return generateTokens(authCode.getUserId(), authCode.getScope(), request.getClientId());
    }
    
    /**
     * 处理刷新令牌授权
     */
    private TokenResponse handleRefreshTokenGrant(TokenRequest request) {
        // 验证客户端凭据
        if (!validateClientCredentials(request.getClientId(), request.getClientSecret())) {
            throw new SecurityException("客户端认证失败");
        }
        
        // 验证刷新令牌
        RefreshTokenInfo refreshTokenInfo = refreshTokens.get(request.getRefreshToken());
        if (refreshTokenInfo == null || !refreshTokenInfo.getClientId().equals(request.getClientId())) {
            throw new SecurityException("无效的刷新令牌");
        }
        
        // 检查刷新令牌是否过期
        if (System.currentTimeMillis() > refreshTokenInfo.getExpiresAt()) {
            refreshTokens.remove(request.getRefreshToken());
            throw new SecurityException("刷新令牌已过期");
        }
        
        // 生成新的访问令牌
        return generateTokens(refreshTokenInfo.getUserId(), refreshTokenInfo.getScope(), request.getClientId());
    }
    
    /**
     * 微信授权登录流程
     */
    public WxAuthResult handleWxAuthorization(String code, WxUserInfo userInfo) {
        try {
            // 模拟微信接口调用（实际项目中应调用真实微信API）
            String openid = generateOpenid(code);
            String sessionKey = generateSessionKey();
            
            // 创建授权请求
            AuthorizationRequest authRequest = new AuthorizationRequest();
            authRequest.setClientId("miniprogram_client");
            authRequest.setRedirectUri("http://localhost/callback");
            authRequest.setResponseType("code");
            authRequest.setScope(Arrays.asList("read", "write", "user_info"));
            authRequest.setUserId(openid);
            authRequest.setState(UUID.randomUUID().toString());
            
            // 处理授权
            AuthorizationResponse authResponse = handleAuthorizationRequest(authRequest);
            
            // 创建令牌请求
            TokenRequest tokenRequest = new TokenRequest();
            tokenRequest.setGrantType("authorization_code");
            tokenRequest.setCode(authResponse.getCode());
            tokenRequest.setClientId("miniprogram_client");
            tokenRequest.setClientSecret("miniprogram_secret_2024");
            
            // 获取令牌
            TokenResponse tokenResponse = handleTokenRequest(tokenRequest);
            
            return new WxAuthResult(
                openid,
                sessionKey,
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                userInfo
            );
            
        } catch (Exception e) {
            System.err.println("微信授权失败: " + e.getMessage());
            throw new RuntimeException("微信授权失败", e);
        }
    }
    
    /**
     * 生成令牌
     */
    private TokenResponse generateTokens(String userId, List<String> scope, String clientId) {
        // 准备JWT声明
        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);
        claims.put("client_id", clientId);
        claims.put("user_type", determineUserType(userId));
        
        // 生成访问令牌
        String accessToken = jwtSecurityUtils.createEnhancedJWT(
            userId, 
            userId, 
            SystemConstant.JWT_TTL, 
            claims
        );
        
        // 生成刷新令牌
        String refreshToken = generateRefreshToken();
        
        // 存储刷新令牌信息
        RefreshTokenInfo refreshTokenInfo = new RefreshTokenInfo(
            refreshToken,
            userId,
            clientId,
            scope,
            System.currentTimeMillis() + REFRESH_TOKEN_TTL
        );
        refreshTokens.put(refreshToken, refreshTokenInfo);
        
        return new TokenResponse(
            accessToken,
            "Bearer",
            (int) (SystemConstant.JWT_TTL / 1000),
            refreshToken,
            String.join(" ", scope)
        );
    }
    
    /**
     * 验证客户端
     */
    private ClientInfo validateClient(String clientId, String redirectUri) {
        ClientInfo client = clients.get(clientId);
        if (client == null) {
            return null;
        }
        
        // 验证重定向URI
        if (!client.getRedirectUri().equals(redirectUri)) {
            return null;
        }
        
        return client;
    }
    
    /**
     * 验证客户端凭据
     */
    private boolean validateClientCredentials(String clientId, String clientSecret) {
        ClientInfo client = clients.get(clientId);
        return client != null && client.getClientSecret().equals(clientSecret);
    }
    
    /**
     * 验证授权范围
     */
    private boolean validateScope(ClientInfo client, List<String> requestedScope) {
        return client.getAllowedScopes().containsAll(requestedScope);
    }
    
    /**
     * 生成授权码
     */
    private String generateAuthorizationCode() {
        return "AUTH_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 生成刷新令牌
     */
    private String generateRefreshToken() {
        return "RT_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 模拟生成openid
     */
    private String generateOpenid(String code) {
        return "wx_" + Math.abs(code.hashCode()) + "_" + System.currentTimeMillis();
    }
    
    /**
     * 模拟生成session_key
     */
    private String generateSessionKey() {
        return "sk_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * 确定用户类型
     */
    private String determineUserType(String userId) {
        if (userId.startsWith("wx_")) {
            return "wxuser";
        } else if (userId.equals("-1")) {
            return "admin";
        } else {
            return "user";
        }
    }
    
    /**
     * 清理过期的授权码
     */
    private void cleanupExpiredAuthCodes() {
        long now = System.currentTimeMillis();
        authorizationCodes.entrySet().removeIf(entry -> now > entry.getValue().getExpiresAt());
    }
    
    /**
     * 验证访问令牌
     */
    public Claims validateAccessToken(String token) {
        return jwtSecurityUtils.validateEnhancedJWT(token);
    }
    
    // 内部类定义
    
    /**
     * 客户端信息
     */
    public static class ClientInfo {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private List<String> allowedScopes;
        
        public ClientInfo(String clientId, String clientSecret, String redirectUri, List<String> allowedScopes) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
            this.allowedScopes = allowedScopes;
        }
        
        // Getters
        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getRedirectUri() { return redirectUri; }
        public List<String> getAllowedScopes() { return allowedScopes; }
    }
    
    /**
     * 授权码信息
     */
    public static class AuthorizationCode {
        private String code;
        private String clientId;
        private String userId;
        private List<String> scope;
        private long expiresAt;
        
        public AuthorizationCode(String code, String clientId, String userId, List<String> scope, long expiresAt) {
            this.code = code;
            this.clientId = clientId;
            this.userId = userId;
            this.scope = scope;
            this.expiresAt = expiresAt;
        }
        
        // Getters
        public String getCode() { return code; }
        public String getClientId() { return clientId; }
        public String getUserId() { return userId; }
        public List<String> getScope() { return scope; }
        public long getExpiresAt() { return expiresAt; }
    }
    
    /**
     * 刷新令牌信息
     */
    public static class RefreshTokenInfo {
        private String refreshToken;
        private String userId;
        private String clientId;
        private List<String> scope;
        private long expiresAt;
        
        public RefreshTokenInfo(String refreshToken, String userId, String clientId, List<String> scope, long expiresAt) {
            this.refreshToken = refreshToken;
            this.userId = userId;
            this.clientId = clientId;
            this.scope = scope;
            this.expiresAt = expiresAt;
        }
        
        // Getters
        public String getRefreshToken() { return refreshToken; }
        public String getUserId() { return userId; }
        public String getClientId() { return clientId; }
        public List<String> getScope() { return scope; }
        public long getExpiresAt() { return expiresAt; }
    }
    
    // 请求和响应类
    
    /**
     * 授权请求
     */
    public static class AuthorizationRequest {
        private String clientId;
        private String redirectUri;
        private String responseType;
        private List<String> scope;
        private String state;
        private String userId;
        
        // Getters and Setters
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getRedirectUri() { return redirectUri; }
        public void setRedirectUri(String redirectUri) { this.redirectUri = redirectUri; }
        public String getResponseType() { return responseType; }
        public void setResponseType(String responseType) { this.responseType = responseType; }
        public List<String> getScope() { return scope; }
        public void setScope(List<String> scope) { this.scope = scope; }
        public String getState() { return state; }
        public void setState(String state) { this.state = state; }
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
    }
    
    /**
     * 授权响应
     */
    public static class AuthorizationResponse {
        private String code;
        private String state;
        
        public AuthorizationResponse(String code, String state) {
            this.code = code;
            this.state = state;
        }
        
        public String getCode() { return code; }
        public String getState() { return state; }
    }
    
    /**
     * 令牌请求
     */
    public static class TokenRequest {
        @JSONField(name = "grant_type")
        @JsonProperty("grant_type")
        private String grantType;
        private String code;
        @JSONField(name = "client_id")
        @JsonProperty("client_id")
        private String clientId;
        @JSONField(name = "client_secret")
        @JsonProperty("client_secret")
        private String clientSecret;
        @JSONField(name = "refresh_token")
        @JsonProperty("refresh_token")
        private String refreshToken;
        
        // Getters and Setters
        public String getGrantType() { return grantType; }
        public void setGrantType(String grantType) { this.grantType = grantType; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getClientId() { return clientId; }
        public void setClientId(String clientId) { this.clientId = clientId; }
        public String getClientSecret() { return clientSecret; }
        public void setClientSecret(String clientSecret) { this.clientSecret = clientSecret; }
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    }
    
    /**
     * 令牌响应
     */
    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
        private int expiresIn;
        private String refreshToken;
        private String scope;
        
        public TokenResponse(String accessToken, String tokenType, int expiresIn, String refreshToken, String scope) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
            this.refreshToken = refreshToken;
            this.scope = scope;
        }
        
        // Getters
        public String getAccessToken() { return accessToken; }
        public String getTokenType() { return tokenType; }
        public int getExpiresIn() { return expiresIn; }
        public String getRefreshToken() { return refreshToken; }
        public String getScope() { return scope; }
    }
    
    /**
     * 微信授权结果
     */
    public static class WxAuthResult {
        private String openid;
        private String sessionKey;
        private String accessToken;
        private String refreshToken;
        private WxUserInfo userInfo;
        
        public WxAuthResult(String openid, String sessionKey, String accessToken, String refreshToken, WxUserInfo userInfo) {
            this.openid = openid;
            this.sessionKey = sessionKey;
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
            this.userInfo = userInfo;
        }
        
        // Getters
        public String getOpenid() { return openid; }
        public String getSessionKey() { return sessionKey; }
        public String getAccessToken() { return accessToken; }
        public String getRefreshToken() { return refreshToken; }
        public WxUserInfo getUserInfo() { return userInfo; }
    }
} 