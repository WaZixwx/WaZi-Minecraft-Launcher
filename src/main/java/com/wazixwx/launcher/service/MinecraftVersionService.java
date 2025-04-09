package com.wazixwx.launcher.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wazixwx.launcher.model.VersionDetail;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Minecraft官方版本服务
 * Minecraft Official Version Service
 * 
 * 该类负责从Minecraft官方API获取版本信息
 * This class is responsible for fetching version information from the Minecraft official API
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class MinecraftVersionService {
    
    /**
     * Minecraft版本清单URL
     * Minecraft version manifest URL
     */
    private static final String VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    
    /**
     * Gson实例
     * Gson instance
     */
    private final Gson gson;
    
    /**
     * 最新发布版本ID
     * Latest release version ID
     */
    private String latestReleaseId;
    
    /**
     * 最新快照版本ID
     * Latest snapshot version ID
     */
    private String latestSnapshotId;
    
    /**
     * 构造函数
     * Constructor
     */
    public MinecraftVersionService() {
        this.gson = new GsonBuilder().create();
    }
    
    /**
     * 获取版本列表
     * Get version list
     * 
     * @return 版本元数据列表 Version metadata list
     */
    public CompletableFuture<List<VersionMetadata>> getVersionList() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LogUtils.info("正在从Minecraft官方API获取版本列表 | Fetching version list from Minecraft official API");
                
                // 获取版本清单JSON
                // Get version manifest JSON
                String manifestJson = fetchUrl(VERSION_MANIFEST_URL);
                
                // 解析版本清单
                // Parse version manifest
                return parseVersionManifest(manifestJson);
            } catch (Exception e) {
                LogUtils.error("获取版本列表失败 | Failed to get version list", e);
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 获取版本详情
     * Get version details
     * 
     * @param versionId 版本ID Version ID
     * @param versionUrl 版本URL Version URL
     * @return 版本详情对象 Version detail object
     */
    public CompletableFuture<VersionDetail> getVersionDetails(String versionId, String versionUrl) {
        return getVersionDetailsJson(versionId, versionUrl).thenApply(this::parseVersionDetail);
    }
    
    /**
     * 获取版本详情
     * Get version details
     * 
     * @param versionId 版本ID Version ID
     * @param versionUrl 版本URL Version URL
     * @return 版本详情JSON Version details JSON
     */
    public CompletableFuture<JsonObject> getVersionDetailsJson(String versionId, String versionUrl) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LogUtils.info("正在获取版本详情: " + versionId + " | Fetching version details: " + versionId);
                
                // 获取版本详情JSON
                // Get version details JSON
                String detailsJson = fetchUrl(versionUrl);
                
                // 解析版本详情
                // Parse version details
                return JsonParser.parseString(detailsJson).getAsJsonObject();
            } catch (Exception e) {
                LogUtils.error("获取版本详情失败: " + versionId + " | Failed to get version details: " + versionId, e);
                return null;
            }
        });
    }
    
    /**
     * 解析版本详情
     * Parse version details
     * 
     * @param detailsJson 版本详情JSON Version details JSON
     * @return 版本详情对象 Version detail object
     */
    private VersionDetail parseVersionDetail(JsonObject detailsJson) {
        if (detailsJson == null) {
            return null;
        }
        
        try {
            VersionDetail detail = new VersionDetail();
            
            // 设置基本信息
            // Set basic information
            detail.setId(detailsJson.get("id").getAsString());
            detail.setType(detailsJson.get("type").getAsString());
            detail.setMainClass(detailsJson.get("mainClass").getAsString());
            detail.setAssets(detailsJson.get("assets").getAsString());
            
            if (detailsJson.has("complianceLevel")) {
                detail.setComplianceLevel(detailsJson.get("complianceLevel").getAsInt());
            }
            
            // 解析AssetIndex
            // Parse AssetIndex
            if (detailsJson.has("assetIndex")) {
                JsonObject assetIndexJson = detailsJson.getAsJsonObject("assetIndex");
                VersionDetail.AssetIndex assetIndex = new VersionDetail.AssetIndex();
                assetIndex.setId(assetIndexJson.get("id").getAsString());
                assetIndex.setSha1(assetIndexJson.get("sha1").getAsString());
                assetIndex.setSize(assetIndexJson.get("size").getAsLong());
                assetIndex.setTotalSize(assetIndexJson.get("totalSize").getAsLong());
                assetIndex.setUrl(assetIndexJson.get("url").getAsString());
                detail.setAssetIndex(assetIndex);
            }
            
            // 解析Downloads
            // Parse Downloads
            if (detailsJson.has("downloads")) {
                JsonObject downloadsJson = detailsJson.getAsJsonObject("downloads");
                Map<String, VersionDetail.DownloadInfo> downloads = new HashMap<>();
                
                for (String key : downloadsJson.keySet()) {
                    JsonObject downloadJson = downloadsJson.getAsJsonObject(key);
                    VersionDetail.DownloadInfo downloadInfo = new VersionDetail.DownloadInfo();
                    downloadInfo.setSha1(downloadJson.get("sha1").getAsString());
                    downloadInfo.setSize(downloadJson.get("size").getAsLong());
                    downloadInfo.setUrl(downloadJson.get("url").getAsString());
                    downloads.put(key, downloadInfo);
                }
                
                detail.setDownloads(downloads);
            }
            
            // 解析JavaVersion
            // Parse JavaVersion
            if (detailsJson.has("javaVersion")) {
                JsonObject javaVersionJson = detailsJson.getAsJsonObject("javaVersion");
                VersionDetail.JavaVersion javaVersion = new VersionDetail.JavaVersion();
                javaVersion.setComponent(javaVersionJson.get("component").getAsString());
                javaVersion.setMajorVersion(javaVersionJson.get("majorVersion").getAsInt());
                detail.setJavaVersion(javaVersion);
            }
            
            // 解析Libraries
            // Parse Libraries
            if (detailsJson.has("libraries")) {
                JsonArray librariesJson = detailsJson.getAsJsonArray("libraries");
                List<VersionDetail.Library> libraries = new ArrayList<>();
                
                for (JsonElement element : librariesJson) {
                    JsonObject libraryJson = element.getAsJsonObject();
                    VersionDetail.Library library = new VersionDetail.Library();
                    library.setName(libraryJson.get("name").getAsString());
                    
                    // 解析Downloads
                    // Parse Downloads
                    if (libraryJson.has("downloads")) {
                        JsonObject downloadsJson = libraryJson.getAsJsonObject("downloads");
                        Map<String, VersionDetail.DownloadInfo> downloads = new HashMap<>();
                        
                        for (String key : downloadsJson.keySet()) {
                            if (downloadsJson.get(key).isJsonObject()) {
                                JsonObject downloadJson = downloadsJson.getAsJsonObject(key);
                                if (downloadJson.has("sha1") && downloadJson.has("size") && downloadJson.has("url")) {
                                    VersionDetail.DownloadInfo downloadInfo = new VersionDetail.DownloadInfo();
                                    downloadInfo.setSha1(downloadJson.get("sha1").getAsString());
                                    downloadInfo.setSize(downloadJson.get("size").getAsLong());
                                    downloadInfo.setUrl(downloadJson.get("url").getAsString());
                                    downloads.put(key, downloadInfo);
                                }
                            }
                        }
                        
                        library.setDownloads(downloads);
                    }
                    
                    // 解析Rules
                    // Parse Rules
                    if (libraryJson.has("rules")) {
                        JsonArray rulesJson = libraryJson.getAsJsonArray("rules");
                        List<VersionDetail.Rule> rules = new ArrayList<>();
                        
                        for (JsonElement ruleElement : rulesJson) {
                            JsonObject ruleJson = ruleElement.getAsJsonObject();
                            VersionDetail.Rule rule = new VersionDetail.Rule();
                            rule.setAction(ruleJson.get("action").getAsString());
                            
                            // 解析OS
                            // Parse OS
                            if (ruleJson.has("os")) {
                                JsonObject osJson = ruleJson.getAsJsonObject("os");
                                Map<String, String> os = new HashMap<>();
                                
                                for (String key : osJson.keySet()) {
                                    os.put(key, osJson.get(key).getAsString());
                                }
                                
                                rule.setOs(os);
                            }
                            
                            // 解析Features
                            // Parse Features
                            if (ruleJson.has("features")) {
                                JsonObject featuresJson = ruleJson.getAsJsonObject("features");
                                Map<String, Boolean> features = new HashMap<>();
                                
                                for (String key : featuresJson.keySet()) {
                                    features.put(key, featuresJson.get(key).getAsBoolean());
                                }
                                
                                rule.setFeatures(features);
                            }
                            
                            rules.add(rule);
                        }
                        
                        library.setRules(rules);
                    }
                    
                    libraries.add(library);
                }
                
                detail.setLibraries(libraries);
            }
            
            // 解析Arguments
            // Parse Arguments
            if (detailsJson.has("arguments")) {
                JsonObject argumentsJson = detailsJson.getAsJsonObject("arguments");
                VersionDetail.Arguments arguments = new VersionDetail.Arguments();
                
                // 解析Game Arguments
                // Parse Game Arguments
                if (argumentsJson.has("game")) {
                    JsonArray gameJson = argumentsJson.getAsJsonArray("game");
                    List<Object> game = new ArrayList<>();
                    
                    for (JsonElement element : gameJson) {
                        if (element.isJsonPrimitive()) {
                            game.add(element.getAsString());
                        } else if (element.isJsonObject()) {
                            // 这里简化处理，实际可能需要更复杂的解析
                            // Simplified processing here, may need more complex parsing in practice
                            game.add(element.toString());
                        }
                    }
                    
                    arguments.setGame(game);
                }
                
                // 解析JVM Arguments
                // Parse JVM Arguments
                if (argumentsJson.has("jvm")) {
                    JsonArray jvmJson = argumentsJson.getAsJsonArray("jvm");
                    List<Object> jvm = new ArrayList<>();
                    
                    for (JsonElement element : jvmJson) {
                        if (element.isJsonPrimitive()) {
                            jvm.add(element.getAsString());
                        } else if (element.isJsonObject()) {
                            // 这里简化处理，实际可能需要更复杂的解析
                            // Simplified processing here, may need more complex parsing in practice
                            jvm.add(element.toString());
                        }
                    }
                    
                    arguments.setJvm(jvm);
                }
                
                detail.setArguments(arguments);
            }
            
            LogUtils.info("版本详情解析完成: " + detail.getId() + " | Version details parsing completed: " + detail.getId());
            return detail;
        } catch (Exception e) {
            LogUtils.error("解析版本详情失败 | Failed to parse version details", e);
            return null;
        }
    }
    
    /**
     * 获取最新发布版本ID
     * Get latest release version ID
     * 
     * @return 最新发布版本ID Latest release version ID
     */
    public String getLatestReleaseId() {
        return latestReleaseId;
    }
    
    /**
     * 获取最新快照版本ID
     * Get latest snapshot version ID
     * 
     * @return 最新快照版本ID Latest snapshot version ID
     */
    public String getLatestSnapshotId() {
        return latestSnapshotId;
    }
    
    /**
     * 解析版本清单
     * Parse version manifest
     * 
     * @param manifestJson 版本清单JSON Version manifest JSON
     * @return 版本元数据列表 Version metadata list
     */
    private List<VersionMetadata> parseVersionManifest(String manifestJson) {
        List<VersionMetadata> versionList = new ArrayList<>();
        
        try {
            // 解析JSON
            // Parse JSON
            JsonObject manifestObj = JsonParser.parseString(manifestJson).getAsJsonObject();
            
            // 获取最新版本信息
            // Get latest version information
            JsonObject latestObj = manifestObj.getAsJsonObject("latest");
            this.latestReleaseId = latestObj.get("release").getAsString();
            this.latestSnapshotId = latestObj.get("snapshot").getAsString();
            
            LogUtils.info("最新发布版本: " + latestReleaseId + " | Latest release version: " + latestReleaseId);
            LogUtils.info("最新快照版本: " + latestSnapshotId + " | Latest snapshot version: " + latestSnapshotId);
            
            // 解析版本列表
            // Parse version list
            JsonArray versionsArray = manifestObj.getAsJsonArray("versions");
            for (JsonElement element : versionsArray) {
                JsonObject versionObj = element.getAsJsonObject();
                
                String id = versionObj.get("id").getAsString();
                String type = versionObj.get("type").getAsString();
                String url = versionObj.get("url").getAsString();
                String timeStr = versionObj.get("time").getAsString();
                String releaseTimeStr = versionObj.get("releaseTime").getAsString();
                
                // 解析时间
                // Parse time
                LocalDateTime time = parseIsoTime(timeStr);
                LocalDateTime releaseTime = parseIsoTime(releaseTimeStr);
                
                // 创建版本元数据
                // Create version metadata
                VersionMetadata metadata = new VersionMetadata();
                metadata.setId(id);
                metadata.setType(type);
                metadata.setUrl(url);
                metadata.setReleaseTime(releaseTime);
                
                // 设置是否为最新版本
                // Set whether it is the latest version
                metadata.setLatest(id.equals(latestReleaseId) || id.equals(latestSnapshotId));
                
                // 设置是否为推荐版本（通常发布版本都是推荐的）
                // Set whether it is a recommended version (usually release versions are recommended)
                metadata.setRecommended(type.equals("release"));
                
                // 设置描述（简单描述）
                // Set description (simple description)
                String description = "Minecraft " + id;
                if (type.equals("snapshot")) {
                    description += " (快照版 | Snapshot)";
                } else if (type.equals("release")) {
                    description += " (正式版 | Release)";
                }
                metadata.setDescription(description);
                
                versionList.add(metadata);
            }
            
            LogUtils.info("共解析 " + versionList.size() + " 个版本 | Parsed " + versionList.size() + " versions");
            
        } catch (Exception e) {
            LogUtils.error("解析版本清单失败 | Failed to parse version manifest", e);
        }
        
        return versionList;
    }
    
    /**
     * 解析ISO 8601时间格式
     * Parse ISO 8601 time format
     * 
     * @param isoTime ISO时间字符串 ISO time string
     * @return LocalDateTime对象 LocalDateTime object
     */
    private LocalDateTime parseIsoTime(String isoTime) {
        try {
            return ZonedDateTime.parse(isoTime, DateTimeFormatter.ISO_DATE_TIME)
                    .toLocalDateTime();
        } catch (Exception e) {
            LogUtils.warn("解析时间失败: " + isoTime + " | Failed to parse time: " + isoTime, e);
            return LocalDateTime.now();
        }
    }
    
    /**
     * 获取URL内容
     * Fetch URL content
     * 
     * @param urlString URL字符串 URL string
     * @return 响应内容 Response content
     * @throws IOException IO异常 IO exception
     */
    private String fetchUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }
        
        return response.toString();
    }
} 