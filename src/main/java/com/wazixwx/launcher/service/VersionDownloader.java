package com.wazixwx.launcher.service;

import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;

/**
 * Minecraft版本下载器
 * Minecraft Version Downloader
 * 
 * 该类负责下载Minecraft版本文件，包括版本JSON、JAR文件、库文件等
 * This class is responsible for downloading Minecraft version files, including version JSON, JAR files, library files, etc.
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class VersionDownloader {
    
    /**
     * 基础目录
     * Base directory
     */
    private final String baseDir;
    
    /**
     * 版本目录
     * Version directory
     */
    private final Path versionsDir;
    
    /**
     * 库目录
     * Library directory
     */
    private final Path librariesDir;
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param baseDir 基础目录 Base directory
     */
    public VersionDownloader(String baseDir) {
        this.baseDir = baseDir;
        this.versionsDir = Paths.get(baseDir, "versions");
        this.librariesDir = Paths.get(baseDir, "libraries");
        
        // 确保目录存在
        // Ensure directories exist
        try {
            Files.createDirectories(versionsDir);
            Files.createDirectories(librariesDir);
        } catch (IOException e) {
            LogUtils.error("Failed to create directories", e);
        }
    }
    
    /**
     * 下载版本
     * Download version
     * 
     * @param metadata 版本元数据 Version metadata
     * @param progressCallback 进度回调 Progress callback
     * @return 下载完成后的版本元数据 Version metadata after download
     */
    public VersionMetadata downloadVersion(VersionMetadata metadata, VersionManager.ProgressCallback progressCallback) {
        try {
            // 创建版本目录
            // Create version directory
            Path versionDir = versionsDir.resolve(metadata.getId());
            Files.createDirectories(versionDir);
            
            // 下载版本JSON
            // Download version JSON
            progressCallback.onProgress(0, "Downloading version JSON...");
            Path jsonPath = versionDir.resolve(metadata.getId() + ".json");
            downloadFile(metadata.getUrl(), jsonPath, progressCallback, 0, 20);
            
            // 下载JAR文件
            // Download JAR file
            progressCallback.onProgress(20, "Downloading JAR file...");
            String jarUrl = extractJarUrl(metadata);
            Path jarPath = versionDir.resolve(metadata.getId() + ".jar");
            downloadFile(jarUrl, jarPath, progressCallback, 20, 60);
            
            // 下载库文件
            // Download library files
            progressCallback.onProgress(60, "Downloading libraries...");
            downloadLibraries(metadata, progressCallback, 60, 100);
            
            // 标记为已安装
            // Mark as installed
            metadata.setInstalled(true);
            
            // 完成
            // Complete
            progressCallback.onProgress(100, "Download complete");
            progressCallback.onComplete();
            
            return metadata;
        } catch (Exception e) {
            LogUtils.error("Failed to download version: " + metadata.getId(), e);
            progressCallback.onError("Failed to download version: " + e.getMessage());
            throw new RuntimeException("Failed to download version: " + metadata.getId(), e);
        }
    }
    
    /**
     * 下载文件
     * Download file
     * 
     * @param url URL
     * @param path 保存路径 Save path
     * @param progressCallback 进度回调 Progress callback
     * @param startProgress 起始进度 Start progress
     * @param endProgress 结束进度 End progress
     * @throws IOException IO异常 IO exception
     */
    private void downloadFile(String url, Path path, VersionManager.ProgressCallback progressCallback, int startProgress, int endProgress) throws IOException {
        // 如果文件已存在，跳过下载
        // If file already exists, skip download
        if (Files.exists(path)) {
            return;
        }
        
        // 创建临时文件
        // Create temporary file
        Path tempPath = path.resolveSibling(path.getFileName() + ".tmp");
        
        // 下载文件
        // Download file
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        
        int contentLength = connection.getContentLength();
        int downloaded = 0;
        
        try (InputStream in = connection.getInputStream()) {
            Files.copy(in, tempPath, StandardCopyOption.REPLACE_EXISTING);
            
            // 更新进度
            // Update progress
            if (contentLength > 0) {
                int progress = startProgress + (int) ((downloaded * (endProgress - startProgress)) / contentLength);
                progressCallback.onProgress(progress, "Downloading... " + downloaded + "/" + contentLength + " bytes");
            }
        }
        
        // 重命名临时文件
        // Rename temporary file
        Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
    }
    
    /**
     * 下载库文件
     * Download library files
     * 
     * @param metadata 版本元数据 Version metadata
     * @param progressCallback 进度回调 Progress callback
     * @param startProgress 起始进度 Start progress
     * @param endProgress 结束进度 End progress
     * @throws IOException IO异常 IO exception
     */
    private void downloadLibraries(VersionMetadata metadata, VersionManager.ProgressCallback progressCallback, int startProgress, int endProgress) throws IOException {
        // 如果没有库文件，直接返回
        // If no libraries, return directly
        if (metadata.getLibraries() == null || metadata.getLibraries().isEmpty()) {
            return;
        }
        
        // 计算每个库文件的进度
        // Calculate progress for each library
        int totalLibraries = metadata.getLibraries().size();
        int progressPerLibrary = (endProgress - startProgress) / totalLibraries;
        
        // 下载每个库文件
        // Download each library
        for (int i = 0; i < totalLibraries; i++) {
            VersionMetadata.Library library = metadata.getLibraries().get(i);
            
            // 计算库文件路径
            // Calculate library path
            String libraryPath = library.getPath();
            if (libraryPath == null || libraryPath.isEmpty()) {
                libraryPath = library.getName().replace(":", "/");
            }
            
            // 创建库文件目录
            // Create library directory
            Path libraryDir = librariesDir.resolve(libraryPath).getParent();
            Files.createDirectories(libraryDir);
            
            // 下载库文件
            // Download library
            Path libraryFile = librariesDir.resolve(libraryPath);
            String libraryUrl = library.getUrl();
            if (libraryUrl == null || libraryUrl.isEmpty()) {
                libraryUrl = "https://libraries.minecraft.net/" + libraryPath;
            }
            
            // 更新进度
            // Update progress
            int currentProgress = startProgress + (i * progressPerLibrary);
            int nextProgress = startProgress + ((i + 1) * progressPerLibrary);
            
            progressCallback.onProgress(currentProgress, "Downloading library: " + library.getName());
            downloadFile(libraryUrl, libraryFile, progressCallback, currentProgress, nextProgress);
        }
    }
    
    /**
     * 从版本元数据中提取JAR URL
     * Extract JAR URL from version metadata
     * 
     * @param metadata 版本元数据 Version metadata
     * @return JAR URL
     */
    private String extractJarUrl(VersionMetadata metadata) {
        // 这里使用模拟数据，实际实现需要从版本JSON中提取
        // Using mock data here, actual implementation needs to extract from version JSON
        return "https://launcher.mojang.com/v1/objects/" + metadata.getId() + "/" + metadata.getId() + ".jar";
    }
} 