package com.wazixwx.launcher.service;

import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

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
     * 构造函数
     * Constructor
     * 
     * @param baseDir 基础目录 Base directory
     */
    public VersionManager(String baseDir) {
        this.versionsDir = Paths.get(baseDir, "versions");
        this.downloader = new VersionDownloader(baseDir);
        this.validator = new VersionValidator(baseDir);
        
        // 确保版本目录存在
        // Ensure version directory exists
        try {
            Files.createDirectories(versionsDir);
        } catch (IOException e) {
            LogUtils.error("Failed to create versions directory", e);
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
                // 这里使用模拟数据，实际实现需要调用API
                // Using mock data here, actual implementation needs to call API
                versionMetadataList = getMockVersionList();
                
                // 获取已安装版本
                // Get installed versions
                List<String> installedVersions = getInstalledVersions();
                
                // 标记已安装版本
                // Mark installed versions
                for (VersionMetadata metadata : versionMetadataList) {
                    metadata.setInstalled(installedVersions.contains(metadata.getId()));
                }
                
                return versionMetadataList;
            } catch (Exception e) {
                LogUtils.error("Failed to get version list", e);
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
            LogUtils.error("Failed to get installed versions", e);
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
                    throw new IllegalArgumentException("Version not found: " + versionId);
                }
                
                // 下载版本
                // Download version
                return downloader.downloadVersion(metadata, progressCallback);
            } catch (Exception e) {
                LogUtils.error("Failed to download version: " + versionId, e);
                throw new RuntimeException("Failed to download version: " + versionId, e);
            }
        });
    }
    
    /**
     * 验证版本
     * Validate version
     * 
     * @param versionId 版本ID Version ID
     * @return 验证结果 Validation result
     */
    public CompletableFuture<boolean> validateVersion(String versionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 查找版本元数据
                // Find version metadata
                VersionMetadata metadata = findVersionById(versionId);
                if (metadata == null) {
                    return false;
                }
                
                // 验证版本
                // Validate version
                return validator.validateVersion(metadata);
            } catch (Exception e) {
                LogUtils.error("Failed to validate version: " + versionId, e);
                return false;
            }
        });
    }
    
    /**
     * 根据ID查找版本
     * Find version by ID
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
     * 获取模拟版本列表（仅用于测试）
     * Get mock version list (for testing only)
     * 
     * @return 模拟版本列表 Mock version list
     */
    private List<VersionMetadata> getMockVersionList() {
        List<VersionMetadata> list = new ArrayList<>();
        
        // 添加一些模拟版本
        // Add some mock versions
        VersionMetadata v1 = new VersionMetadata();
        v1.setId("1.19.2");
        v1.setType("release");
        v1.setUrl("https://launcher.mojang.com/v1/objects/f1bc5579a035d2e478d54a8f416391d7c3ab7355/1.19.2.json");
        v1.setReleaseTime(java.time.LocalDateTime.now().minusDays(30));
        v1.setDescription("Minecraft 1.19.2");
        v1.setRecommended(true);
        v1.setLatest(false);
        v1.setMainClass("net.minecraft.client.main.Main");
        v1.setMinMemory(2048);
        v1.setRecommendedMemory(4096);
        list.add(v1);
        
        VersionMetadata v2 = new VersionMetadata();
        v2.setId("1.18.2");
        v2.setType("release");
        v2.setUrl("https://launcher.mojang.com/v1/objects/c8f83c5655308435b3dcf03c06d9fe8740a77469/1.18.2.json");
        v2.setReleaseTime(java.time.LocalDateTime.now().minusDays(60));
        v2.setDescription("Minecraft 1.18.2");
        v2.setRecommended(true);
        v2.setLatest(false);
        v2.setMainClass("net.minecraft.client.main.Main");
        v2.setMinMemory(2048);
        v2.setRecommendedMemory(4096);
        list.add(v2);
        
        VersionMetadata v3 = new VersionMetadata();
        v3.setId("1.16.5");
        v3.setType("release");
        v3.setUrl("https://launcher.mojang.com/v1/objects/1b557e7b033b583cd9f66746b7a9ab1ec1673ced/1.16.5.json");
        v3.setReleaseTime(java.time.LocalDateTime.now().minusDays(120));
        v3.setDescription("Minecraft 1.16.5");
        v3.setRecommended(true);
        v3.setLatest(false);
        v3.setMainClass("net.minecraft.client.main.Main");
        v3.setMinMemory(2048);
        v3.setRecommendedMemory(4096);
        list.add(v3);
        
        return list;
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
} 