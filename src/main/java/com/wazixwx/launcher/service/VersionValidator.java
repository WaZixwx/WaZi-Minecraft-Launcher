package com.wazixwx.launcher.service;

import com.wazixwx.launcher.model.VersionMetadata;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Minecraft版本验证器
 * Minecraft Version Validator
 * 
 * 该类负责验证Minecraft版本文件的完整性，包括版本JSON、JAR文件、库文件等
 * This class is responsible for validating the integrity of Minecraft version files, including version JSON, JAR files, library files, etc.
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
     * 构造函数
     * Constructor
     * 
     * @param baseDir 基础目录 Base directory
     */
    public VersionValidator(String baseDir) {
        this.baseDir = baseDir;
        this.versionsDir = Paths.get(baseDir, "versions");
        this.librariesDir = Paths.get(baseDir, "libraries");
    }
    
    /**
     * 验证版本
     * Validate version
     * 
     * @param metadata 版本元数据 Version metadata
     * @return 验证结果 Validation result
     */
    public boolean validateVersion(VersionMetadata metadata) {
        try {
            // 验证版本目录是否存在
            // Validate version directory exists
            Path versionDir = versionsDir.resolve(metadata.getId());
            if (!Files.exists(versionDir)) {
                LogUtils.warn("Version directory does not exist: " + versionDir);
                return false;
            }
            
            // 验证版本JSON是否存在
            // Validate version JSON exists
            Path jsonPath = versionDir.resolve(metadata.getId() + ".json");
            if (!Files.exists(jsonPath)) {
                LogUtils.warn("Version JSON does not exist: " + jsonPath);
                return false;
            }
            
            // 验证JAR文件是否存在
            // Validate JAR file exists
            Path jarPath = versionDir.resolve(metadata.getId() + ".jar");
            if (!Files.exists(jarPath)) {
                LogUtils.warn("JAR file does not exist: " + jarPath);
                return false;
            }
            
            // 验证库文件是否存在
            // Validate library files exist
            if (!validateLibraries(metadata)) {
                return false;
            }
            
            return true;
        } catch (Exception e) {
            LogUtils.error("Failed to validate version: " + metadata.getId(), e);
            return false;
        }
    }
    
    /**
     * 验证库文件
     * Validate library files
     * 
     * @param metadata 版本元数据 Version metadata
     * @return 验证结果 Validation result
     */
    private boolean validateLibraries(VersionMetadata metadata) {
        // 如果没有库文件，直接返回true
        // If no libraries, return true directly
        if (metadata.getLibraries() == null || metadata.getLibraries().isEmpty()) {
            return true;
        }
        
        // 验证每个库文件
        // Validate each library
        for (VersionMetadata.Library library : metadata.getLibraries()) {
            // 计算库文件路径
            // Calculate library path
            String libraryPath = library.getPath();
            if (libraryPath == null || libraryPath.isEmpty()) {
                libraryPath = library.getName().replace(":", "/");
            }
            
            // 验证库文件是否存在
            // Validate library file exists
            Path libraryFile = librariesDir.resolve(libraryPath);
            if (!Files.exists(libraryFile)) {
                LogUtils.warn("Library file does not exist: " + libraryFile);
                return false;
            }
            
            // 验证库文件SHA1（如果有）
            // Validate library file SHA1 (if any)
            String sha1 = library.getSha1();
            if (sha1 != null && !sha1.isEmpty()) {
                try {
                    String calculatedSha1 = calculateSha1(libraryFile);
                    if (!sha1.equals(calculatedSha1)) {
                        LogUtils.warn("Library file SHA1 mismatch: " + libraryFile);
                        return false;
                    }
                } catch (Exception e) {
                    LogUtils.error("Failed to calculate SHA1: " + libraryFile, e);
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /**
     * 计算文件SHA1
     * Calculate file SHA1
     * 
     * @param file 文件 File
     * @return SHA1值 SHA1 value
     * @throws IOException IO异常 IO exception
     * @throws NoSuchAlgorithmException 算法异常 Algorithm exception
     */
    private String calculateSha1(Path file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        try (java.io.InputStream fis = Files.newInputStream(file)) {
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }
        
        byte[] sha1Bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : sha1Bytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
} 