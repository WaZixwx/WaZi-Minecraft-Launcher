package com.wazixwx.launcher.model;

import java.util.UUID;

/**
 * Minecraft账号模型类
 * Minecraft Account Model Class
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class Account {
    private String username;        // 用户名 Username
    private String email;           // 邮箱 Email
    private UUID uuid;              // UUID
    private String accessToken;     // 访问令牌 Access Token
    private String refreshToken;    // 刷新令牌 Refresh Token
    private long expiresAt;         // 过期时间 Expiration Time
    private String clientToken;     // 客户端令牌 Client Token
    private boolean selected;       // 是否选中 Is Selected
    
    /**
     * 构造函数 Constructor
     * 
     * @param username 用户名 Username
     * @param email 邮箱 Email
     * @param uuid UUID
     * @param accessToken 访问令牌 Access Token
     * @param refreshToken 刷新令牌 Refresh Token
     * @param expiresAt 过期时间 Expiration Time
     * @param clientToken 客户端令牌 Client Token
     */
    public Account(String username, String email, UUID uuid, String accessToken, 
                  String refreshToken, long expiresAt, String clientToken) {
        this.username = username;
        this.email = email;
        this.uuid = uuid;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
        this.clientToken = clientToken;
        this.selected = false;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UUID getUuid() {
        return uuid;
    }
    
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
    
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public long getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getClientToken() {
        return clientToken;
    }
    
    public void setClientToken(String clientToken) {
        this.clientToken = clientToken;
    }
    
    public boolean isSelected() {
        return selected;
    }
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
    
    /**
     * 检查令牌是否过期
     * Check if the token is expired
     * 
     * @return 是否过期 Is Expired
     */
    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
} 