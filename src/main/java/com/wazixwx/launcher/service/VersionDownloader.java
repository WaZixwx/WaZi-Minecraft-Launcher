package com.wazixwx.launcher.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wazixwx.launcher.model.VersionDetail;
import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.List;
import java.util.Map;

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
     * 资源目录
     * Assets directory
     */
    private final Path assetsDir;
    
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
    public VersionDownloader(String baseDir) {
        this.baseDir = baseDir;
        this.versionsDir = Paths.get(baseDir, "versions");
        this.librariesDir = Paths.get(baseDir, "libraries");
        this.assetsDir = Paths.get(baseDir, "assets");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        // 确保目录存在
        // Ensure directories exist
        try {
            Files.createDirectories(versionsDir);
            Files.createDirectories(librariesDir);
            Files.createDirectories(assetsDir);
        } catch (IOException e) {
            LogUtils.error("创建目录失败 | Failed to create directories", e);
        }
    }
    
    /**
     * 下载版本
     * Download version
     * 
     * @param metadata 版本元数据 Version metadata
     * @param versionDetail 版本详情 Version detail
     * @param progressCallback 进度回调 Progress callback
     * @return 下载完成后的版本元数据 Version metadata after download
     */
    public VersionMetadata downloadVersion(VersionMetadata metadata, VersionDetail versionDetail, VersionManager.ProgressCallback progressCallback) {
        try {
            // 创建版本目录
            // Create version directory
            Path versionDir = versionsDir.resolve(metadata.getId());
            Files.createDirectories(versionDir);
            
            // 保存版本JSON
            // Save version JSON
            progressCallback.onProgress(0, "保存版本JSON | Saving version JSON...");
            Path jsonPath = versionDir.resolve(metadata.getId() + ".json");
            saveVersionJson(versionDetail, jsonPath);
            
            // 下载主JAR文件
            // Download main JAR file
            progressCallback.onProgress(5, "下载JAR文件 | Downloading JAR file...");
            VersionDetail.DownloadInfo clientDownload = versionDetail.getDownloads().get("client");
            if (clientDownload != null) {
            Path jarPath = versionDir.resolve(metadata.getId() + ".jar");
                downloadFile(clientDownload.getUrl(), jarPath, progressCallback, 5, 30);
            } else {
                LogUtils.warn("找不到客户端JAR下载信息: " + metadata.getId() + " | Client JAR download info not found: " + metadata.getId());
            }
            
            // 下载库文件
            // Download library files
            progressCallback.onProgress(30, "下载库文件 | Downloading libraries...");
            downloadLibraries(versionDetail.getLibraries(), progressCallback, 30, 70);
            
            // 下载资源索引
            // Download asset index
            progressCallback.onProgress(70, "下载资源索引 | Downloading asset index...");
            VersionDetail.AssetIndex assetIndex = versionDetail.getAssetIndex();
            if (assetIndex != null) {
                Path indexesDir = assetsDir.resolve("indexes");
                Files.createDirectories(indexesDir);
                Path indexPath = indexesDir.resolve(assetIndex.getId() + ".json");
                downloadFile(assetIndex.getUrl(), indexPath, progressCallback, 70, 75);
                
                // 下载资源文件（可选，因为资源文件可能很多）
                // Download asset files (optional, as there may be many asset files)
                progressCallback.onProgress(75, "下载资源文件 | Downloading asset files...");
                // downloadAssets(indexPath, progressCallback, 75, 95);
                
                // 实际项目中可以提供选项让用户选择是否下载资源文件
                // In actual projects, you can provide options for users to choose whether to download asset files
            }
            
            // 标记为已安装
            // Mark as installed
            metadata.setInstalled(true);
            
            // 设置详细信息
            // Set detailed information
            metadata.setMainClass(versionDetail.getMainClass());
            
            // 如果有Java版本要求，设置依赖
            // If there is a Java version requirement, set dependencies
            if (versionDetail.getJavaVersion() != null) {
                Map<String, String> dependencies = metadata.getDependencies();
                if (dependencies == null) {
                    dependencies = new java.util.HashMap<>();
                    metadata.setDependencies(dependencies);
                }
                dependencies.put("java", String.valueOf(versionDetail.getJavaVersion().getMajorVersion()));
            }
            
            // 完成
            // Complete
            progressCallback.onProgress(100, "下载完成 | Download complete");
            progressCallback.onComplete();
            
            return metadata;
        } catch (Exception e) {
            LogUtils.error("下载版本失败: " + metadata.getId() + " | Failed to download version: " + metadata.getId(), e);
            progressCallback.onError("下载版本失败: " + e.getMessage() + " | Failed to download version: " + e.getMessage());
            throw new RuntimeException("下载版本失败: " + metadata.getId() + " | Failed to download version: " + metadata.getId(), e);
        }
    }
    
    /**
     * 保存版本JSON
     * Save version JSON
     * 
     * @param versionDetail 版本详情 Version detail
     * @param jsonPath JSON文件路径 JSON file path
     * @throws IOException IO异常 IO exception
     */
    private void saveVersionJson(VersionDetail versionDetail, Path jsonPath) throws IOException {
        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(versionDetail, writer);
        }
    }
    
    /**
     * 下载进度回调接口
     * Download progress callback interface
     */
    public interface DownloadProgressCallback {
        /**
         * 下载进度更新
         * Download progress update
         * 
         * @param current 当前下载字节数 Current downloaded bytes
         * @param total 总字节数 Total bytes
         * @param speed 下载速度（字节/秒） Download speed (bytes/second)
         */
        void onProgress(long current, long total, long speed);
        
        /**
         * 下载完成
         * Download completed
         */
        void onCompleted();
        
        /**
         * 下载失败
         * Download failed
         * 
         * @param error 错误信息 Error message
         */
        void onError(String error);
    }
    
    /**
     * 下载文件
     * Download file
     * 
     * @param url 下载URL Download URL
     * @param targetPath 目标路径 Target path
     * @param callback 进度回调 Progress callback
     * @return CompletableFuture
     */
    public CompletableFuture<Void> downloadFile(String url, Path targetPath, DownloadProgressCallback callback) {
        return CompletableFuture.runAsync(() -> {
            try {
                // 创建HTTP客户端
                // Create HTTP client
                HttpClient client = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
                
                // 创建请求
                // Create request
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();
                
                // 发送请求并获取响应
                // Send request and get response
                HttpResponse<InputStream> response = client.send(request, 
                    HttpResponse.BodyHandlers.ofInputStream());
                
                if (response.statusCode() != 200) {
                    throw new IOException("HTTP错误: " + response.statusCode() + 
                        " | HTTP error: " + response.statusCode());
                }
                
                // 获取文件大小
                // Get file size
                long totalSize = response.headers()
                    .firstValueAsLong("Content-Length")
                    .orElse(0);
                
                // 确保目标目录存在
                // Ensure target directory exists
                Files.createDirectories(targetPath.getParent());
        
        // 创建临时文件
        // Create temporary file
                Path tempFile = Files.createTempFile("download-", ".tmp");
                
                try (InputStream input = response.body();
                     OutputStream output = Files.newOutputStream(tempFile)) {
                    
                    byte[] buffer = new byte[8192];
                    long downloaded = 0;
                    long lastTime = System.currentTimeMillis();
                    long lastBytes = 0;
                    
                    int read;
                    while ((read = input.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                        downloaded += read;
                        
                        // 计算下载速度
                        // Calculate download speed
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastTime >= 1000) {
                            long speed = (downloaded - lastBytes) * 1000 / 
                                (currentTime - lastTime);
                            callback.onProgress(downloaded, totalSize, speed);
                            lastTime = currentTime;
                            lastBytes = downloaded;
                        }
                    }
                }
                
                // 移动临时文件到目标位置
                // Move temporary file to target location
                Files.move(tempFile, targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                callback.onCompleted();
                
            } catch (Exception e) {
                callback.onError(e.getMessage());
                throw new CompletionException(e);
            }
        });
    }
    
    /**
     * 下载库文件
     * Download library files
     * 
     * @param libraries 库列表 Library list
     * @param progressCallback 进度回调 Progress callback
     * @param startProgress 起始进度 Start progress
     * @param endProgress 结束进度 End progress
     * @throws IOException IO异常 IO exception
     */
    private void downloadLibraries(List<VersionDetail.Library> libraries, VersionManager.ProgressCallback progressCallback, int startProgress, int endProgress) throws IOException {
        // 如果没有库文件，直接返回
        // If no libraries, return directly
        if (libraries == null || libraries.isEmpty()) {
            progressCallback.onProgress(endProgress, "没有库文件需要下载 | No libraries to download");
            return;
        }
        
        // 计算每个库文件的进度
        // Calculate progress for each library
        int totalLibraries = libraries.size();
        int progressPerLibrary = (endProgress - startProgress) / totalLibraries;
        
        // 下载每个库文件
        // Download each library
        for (int i = 0; i < totalLibraries; i++) {
            VersionDetail.Library library = libraries.get(i);
            
            // 检查规则，判断是否需要下载
            // Check rules to determine if download is needed
            if (!shouldDownloadLibrary(library)) {
                continue;
            }
            
            // 当前进度
            // Current progress
            int currentProgress = startProgress + (i * progressPerLibrary);
            
            // 获取下载信息
            // Get download information
            Map<String, VersionDetail.DownloadInfo> downloads = library.getDownloads();
            if (downloads != null && downloads.containsKey("artifact")) {
                VersionDetail.DownloadInfo artifact = downloads.get("artifact");
                
                // 解析库路径
                // Parse library path
                String libraryPath = artifact.getPath();
                if (libraryPath == null || libraryPath.isEmpty()) {
                    // 从库名称解析路径
                    // Parse path from library name
                    libraryPath = getPathFromLibraryName(library.getName());
                }
                
                // 创建目标路径
                // Create target path
            Path libraryFile = librariesDir.resolve(libraryPath);
                
                // 更新进度
                // Update progress
                progressCallback.onProgress(currentProgress, "下载库文件 | Downloading library: " + library.getName());
                
                // 下载文件
                // Download file
                downloadFile(artifact.getUrl(), libraryFile, progressCallback, currentProgress, currentProgress + progressPerLibrary);
            }
        }
    }
    
    /**
     * 检查库是否应该下载（根据规则）
     * Check if library should be downloaded (according to rules)
     * 
     * @param library 库 Library
     * @return 是否应该下载 Whether it should be downloaded
     */
    private boolean shouldDownloadLibrary(VersionDetail.Library library) {
        // 如果没有规则，默认下载
        // If no rules, download by default
        if (library.getRules() == null || library.getRules().isEmpty()) {
            return true;
        }
        
        // 默认允许
        // Default is allowed
        boolean allowed = false;
        
        // 检查每条规则
        // Check each rule
        for (VersionDetail.Rule rule : library.getRules()) {
            boolean ruleMatches = true;
            
            // 检查操作系统
            // Check operating system
            if (rule.getOs() != null && !rule.getOs().isEmpty()) {
                String osName = System.getProperty("os.name").toLowerCase();
                String osArch = System.getProperty("os.arch").toLowerCase();
                
                // 检查每个OS条件
                // Check each OS condition
                for (Map.Entry<String, String> entry : rule.getOs().entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    
                    if (key.equals("name")) {
                        if (value.equals("windows") && !osName.contains("win")) {
                            ruleMatches = false;
                        } else if (value.equals("osx") && !osName.contains("mac")) {
                            ruleMatches = false;
                        } else if (value.equals("linux") && !osName.contains("linux") && !osName.contains("unix")) {
                            ruleMatches = false;
                        }
                    } else if (key.equals("arch")) {
                        if (!osArch.contains(value)) {
                            ruleMatches = false;
                        }
                    }
                }
            }
            
            // 如果规则匹配，应用操作
            // If rule matches, apply action
            if (ruleMatches) {
                if (rule.getAction().equals("allow")) {
                    allowed = true;
                } else if (rule.getAction().equals("disallow")) {
                    allowed = false;
                }
            }
        }
        
        return allowed;
    }
    
    /**
     * 从库名称获取路径
     * Get path from library name
     * 
     * @param name 库名称 Library name
     * @return 库路径 Library path
     */
    private String getPathFromLibraryName(String name) {
        // 例如：com.google.code.gson:gson:2.8.0
        // 转换为：com/google/code/gson/gson/2.8.0/gson-2.8.0.jar
        String[] parts = name.split(":");
        if (parts.length < 3) {
            return name.replace(".", "/") + ".jar";
        }
        
        String groupId = parts[0];
        String artifactId = parts[1];
        String version = parts[2];
        
        return groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";
    }
} 