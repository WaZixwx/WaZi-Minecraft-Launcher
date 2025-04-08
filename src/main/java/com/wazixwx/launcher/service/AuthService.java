package com.wazixwx.launcher.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wazixwx.launcher.model.Account;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务类
 * Authentication Service Class
 * 
 * 提供Minecraft账号认证功能，支持Mojang和Microsoft认证
 * Provides Minecraft account authentication functionality, supporting Mojang and Microsoft authentication
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class AuthService {
    private static final String MOJANG_AUTH_URL = "https://authserver.mojang.com/authenticate";
    private static final String MOJANG_REFRESH_URL = "https://authserver.mojang.com/refresh";
    private static final String MOJANG_VALIDATE_URL = "https://authserver.mojang.com/validate";
    
    private static final String MS_OAUTH_URL = "https://login.live.com/oauth20_token.srf";
    private static final String MS_XBL_URL = "https://user.auth.xboxlive.com/user/authenticate";
    private static final String MS_XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    private static final String MS_MINECRAFT_URL = "https://api.minecraftservices.com/authentication/login_with_xbox";
    private static final String MS_MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    
    private static final long TOKEN_EXPIRY = TimeUnit.HOURS.toMillis(24); // 24小时过期
    private static final Gson GSON = new Gson();
    
    private final AccountService accountService;
    private final String clientId;
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param accountService 账号服务 Account Service
     * @param clientId 客户端ID Client ID
     */
    public AuthService(AccountService accountService, String clientId) {
        this.accountService = accountService;
        this.clientId = clientId;
    }
    
    /**
     * 使用Mojang账号登录
     * Login with Mojang account
     * 
     * @param email 邮箱 Email
     * @param password 密码 Password
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Account> loginWithMojang(String email, String password) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建客户端令牌 Create client token
                String clientToken = UUID.randomUUID().toString().replace("-", "");
                
                // 构建请求体 Build request body
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("username", email);
                requestJson.addProperty("password", password);
                requestJson.addProperty("clientToken", clientToken);
                requestJson.addProperty("agent", "Minecraft");
                requestJson.addProperty("requestUser", true);
                
                String requestBody = GSON.toJson(requestJson);
                
                // 发送请求 Send request
                URL url = new URL(MOJANG_AUTH_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // 处理响应 Handle response
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        
                        JsonObject jsonResponse = GSON.fromJson(response.toString(), JsonObject.class);
                        
                        // 提取认证信息 Extract authentication info
                        String accessToken = jsonResponse.get("accessToken").getAsString();
                        String selectedProfile = jsonResponse.getAsJsonObject("selectedProfile").get("id").getAsString();
                        String username = jsonResponse.getAsJsonObject("selectedProfile").get("name").getAsString();
                        
                        // 创建账号对象 Create account object
                        long expiresAt = System.currentTimeMillis() + TOKEN_EXPIRY;
                        UUID uuid = UUID.fromString(
                            selectedProfile.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                            )
                        );
                        
                        Account account = new Account(username, email, uuid, accessToken, null, expiresAt, clientToken);
                        
                        // 保存账号 Save account
                        accountService.addAccount(account);
                        
                        return account;
                    }
                } else {
                    // 处理错误 Handle error
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        
                        JsonObject jsonResponse = GSON.fromJson(response.toString(), JsonObject.class);
                        String errorMessage = jsonResponse.get("errorMessage").getAsString();
                        
                        LogUtils.error("Mojang登录失败: " + errorMessage);
                        throw new RuntimeException("登录失败: " + errorMessage);
                    }
                }
            } catch (Exception e) {
                LogUtils.error("Mojang登录过程中出错", e);
                throw new RuntimeException("登录过程中出错: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 使用Microsoft账号登录
     * Login with Microsoft account
     * 
     * @param authCode 授权码 Authorization code
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Account> loginWithMicrosoft(String authCode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 1. 获取OAuth令牌 Get OAuth token
                String oauthJson = String.format("client_id=%s&code=%s&grant_type=authorization_code&redirect_uri=https://login.live.com/oauth20_desktop.srf",
                        clientId, authCode);
                
                String oauthResponse = sendPostRequest(MS_OAUTH_URL, oauthJson, "application/x-www-form-urlencoded");
                JsonObject oauthJsonResponse = GSON.fromJson(oauthResponse, JsonObject.class);
                
                String accessToken = oauthJsonResponse.get("access_token").getAsString();
                String refreshToken = oauthJsonResponse.get("refresh_token").getAsString();
                
                // 2. 获取XBL令牌 Get XBL token
                JsonObject xblJson = new JsonObject();
                JsonObject properties = new JsonObject();
                properties.addProperty("AuthMethod", "RPS");
                properties.addProperty("SiteName", "user.auth.xboxlive.com");
                properties.addProperty("RpsTicket", "d=" + accessToken);
                xblJson.add("Properties", properties);
                xblJson.addProperty("RelyingParty", "http://auth.xboxlive.com");
                xblJson.addProperty("TokenType", "JWT");
                
                String xblResponse = sendPostRequest(MS_XBL_URL, GSON.toJson(xblJson), "application/json");
                JsonObject xblJsonResponse = GSON.fromJson(xblResponse, JsonObject.class);
                
                String xblToken = xblJsonResponse.get("Token").getAsString();
                String userHash = xblJsonResponse.getAsJsonObject("DisplayClaims")
                        .getAsJsonArray("xui").get(0).getAsJsonObject()
                        .get("uhs").getAsString();
                
                // 3. 获取XSTS令牌 Get XSTS token
                JsonObject xstsJson = new JsonObject();
                JsonObject xstsProperties = new JsonObject();
                xstsProperties.addProperty("SandboxId", "RETAIL");
                
                JsonObject userTokens = new JsonObject();
                userTokens.addProperty("UserTokens", xblToken);
                xstsProperties.add("UserTokens", userTokens.getAsJsonArray("UserTokens"));
                
                xstsJson.add("Properties", xstsProperties);
                xstsJson.addProperty("RelyingParty", "rp://api.minecraftservices.com/");
                xstsJson.addProperty("TokenType", "JWT");
                
                String xstsResponse = sendPostRequest(MS_XSTS_URL, GSON.toJson(xstsJson), "application/json");
                JsonObject xstsJsonResponse = GSON.fromJson(xstsResponse, JsonObject.class);
                
                String xstsToken = xstsJsonResponse.get("Token").getAsString();
                
                // 4. 获取Minecraft令牌 Get Minecraft token
                JsonObject mcJson = new JsonObject();
                mcJson.addProperty("identityToken", "XBL3.0 x=" + userHash + ";" + xstsToken);
                
                String mcResponse = sendPostRequest(MS_MINECRAFT_URL, GSON.toJson(mcJson), "application/json");
                JsonObject mcJsonResponse = GSON.fromJson(mcResponse, JsonObject.class);
                
                String mcAccessToken = mcJsonResponse.get("access_token").getAsString();
                
                // 5. 获取用户信息 Get user profile
                URL profileUrl = new URL(MS_MINECRAFT_PROFILE_URL);
                HttpURLConnection profileConnection = (HttpURLConnection) profileUrl.openConnection();
                profileConnection.setRequestMethod("GET");
                profileConnection.setRequestProperty("Authorization", "Bearer " + mcAccessToken);
                
                if (profileConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(profileConnection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder profileResponse = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            profileResponse.append(responseLine.trim());
                        }
                        
                        JsonObject profileJsonResponse = GSON.fromJson(profileResponse.toString(), JsonObject.class);
                        
                        String username = profileJsonResponse.get("name").getAsString();
                        String uuidString = profileJsonResponse.get("id").getAsString();
                        UUID uuid = UUID.fromString(
                            uuidString.replaceFirst(
                                "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                                "$1-$2-$3-$4-$5"
                            )
                        );
                        
                        // 创建客户端令牌 Create client token
                        String clientToken = UUID.randomUUID().toString().replace("-", "");
                        
                        // 创建账号对象 Create account object
                        long expiresAt = System.currentTimeMillis() + TOKEN_EXPIRY;
                        
                        Account account = new Account(username, null, uuid, mcAccessToken, refreshToken, expiresAt, clientToken);
                        
                        // 保存账号 Save account
                        accountService.addAccount(account);
                        
                        return account;
                    }
                } else {
                    // 处理错误 Handle error
                    LogUtils.error("获取微软账号Minecraft信息失败: HTTP " + profileConnection.getResponseCode());
                    throw new RuntimeException("获取微软账号Minecraft信息失败");
                }
            } catch (Exception e) {
                LogUtils.error("微软登录过程中出错", e);
                throw new RuntimeException("登录过程中出错: " + e.getMessage(), e);
            }
        });
    }
    
    /**
     * 刷新令牌
     * Refresh token
     * 
     * @param account 账号 Account
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Boolean> refreshToken(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: 实现具体的刷新逻辑，分为Mojang和Microsoft两种情况
                // TODO: Implement specific refresh logic for both Mojang and Microsoft
                
                // 这里是Mojang账号的刷新逻辑示例
                // Example of refresh logic for Mojang accounts
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("accessToken", account.getAccessToken());
                requestJson.addProperty("clientToken", account.getClientToken());
                
                String requestBody = GSON.toJson(requestJson);
                
                URL url = new URL(MOJANG_REFRESH_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        
                        JsonObject jsonResponse = GSON.fromJson(response.toString(), JsonObject.class);
                        
                        // 更新令牌信息
                        String newAccessToken = jsonResponse.get("accessToken").getAsString();
                        account.setAccessToken(newAccessToken);
                        account.setExpiresAt(System.currentTimeMillis() + TOKEN_EXPIRY);
                        
                        // 保存更新后的账号
                        accountService.addAccount(account);
                        
                        return true;
                    }
                } else {
                    LogUtils.error("刷新令牌失败: HTTP " + connection.getResponseCode());
                    return false;
                }
            } catch (Exception e) {
                LogUtils.error("刷新令牌过程中出错", e);
                return false;
            }
        });
    }
    
    /**
     * 验证令牌
     * Validate token
     * 
     * @param account 账号 Account
     * @return 异步操作结果 Async operation result
     */
    public CompletableFuture<Boolean> validateToken(Account account) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 如果令牌已过期，直接返回false
                if (account.isExpired()) {
                    return false;
                }
                
                // 发送验证请求
                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("accessToken", account.getAccessToken());
                
                String requestBody = GSON.toJson(requestJson);
                
                URL url = new URL(MOJANG_VALIDATE_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }
                
                // 如果返回204，则令牌有效
                return connection.getResponseCode() == HttpURLConnection.HTTP_NO_CONTENT;
            } catch (Exception e) {
                LogUtils.error("验证令牌过程中出错", e);
                return false;
            }
        });
    }
    
    /**
     * 发送POST请求
     * Send POST request
     * 
     * @param urlString URL字符串 URL string
     * @param body 请求体 Request body
     * @param contentType 内容类型 Content type
     * @return 响应内容 Response content
     * @throws Exception 如果请求失败 If request fails
     */
    private String sendPostRequest(String urlString, String body, String contentType) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", contentType);
        connection.setDoOutput(true);
        
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = body.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
        
        if (connection.getResponseCode() >= 200 && connection.getResponseCode() < 300) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                LogUtils.error("HTTP请求失败: " + connection.getResponseCode() + " - " + response.toString());
                throw new RuntimeException("HTTP请求失败: " + connection.getResponseCode() + " - " + response.toString());
            }
        }
    }
} 