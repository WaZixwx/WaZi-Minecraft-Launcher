package com.wazixwx.launcher.model;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * 模组数据模型类
 * Mod Model Class
 * 
 * 提供对Minecraft模组的数据模型支持
 * Provides data model support for Minecraft mods
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class Mod {
    private final String id;
    private final String name;
    private final String description;
    private final String version;
    private final String author;
    private final List<String> compatibleMinecraftVersions;
    private final Path filePath;
    private final String fileHash;
    private boolean enabled;
    private final ModType modType;
    private final String modLoaderType; // Forge, Fabric, Quilt, etc.
    
    /**
     * 模组类型枚举
     * Mod Type Enumeration
     */
    public enum ModType {
        /**
         * 常规模组
         * Normal mod
         */
        NORMAL,
        
        /**
         * 核心模组 (如OptiFine)
         * Core mod (like OptiFine)
         */
        CORE,
        
        /**
         * 资源模组 (主要提供资源)
         * Resource mod (mainly provides resources)
         */
        RESOURCE,
        
        /**
         * 模组加载器 (如Forge, Fabric)
         * Mod loader (like Forge, Fabric)
         */
        LOADER,
        
        /**
         * API模组 (为其他模组提供API)
         * API mod (provides API for other mods)
         */
        API,
        
        /**
         * 未知类型
         * Unknown type
         */
        UNKNOWN
    }
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param name 模组名称 | Mod name
     * @param description 模组描述 | Mod description
     * @param version 模组版本 | Mod version
     * @param author 模组作者 | Mod author
     * @param compatibleMinecraftVersions 兼容的Minecraft版本列表 | List of compatible Minecraft versions
     * @param filePath 模组文件路径 | Mod file path
     * @param fileHash 模组文件哈希 | Mod file hash
     * @param enabled 是否启用 | Whether enabled
     * @param modType 模组类型 | Mod type
     * @param modLoaderType 模组加载器类型 | Mod loader type
     */
    public Mod(String name, String description, String version, String author,
               List<String> compatibleMinecraftVersions, Path filePath, String fileHash,
               boolean enabled, ModType modType, String modLoaderType) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.description = description;
        this.version = version;
        this.author = author;
        this.compatibleMinecraftVersions = compatibleMinecraftVersions;
        this.filePath = filePath;
        this.fileHash = fileHash;
        this.enabled = enabled;
        this.modType = modType;
        this.modLoaderType = modLoaderType;
    }
    
    /**
     * 获取模组ID
     * Get mod ID
     * 
     * @return 模组ID | Mod ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * 获取模组名称
     * Get mod name
     * 
     * @return 模组名称 | Mod name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取模组描述
     * Get mod description
     * 
     * @return 模组描述 | Mod description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 获取模组版本
     * Get mod version
     * 
     * @return 模组版本 | Mod version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 获取模组作者
     * Get mod author
     * 
     * @return 模组作者 | Mod author
     */
    public String getAuthor() {
        return author;
    }
    
    /**
     * 获取兼容的Minecraft版本列表
     * Get list of compatible Minecraft versions
     * 
     * @return 兼容的Minecraft版本列表 | List of compatible Minecraft versions
     */
    public List<String> getCompatibleMinecraftVersions() {
        return compatibleMinecraftVersions;
    }
    
    /**
     * 获取模组文件路径
     * Get mod file path
     * 
     * @return 模组文件路径 | Mod file path
     */
    public Path getFilePath() {
        return filePath;
    }
    
    /**
     * 获取模组文件哈希
     * Get mod file hash
     * 
     * @return 模组文件哈希 | Mod file hash
     */
    public String getFileHash() {
        return fileHash;
    }
    
    /**
     * 检查模组是否启用
     * Check if mod is enabled
     * 
     * @return 是否启用 | Whether enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * 设置模组启用状态
     * Set mod enabled status
     * 
     * @param enabled 是否启用 | Whether enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * 获取模组类型
     * Get mod type
     * 
     * @return 模组类型 | Mod type
     */
    public ModType getModType() {
        return modType;
    }
    
    /**
     * 获取模组加载器类型
     * Get mod loader type
     * 
     * @return 模组加载器类型 | Mod loader type
     */
    public String getModLoaderType() {
        return modLoaderType;
    }
    
    /**
     * 检查模组是否与指定的Minecraft版本兼容
     * Check if mod is compatible with the specified Minecraft version
     * 
     * @param minecraftVersion Minecraft版本 | Minecraft version
     * @return 是否兼容 | Whether compatible
     */
    public boolean isCompatibleWith(String minecraftVersion) {
        return compatibleMinecraftVersions.contains(minecraftVersion);
    }
    
    /**
     * 获取模组信息的字符串表示
     * Get string representation of mod information
     * 
     * @return 模组信息字符串 | Mod information string
     */
    @Override
    public String toString() {
        return name + " v" + version + " (" + modLoaderType + ")";
    }
} 