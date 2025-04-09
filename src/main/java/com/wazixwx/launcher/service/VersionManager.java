package com.wazixwx.launcher.service;

import com.wazixwx.launcher.model.VersionDetail;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 * Minecraft版本管理器
 * Minecraft Version Manager
 * 
 * 该类负责管理Minecraft版本，包括获取版本列表、下载版本、验证版本等
 * This class is responsible for managing Minecraft versions, including getting version list, downloading versions, validating versions, etc.
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class VersionManager {
    
    /**
     * 版本目录
     * Version directory
     */
    private final Path versionsDir;
    
    /**
     * 版本元数据列表
     * Version metadata list
     */
    private List<VersionMetadata> versionMetadataList;
    
    /**
     * 版本下载器
     * Version downloader
     */
    private final VersionDownloader downloader;
    
    /**
     * 版本验证器
     * Version validator
     */
    private final VersionValidator validator;
    
    /**
     * Minecraft官方版本服务
     * Minecraft official version service
     */
    private final MinecraftVersionService versionService;
    
    /**
     * Gson实例
     * Gson instance
     */
    private final Gson gson;
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param baseDir 基础目录 Base directory
     */
    public VersionManager(String baseDir) {
        this.versionsDir = Paths.get(baseDir, "versions");
        this.downloader = new VersionDownloader(baseDir);
        this.validator = new VersionValidator(baseDir);
        this.versionService = new MinecraftVersionService();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // 确保版本目录存在
        // Ensure version directory exists
        try {
            Files.createDirectories(versionsDir);
        } catch (IOException e) {
            LogUtils.error("创建版本目录失败 | Failed to create versions directory", e);
        }
    }
    
    /**
     * 获取所有可用版本
     * Get all available versions
     * 
     * @return 版本元数据列表 Version metadata list
     */
    public CompletableFuture<List<VersionMetadata>> getAllVersions() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 从Minecraft官方API获取版本列表
                // Get version list from Minecraft official API
                return versionService.getVersionList()
                    .thenCompose(officialVersions -> {
                        // 保存官方版本列表
                        // Save official version list
                        versionMetadataList = officialVersions;
                
                // 获取已安装版本
                // Get installed versions
                List<String> installedVersions = getInstalledVersions();
                
                // 标记已安装版本
                // Mark installed versions
                for (VersionMetadata metadata : versionMetadataList) {
                    metadata.setInstalled(installedVersions.contains(metadata.getId()));
                }
                
                        return CompletableFuture.completedFuture(versionMetadataList);
                    }).join();
            } catch (Exception e) {
                LogUtils.error("获取版本列表失败 | Failed to get version list", e);
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 获取已安装版本列表
     * Get installed version list
     * 
     * @return 已安装版本ID列表 List of installed version IDs
     */
    private List<String> getInstalledVersions() {
        try {
            return Files.list(versionsDir)
                    .filter(Files::isDirectory)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LogUtils.error("获取已安装版本失败 | Failed to get installed versions", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 获取推荐版本
     * Get recommended versions
     * 
     * @return 推荐版本列表 Recommended version list
     */
    public List<VersionMetadata> getRecommendedVersions() {
        if (versionMetadataList == null) {
            return new ArrayList<>();
        }
        
        return versionMetadataList.stream()
                .filter(VersionMetadata::isRecommended)
                .sorted(Comparator.comparing(VersionMetadata::getReleaseTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取最新版本
     * Get latest versions
     * 
     * @return 最新版本列表 Latest version list
     */
    public List<VersionMetadata> getLatestVersions() {
        if (versionMetadataList == null) {
            return new ArrayList<>();
        }
        
        return versionMetadataList.stream()
                .filter(VersionMetadata::isLatest)
                .sorted(Comparator.comparing(VersionMetadata::getReleaseTime).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 获取版本详情
     * Get version details
     * 
     * @param versionId 版本ID Version ID
     * @return 版本详情 Version details
     */
    public CompletableFuture<VersionDetail> getVersionDetails(String versionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 查找版本元数据
                // Find version metadata
                VersionMetadata metadata = findVersionById(versionId);
                if (metadata == null) {
                    throw new IllegalArgumentException("未找到版本: " + versionId + " | Version not found: " + versionId);
                }
                
                // 获取版本详情
                // Get version details
                return versionService.getVersionDetails(versionId, metadata.getUrl()).join();
            } catch (Exception e) {
                LogUtils.error("获取版本详情失败: " + versionId + " | Failed to get version details: " + versionId, e);
                return null;
            }
        });
    }
    
    /**
     * 下载版本
     * Download version
     * 
     * @param versionId 版本ID Version ID
     * @param progressCallback 进度回调 Progress callback
     * @return 下载完成后的版本元数据 Version metadata after download
     */
    public CompletableFuture<VersionMetadata> downloadVersion(String versionId, ProgressCallback progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 查找版本元数据
                // Find version metadata
                VersionMetadata metadata = findVersionById(versionId);
                if (metadata == null) {
                    LogUtils.warn("未找到版本: " + versionId + " | Version not found: " + versionId);
                    throw new IllegalArgumentException("未找到版本: " + versionId + " | Version not found: " + versionId);
                }
                
                // 获取版本详情
                // Get version details
                progressCallback.onProgress(0, "获取版本详情 | Getting version details...");
                VersionDetail versionDetail = getVersionDetails(versionId).join();
                if (versionDetail == null) {
                    throw new IllegalArgumentException("获取版本详情失败: " + versionId + " | Failed to get version details: " + versionId);
                }
                
                // 设置主类
                // Set main class
                metadata.setMainClass(versionDetail.getMainClass());
                
                // 下载版本JAR
                progressCallback.onProgress(5, "下载版本文件 | Downloading version files...");
                
                // 创建版本目录
                Path versionDir = versionsDir.resolve(metadata.getId());
                Files.createDirectories(versionDir);
                
                // 保存版本JSON
                Path jsonPath = versionDir.resolve(metadata.getId() + ".json");
                progressCallback.onProgress(10, "保存版本JSON | Saving version JSON...");
                
                // 保存JSON文件
                try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
                    gson.toJson(versionDetail, writer);
                    LogUtils.info("版本JSON已保存：" + jsonPath + " | Version JSON saved: " + jsonPath);
                }
                
                // 下载主JAR文件
                Path jarPath = versionDir.resolve(metadata.getId() + ".jar");
                VersionDetail.DownloadInfo clientDownload = versionDetail.getDownloads().get("client");
                
                if (clientDownload != null) {
                    progressCallback.onProgress(15, "下载游戏核心文件 | Downloading game core file...");
                    
                    // 使用新的回调接口
                    CompletableFuture<Void> downloadFuture = downloader.downloadFile(
                        clientDownload.getUrl(), 
                        jarPath, 
                        new VersionDownloader.DownloadProgressCallback() {
                            private long lastReportTime = System.currentTimeMillis();
                            
                            @Override
                            public void onProgress(long current, long total, long speed) {
                                long now = System.currentTimeMillis();
                                if (now - lastReportTime > 500 || current == total) { // 每500ms更新一次UI
                                    lastReportTime = now;
                                    int progress = total > 0 ? (int)(15 + current * 40 / total) : 15;
                                    String speedStr = formatSpeed(speed);
                                    String status = String.format(
                                        "下载游戏核心文件：%.2f MB / %.2f MB (%s) | Downloading core: %.2f MB / %.2f MB (%s)", 
                                        current / 1024.0 / 1024.0, 
                                        total / 1024.0 / 1024.0,
                                        speedStr,
                                        current / 1024.0 / 1024.0, 
                                        total / 1024.0 / 1024.0,
                                        speedStr
                                    );
                                    progressCallback.onProgress(progress, status);
                                }
                            }
                            
                            @Override
                            public void onCompleted() {
                                progressCallback.onProgress(55, "核心文件下载完成 | Core file download completed");
                            }
                            
                            @Override
                            public void onError(String error) {
                                progressCallback.onError("下载核心文件失败: " + error + " | Failed to download core file: " + error);
                            }
                        }
                    );
                    
                    // 等待下载完成
                    downloadFuture.join();
                    
                    // 下载资源索引
                    VersionDetail.AssetIndex assetIndex = versionDetail.getAssetIndex();
                    if (assetIndex != null) {
                        progressCallback.onProgress(60, "下载游戏资源索引 | Downloading game assets index...");
                        
                        // 创建资源索引目录
                        Path indexesDir = Paths.get(versionsDir.getParent().toString(), "assets", "indexes");
                        Files.createDirectories(indexesDir);
                        Path indexPath = indexesDir.resolve(assetIndex.getId() + ".json");
                        
                        // 使用新的回调接口下载资源索引
                        CompletableFuture<Void> indexDownloadFuture = downloader.downloadFile(
                            assetIndex.getUrl(), 
                            indexPath, 
                            new VersionDownloader.DownloadProgressCallback() {
                                @Override
                                public void onProgress(long current, long total, long speed) {
                                    int progress = total > 0 ? (int)(60 + current * 10 / total) : 60;
                                    String status = String.format(
                                        "下载资源索引：%.2f KB / %.2f KB | Downloading asset index: %.2f KB / %.2f KB", 
                                        current / 1024.0, 
                                        total / 1024.0,
                                        current / 1024.0, 
                                        total / 1024.0
                                    );
                                    progressCallback.onProgress(progress, status);
                                }
                                
                                @Override
                                public void onCompleted() {
                                    progressCallback.onProgress(70, "资源索引下载完成 | Asset index download completed");
                                }
                                
                                @Override
                                public void onError(String error) {
                                    LogUtils.warn("下载资源索引失败: " + error + " | Failed to download asset index: " + error);
                                    // 继续执行，资源索引不是必需的
                                    progressCallback.onProgress(70, "资源索引下载失败，继续执行 | Asset index download failed, continuing...");
                                }
                            }
                        );
                        
                        // 等待资源索引下载完成
                        try {
                            indexDownloadFuture.join();
                            
                            // 可选：下载资源文件
                            // 在实际项目中，可以提供一个选项让用户决定是否下载资源文件
                            // 这里我们只下载较小的部分重要资源
                            if (Files.exists(indexPath)) {
                                downloadPartialAssets(indexPath, progressCallback);
                            }
                        } catch (Exception e) {
                            LogUtils.warn("下载资源索引时出错: " + e.getMessage() + " | Error when downloading asset index: " + e.getMessage());
                            // 继续执行，资源索引不是必需的
                            progressCallback.onProgress(70, "资源索引下载失败，继续执行 | Asset index download failed, continuing...");
                        }
                    }
                    
                    // 下载库文件
                    progressCallback.onProgress(70, "下载游戏依赖库 | Downloading game libraries...");
                    // 处理库文件下载...
                    if (versionDetail.getLibraries() != null && !versionDetail.getLibraries().isEmpty()) {
                        int totalLibraries = versionDetail.getLibraries().size();
                        int completedLibraries = 0;
                        
                        // 创建库文件目录
                        Path librariesDir = Paths.get(versionsDir.getParent().toString(), "libraries");
                        Files.createDirectories(librariesDir);
                        
                        // 下载每个库文件
                        for (VersionDetail.Library library : versionDetail.getLibraries()) {
                            try {
                                // 检查是否应该下载此库
                                if (!shouldDownloadLibrary(library)) {
                                    completedLibraries++;
                                    continue;
                                }
                                
                                // 获取库文件下载信息
                                Map<String, VersionDetail.DownloadInfo> downloads = library.getDownloads();
                                if (downloads == null || !downloads.containsKey("artifact")) {
                                    completedLibraries++;
                                    continue;
                                }
                                
                                VersionDetail.DownloadInfo artifactInfo = downloads.get("artifact");
                                
                                // 构建库文件路径
                                String libraryPath = getPathFromLibraryName(library.getName());
                                Path libraryFile = librariesDir.resolve(libraryPath);
                                
                                // 确保父目录存在
                                Files.createDirectories(libraryFile.getParent());
                                
                                // 计算当前进度
                                int currentProgress = 70 + completedLibraries * 30 / totalLibraries;
                                
                                // 显示当前下载信息
                                progressCallback.onProgress(currentProgress, 
                                    "下载库文件：" + library.getName() + " (" + (completedLibraries + 1) + "/" + totalLibraries + ") | " +
                                    "Downloading library: " + library.getName() + " (" + (completedLibraries + 1) + "/" + totalLibraries + ")");
                                
                                // 使用新的回调接口下载库文件
                                if (artifactInfo.getUrl() != null && !artifactInfo.getUrl().isEmpty()) {
                                    CompletableFuture<Void> libraryDownloadFuture = downloader.downloadFile(
                                        artifactInfo.getUrl(), 
                                        libraryFile, 
                                        new VersionDownloader.DownloadProgressCallback() {
                                            @Override
                                            public void onProgress(long current, long total, long speed) {
                                                // 库文件下载进度不显示给用户，避免UI频繁更新
                                            }
                                            
                                            @Override
                                            public void onCompleted() {
                                                // 库文件下载完成，不需要特殊处理
                                            }
                                            
                                            @Override
                                            public void onError(String error) {
                                                LogUtils.warn("下载库文件失败: " + library.getName() + " - " + error + 
                                                    " | Failed to download library: " + library.getName() + " - " + error);
                                            }
                                        }
                                    );
                                    
                                    // 等待库文件下载完成
                                    try {
                                        libraryDownloadFuture.join();
                                    } catch (Exception e) {
                                        LogUtils.warn("下载库文件时出错: " + library.getName() + " - " + e.getMessage() + 
                                            " | Error when downloading library: " + library.getName() + " - " + e.getMessage());
                                    }
                                }
                            } catch (Exception e) {
                                LogUtils.warn("处理库文件时出错: " + library.getName() + " - " + e.getMessage() + 
                                    " | Error when processing library: " + library.getName() + " - " + e.getMessage());
                            } finally {
                                completedLibraries++;
                            }
                        }
                    }
                    
                    // 标记为已安装
                    metadata.setInstalled(true);
                    
                    // 完成
                    progressCallback.onProgress(100, "下载完成 | Download complete");
                    progressCallback.onComplete();
                    
                    return metadata;
                } else {
                    throw new IllegalArgumentException("找不到客户端JAR下载信息: " + versionId + " | Client JAR download info not found: " + versionId);
                }
            } catch (Exception e) {
                LogUtils.error("下载版本失败: " + versionId + " | Failed to download version: " + versionId, e);
                progressCallback.onError("下载版本失败: " + e.getMessage() + " | Failed to download version: " + e.getMessage());
                throw new RuntimeException("下载版本失败: " + versionId + " | Failed to download version: " + versionId, e);
            }
        });
    }
    
    /**
     * 格式化下载速度
     * Format download speed
     * 
     * @param bytesPerSecond 每秒字节数 Bytes per second
     * @return 格式化后的速度字符串 Formatted speed string
     */
    private String formatSpeed(long bytesPerSecond) {
        if (bytesPerSecond < 1024) {
            return bytesPerSecond + " B/s";
        } else if (bytesPerSecond < 1024 * 1024) {
            return String.format("%.2f KB/s", bytesPerSecond / 1024.0);
        } else {
            return String.format("%.2f MB/s", bytesPerSecond / 1024.0 / 1024.0);
        }
    }
    
    /**
     * 验证版本
     * Validate version
     * 
     * @param versionId 版本ID Version ID
     * @return 验证结果 Validation result
     */
    public CompletableFuture<Boolean> validateVersion(String versionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 查找版本元数据
                // Find version metadata
                VersionMetadata metadata = findVersionById(versionId);
                if (metadata == null) {
                    LogUtils.warn("未找到版本: " + versionId + " | Version not found: " + versionId);
                    return false;
                }
                
                // 验证版本
                // Validate version
                return validator.validateVersion(metadata).join();
            } catch (Exception e) {
                LogUtils.error("验证版本失败: " + versionId + " | Failed to validate version: " + versionId, e);
                return false;
            }
        });
    }
    
    /**
     * 通过ID查找版本元数据
     * Find version metadata by ID
     * 
     * @param versionId 版本ID Version ID
     * @return 版本元数据 Version metadata
     */
    private VersionMetadata findVersionById(String versionId) {
        if (versionMetadataList == null) {
            return null;
        }
        
        return versionMetadataList.stream()
                .filter(metadata -> metadata.getId().equals(versionId))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * 进度回调接口
     * Progress callback interface
     */
    public interface ProgressCallback {
        
        /**
         * 进度更新
         * Progress update
         * 
         * @param progress 进度（0-100） Progress (0-100)
         * @param status 状态信息 Status information
         */
        void onProgress(int progress, String status);
        
        /**
         * 完成
         * Complete
         */
        void onComplete();
        
        /**
         * 错误
         * Error
         * 
         * @param error 错误信息 Error information
         */
        void onError(String error);
    }
    
    /**
     * 检查是否应该下载这个库
     * Check if this library should be downloaded
     * 
     * @param library 库 Library
     * @return 是否应该下载 Whether should download
     */
    private boolean shouldDownloadLibrary(VersionDetail.Library library) {
        // 如果没有规则，默认下载
        // If no rules, download by default
        if (library.getRules() == null || library.getRules().isEmpty()) {
            return true;
        }
        
        // 获取当前操作系统
        // Get current operating system
        String osName = System.getProperty("os.name").toLowerCase();
        String osType;
        
        if (osName.contains("win")) {
            osType = "windows";
        } else if (osName.contains("mac")) {
            osType = "osx";
        } else if (osName.contains("linux") || osName.contains("unix")) {
            osType = "linux";
        } else {
            osType = "unknown";
        }
        
        // 应用规则
        // Apply rules
        boolean result = false;
        
        for (VersionDetail.Rule rule : library.getRules()) {
            boolean action = "allow".equals(rule.getAction());
            
            if (rule.getOs() != null) {
                // 操作系统规则
                // OS rule
                String ruleOs = rule.getOs().get("name");
                if (ruleOs.equals(osType)) {
                    result = action;
                }
            } else {
                // 全局规则
                // Global rule
                result = action;
            }
        }
        
        return result;
    }
    
    /**
     * 从库名称获取路径
     * Get path from library name
     * 
     * @param name 库名称 Library name
     * @return 路径 Path
     */
    private String getPathFromLibraryName(String name) {
        String[] parts = name.split(":");
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        
        String classifier = "";
        if (parts.length > 3) {
            classifier = "-" + parts[3];
        }
        
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + 
               artifactId + "-" + version + classifier + ".jar";
    }
    
    /**
     * 下载部分资源文件
     * Download partial assets
     * 
     * @param indexPath 资源索引路径 Asset index path
     * @param progressCallback 进度回调 Progress callback
     */
    private void downloadPartialAssets(Path indexPath, ProgressCallback progressCallback) {
        try {
            // 读取资源索引文件
            String content = new String(Files.readAllBytes(indexPath));
            JSONObject indexJson = new JSONObject(content);
            JSONObject objects = indexJson.getJSONObject("objects");
            
            // 创建资源对象目录
            Path objectsDir = Paths.get(versionsDir.getParent().toString(), "assets", "objects");
            Files.createDirectories(objectsDir);
            
            // 获取重要资源列表（如音乐、声音等）
            List<String> importantAssets = new ArrayList<>();
            for (String key : objects.keySet()) {
                if (key.startsWith("minecraft/sounds/") || 
                    key.startsWith("minecraft/music/") || 
                    key.startsWith("minecraft/lang/") ||
                    key.contains("pack.png") || 
                    key.contains("pack.mcmeta")) {
                    importantAssets.add(key);
                }
            }
            
            // 如果重要资源太多，只下载一部分
            int maxAssetsToDownload = Math.min(importantAssets.size(), 50);
            importantAssets = importantAssets.subList(0, maxAssetsToDownload);
            
            LogUtils.info("开始下载重要资源文件，共" + maxAssetsToDownload + "个 | Starting to download important assets, total: " + maxAssetsToDownload);
            progressCallback.onProgress(70, "下载重要资源文件 | Downloading important assets...");
            
            int downloadedAssets = 0;
            for (String key : importantAssets) {
                JSONObject assetInfo = objects.getJSONObject(key);
                String hash = assetInfo.getString("hash");
                long size = assetInfo.getLong("size");
                
                // 构建资源文件路径
                String firstTwo = hash.substring(0, 2);
                Path assetPath = objectsDir.resolve(firstTwo).resolve(hash);
                Files.createDirectories(assetPath.getParent());
                
                // 构建下载URL
                String url = "https://resources.download.minecraft.net/" + firstTwo + "/" + hash;
                
                // 更新进度
                int currentProgress = 70 + downloadedAssets * 10 / maxAssetsToDownload;
                progressCallback.onProgress(currentProgress, 
                    "下载资源文件：" + key + " (" + (downloadedAssets + 1) + "/" + maxAssetsToDownload + ") | " +
                    "Downloading asset: " + key + " (" + (downloadedAssets + 1) + "/" + maxAssetsToDownload + ")");
                
                // 下载资源文件
                try {
                    downloader.downloadFile(url, assetPath, new VersionDownloader.DownloadProgressCallback() {
                        @Override
                        public void onProgress(long current, long total, long speed) {
                            // 不更新进度，避免UI频繁刷新
                        }
                        
                        @Override
                        public void onCompleted() {
                            // 不需要特殊处理
                        }
                        
                        @Override
                        public void onError(String error) {
                            LogUtils.warn("下载资源文件失败: " + key + " - " + error +
                                " | Failed to download asset: " + key + " - " + error);
                        }
                    }).join();
                } catch (Exception e) {
                    LogUtils.warn("下载资源文件时出错: " + key + " - " + e.getMessage() +
                        " | Error when downloading asset: " + key + " - " + e.getMessage());
                }
                
                downloadedAssets++;
            }
            
            progressCallback.onProgress(80, "重要资源文件下载完成 | Important assets download completed");
        } catch (Exception e) {
            LogUtils.error("下载资源文件失败 | Failed to download assets", e);
            progressCallback.onProgress(80, "资源文件下载失败，继续执行 | Assets download failed, continuing...");
        }
    }
} 