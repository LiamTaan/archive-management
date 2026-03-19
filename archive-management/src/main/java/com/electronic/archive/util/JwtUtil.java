package com.electronic.archive.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
public class JwtUtil {
    // 使用固定密钥，实际项目中应该从配置文件读取
    private static final String SECRET_KEY_STRING = "ThisIsAFixedSecretKeyForJWTTokenGenerationAndValidation1234567890123456789012345678901234567890"; // 至少48个字符
    
    // 创建SecretKeySpec对象，使用HS512算法
    private static final SecretKeySpec SECRET_KEY = new SecretKeySpec(
            SECRET_KEY_STRING.getBytes(),
            SignatureAlgorithm.HS512.getJcaName()
    );

    // Token有效期，默认24小时
    private static final long EXPIRATION_TIME = 86400000L;

    /**
     * 生成Token
     * @param username 用户名
     * @return Token字符串
     */
    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // 使用JJWT 0.11.5版本的API生成Token
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, SECRET_KEY)
                .compact();
    }

    /**
     * 从Token中获取用户名
     * @param token Token字符串
     * @return 用户名，如果Token无效则返回null
     */
    public String getUsernameFromToken(String token) {
        try {
            // 使用JJWT 0.11.5版本的API解析Token
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject();
        } catch (Exception e) {
            // 记录具体的错误信息，便于调试
            System.err.println("JWT解析失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 验证Token是否有效
     * @param token Token字符串
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            // 使用JJWT 0.11.5版本的API验证Token
            Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 记录具体的错误信息，便于调试
            System.err.println("JWT验证失败: " + e.getMessage());
            return false;
        }
    }
}