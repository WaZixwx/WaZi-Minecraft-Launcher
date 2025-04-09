package com.wazixwx.launcher.service;

import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Minecraft版本验证器
 * Minecraft Version Validator
 * 
 * 该类负责验证Minecraft版本的完整性和正确性
 * This class is responsible for verifying the integrity and correctness of Minecraft versions
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class VersionValidator {
    
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
     * 构造函数
     * Constructor
     * 
     * @param baseDir 基础目录 Base directory
     */
    public VersionValidator(String baseDir) {
        this.baseDir = baseDir;
        this.versionsDir = Paths.get(baseDir, "versions");
        this.librariesDir = Paths.get(baseDir, "libraries");
        this.assetsDir = Paths.get(baseDir, "assets");
    }
    
    /**
     * 验证版本
     * Validate version
     * 
     * @param metadata 版本元数据 Version metadata
     * @return 验证结果 Validation result
     */
    public CompletableFuture<Boolean> validateVersion(VersionMetadata metadata) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LogUtils.info("开始验证版本: " + metadata.getId() + " | Starting version validation: " + metadata.getId());
                
                // 验证版本目录
                // Validate version directory
                Path versionDir = versionsDir.resolve(metadata.getId());
                if (!Files.exists(versionDir)) {
                    LogUtils.warn("版本目录不存在: " + versionDir + " | Version directory does not exist: " + versionDir);
                    return false;
                }
                
                // 验证版本JSON
                // Validate version JSON
                Path jsonPath = versionDir.resolve(metadata.getId() + ".json");
                if (!Files.exists(jsonPath)) {
                    LogUtils.warn("版本JSON不存在: " + jsonPath + " | Version JSON does not exist: " + jsonPath);
                    return false;
                }
                
                // 验证主JAR文件
                // Validate main JAR file
                Path jarPath = versionDir.resolve(metadata.getId() + ".jar");
                if (!Files.exists(jarPath)) {
                    LogUtils.warn("主JAR文件不存在: " + jarPath + " | Main JAR file does not exist: " + jarPath);
                    return false;
                }
                
                // 验证依赖库
                // Validate libraries
                if (!validateLibraries(metadata)) {
                    return false;
                }
                
                // 验证资源文件
                // Validate assets
                if (!validateAssets(metadata)) {
                    return false;
                }
                
                LogUtils.info("版本验证通过: " + metadata.getId() + " | Version validation passed: " + metadata.getId());
                return true;
            } catch (Exception e) {
                LogUtils.error("版本验证失败: " + metadata.getId() + " | Version validation failed: " + metadata.getId(), e);
                return false;
            }
        });
    }
    
    /**
     * 验证依赖库
     * Validate libraries
     * 
     * @param metadata 版本元数据 Version metadata
     * @return 验证结果 Validation result
     */
    private boolean validateLibraries(VersionMetadata metadata) {
        try {
            // 获取依赖库列表
            // Get library list
            Map<String, String> dependencies = metadata.getDependencies();
            if (dependencies == null || dependencies.isEmpty()) {
                return true;
            }
            
            // 验证每个依赖库
            // Validate each library
            for (Map.Entry<String, String> entry : dependencies.entrySet()) {
                String libraryName = entry.getKey();
                String version = entry.getValue();
                
                // 构建库文件路径
                // Build library file path
                String[] parts = libraryName.split(":");
                if (parts.length < 3) {
                    LogUtils.warn("无效的库名称格式: " + libraryName + " | Invalid library name format: " + libraryName);
                    return false;
                }
                
                String groupId = parts[0];
                String artifactId = parts[1];
                String libraryVersion = parts[2];
                
                String libraryPath = groupId.replace(".", "/") + "/" + artifactId + "/" + libraryVersion + "/" + artifactId + "-" + libraryVersion + ".jar";
                Path libraryFile = librariesDir.resolve(libraryPath);
                
                if (!Files.exists(libraryFile)) {
                    LogUtils.warn("依赖库不存在: " + libraryFile + " | Library does not exist: " + libraryFile);
                    return false;
                }
            }
            
            return true;
        } catch (Exception e) {
            LogUtils.error("验证依赖库失败 | Failed to validate libraries", e);
            return false;
        }
    }
    
    /**
     * 验证资源文件
     * Validate assets
     * 
     * @param metadata 版本元数据 Version metadata
     * @return 验证结果 Validation result
     */
    private boolean validateAssets(VersionMetadata metadata) {
        try {
            // 验证资源索引
            // Validate asset index
            Path indexesDir = assetsDir.resolve("indexes");
            Path indexPath = indexesDir.resolve(metadata.getAssets() + ".json");
            
            if (!Files.exists(indexPath)) {
                LogUtils.warn("资源索引不存在: " + indexPath + " | Asset index does not exist: " + indexPath);
                return false;
            }
            
            // 验证资源对象
            // Validate asset objects
            Path objectsDir = assetsDir.resolve("objects");
            if (!Files.exists(objectsDir)) {
                LogUtils.warn("资源对象目录不存在: " + objectsDir + " | Asset objects directory does not exist: " + objectsDir);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            LogUtils.error("验证资源文件失败 | Failed to validate assets", e);
            return false;
        }
    }
    
    /**
     * 计算文件SHA1
     * Calculate file SHA1
     * 
     * @param file 文件 File
     * @return SHA1值 SHA1 value
     * @throws IOException IO异常 IO exception
     * @throws NoSuchAlgorithmException 算法不存在异常 Algorithm not found exception
     */
    private String calculateSHA1(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] data = Files.readAllBytes(file.toPath());
        byte[] hash = digest.digest(data);
        
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        
        return hexString.toString();
    }
} 