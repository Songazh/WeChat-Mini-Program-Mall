package com.example.util;

import com.example.constant.SystemConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;
import org.bouncycastle.util.encoders.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * JWT安全增强工具类
 * 实现双重哈希机制、密钥轮换、防重放攻击、智能刷新策略等安全功能
 */
@Component
public class JwtSecurityUtils {
    
    // 密钥版本管理
    private static final Map<String, SecretKey> keyVersions = new ConcurrentHashMap<>();
    private static String currentVersion = "v1.0";
    private static final String SALT = "java1234_mall_security_salt";
    
    // 防重放攻击 - 使用内存存储替代Redis
    private static final Map<String, Long> usedTokens = new ConcurrentHashMap<>();
    
    // 令牌刷新机制
    private static final long REFRESH_THRESHOLD = 30 * 60 * 1000; // 30分钟
    
    static {
        // 初始化默认密钥
        initializeDefaultKey();
    }
    
    /**
     * 初始化默认密钥
     */
    private static void initializeDefaultKey() {
        try {
            // 使用原有系统密钥确保兼容性（与JwtUtils保持一致）
            byte[] encodedKey = Base64.decode(SystemConstant.JWT_SECERT);
            SecretKey defaultKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            keyVersions.put(currentVersion, defaultKey);
            System.out.println("增强版JWT密钥初始化成功: " + currentVersion);
        } catch (Exception e) {
            System.err.println("增强版JWT密钥初始化失败: " + e.getMessage());
            // 确保降级密钥与原有JwtUtils一致
            byte[] encodedKey = Base64.decode(SystemConstant.JWT_SECERT);
            SecretKey fallbackKey = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
            keyVersions.put(currentVersion, fallbackKey);
        }
    }
    
    /**
     * 双重哈希密钥生成 - 增加破解难度，防范重放攻击
     */
    public static SecretKey generateSecureKey() {
        try {
            // 第一重：生成随机种子
            SecureRandom secureRandom = new SecureRandom();
            byte[] seed = new byte[32];
            secureRandom.nextBytes(seed);
            
            // 第二重：对种子进行SHA-256哈希
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] firstHash = digest.digest(seed);
            
            // 第三重：添加盐值并再次哈希
            String saltedData = new String(firstHash, StandardCharsets.UTF_8) + SALT;
            byte[] finalHash = digest.digest(saltedData.getBytes(StandardCharsets.UTF_8));
            
            // 截取32字节作为密钥
            byte[] keyBytes = new byte[32];
            System.arraycopy(finalHash, 0, keyBytes, 0, Math.min(finalHash.length, 32));
            
            return new SecretKeySpec(keyBytes, "HmacSHA256");
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("密钥生成失败", e);
        }
    }
    
    /**
     * 密钥轮换机制 - 定期更新密钥，防范密钥泄露风险
     */
    public static void rotateKey() {
        String newVersion = generateVersion();
        SecretKey newKey = generateSecureKey();
        keyVersions.put(newVersion, newKey);
        
        // 平滑过渡：保持旧版本一段时间
        String oldVersion = currentVersion;
        currentVersion = newVersion;
        
        System.out.println("密钥轮换完成: " + oldVersion + " -> " + newVersion);
        
        // 5分钟后清理旧密钥
        new Thread(() -> {
            try {
                Thread.sleep(5 * 60 * 1000);
                keyVersions.remove(oldVersion);
                System.out.println("旧密钥已清理: " + oldVersion);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 生成版本号
     */
    private static String generateVersion() {
        return "v" + System.currentTimeMillis();
    }
    
    /**
     * 获取当前有效密钥
     */
    public static SecretKey getCurrentKey() {
        return keyVersions.get(currentVersion);
    }
    
    /**
     * 防重放攻击检查
     */
    public boolean isReplayAttack(String tokenId) {
        if (usedTokens.containsKey(tokenId)) {
            return true;
        }
        usedTokens.put(tokenId, System.currentTimeMillis());
        // 清理过期的token记录
        cleanupExpiredTokens();
        return false;
    }
    
    /**
     * 清理过期的token记录
     */
    private void cleanupExpiredTokens() {
        long now = System.currentTimeMillis();
        usedTokens.entrySet().removeIf(entry -> 
            now - entry.getValue() > TimeUnit.HOURS.toMillis(24));
    }
    
    /**
     * 智能令牌刷新机制
     */
    public String refreshToken(String oldToken) {
        if (shouldRefresh(oldToken)) {
            try {
                Claims claims = parseToken(oldToken);
                return generateNewToken(claims);
            } catch (Exception e) {
                System.err.println("令牌刷新失败: " + e.getMessage());
                return null;
            }
        }
        return oldToken;
    }
    
    /**
     * 判断是否需要刷新令牌
     */
    private boolean shouldRefresh(String token) {
        try {
            Date expiration = getExpirationDate(token);
            return expiration.getTime() - System.currentTimeMillis() < REFRESH_THRESHOLD;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 解析令牌
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(getCurrentKey())
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            // 尝试用其他版本的密钥解析
            for (SecretKey key : keyVersions.values()) {
                try {
                    return Jwts.parser()
                            .setSigningKey(key)
                            .parseClaimsJws(token)
                            .getBody();
                } catch (Exception ignored) {
                    // 继续尝试下一个密钥
                }
            }
            throw new RuntimeException("令牌解析失败", e);
        }
    }
    
    /**
     * 生成新令牌
     */
    private String generateNewToken(Claims oldClaims) {
        return Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(oldClaims.getSubject())
                .setIssuer("Java1234")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + SystemConstant.JWT_TTL))
                .signWith(SignatureAlgorithm.HS256, getCurrentKey())
                .compact();
    }
    
    /**
     * 获取令牌过期时间
     */
    private Date getExpirationDate(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 增强版JWT令牌生成
     */
    public String createEnhancedJWT(String id, String subject, long ttlMillis, Map<String, Object> additionalClaims) {
        String jti = UUID.randomUUID().toString();
        
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        
        io.jsonwebtoken.JwtBuilder builder = Jwts.builder()
                .setId(jti)
                .setSubject(subject)
                .setIssuer("Java1234")
                .setIssuedAt(now)
                .signWith(SignatureAlgorithm.HS256, getCurrentKey());
        
        // 添加自定义声明
        if (additionalClaims != null) {
            additionalClaims.forEach(builder::claim);
        }
        
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date expDate = new Date(expMillis);
            builder.setExpiration(expDate);
        }
        
        return builder.compact();
    }
    
    /**
     * 增强版JWT验证
     */
    public Claims validateEnhancedJWT(String jwtStr) {
        try {
            Claims claims = parseToken(jwtStr);
            
            // 只检查但不标记JTI（避免验证时被标记为重放攻击）
            String jti = claims.getId();
            if (jti != null && usedTokens.containsKey(jti)) {
                throw new SecurityException("检测到重放攻击");
            }
            
            // 检查令牌是否需要刷新
            String refreshedToken = refreshToken(jwtStr);
            if (!refreshedToken.equals(jwtStr)) {
                System.out.println("令牌已自动刷新");
            }
            
            return claims;
        } catch (Exception e) {
            throw new RuntimeException("令牌验证失败", e);
        }
    }
    
    /**
     * 标记令牌为已使用（用于敏感操作）
     */
    public void markTokenAsUsed(String jwtStr) {
        try {
            Claims claims = parseToken(jwtStr);
            String jti = claims.getId();
            if (jti != null) {
                usedTokens.put(jti, System.currentTimeMillis());
                cleanupExpiredTokens();
            }
        } catch (Exception e) {
            System.err.println("标记令牌失败: " + e.getMessage());
        }
    }
} 