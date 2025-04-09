package com.wazixwx.launcher.model;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 资源包模型类
 * Resource Pack Model Class
 * 
 * 表示一个Minecraft资源包
 * Represents a Minecraft resource pack
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ResourcePack {
    private String name;          // 资源包名称 | Resource pack name
    private String description;   // 资源包描述 | Resource pack description
    private String version;       // 资源包版本 | Resource pack version
    private Path filePath;        // 文件路径 | File path
    private String hash;          // 文件哈希 | File hash
    private UUID ownerId;         // 所有者ID | Owner ID
    private boolean isEnabled;    // 是否启用 | Is enabled
    private int priority;         // 优先级 | Priority
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param name 资源包名称 | Resource pack name
     * @param description 资源包描述 | Resource pack description
     * @param version 资源包版本 | Resource pack version
     * @param filePath 文件路径 | File path
     * @param hash 文件哈希 | File hash
     * @param ownerId 所有者ID | Owner ID
     * @param isEnabled 是否启用 | Is enabled
     * @param priority 优先级 | Priority
     */
    public ResourcePack(String name, String description, String version, Path filePath, 
            String hash, UUID ownerId, boolean isEnabled, int priority) {
        this.name = name;
        this.description = description;
        this.version = version;
        this.filePath = filePath;
        this.hash = hash;
        this.ownerId = ownerId;
        this.isEnabled = isEnabled;
        this.priority = priority;
    }
    
    /**
     * 构造函数（自定义资源包）
     * Constructor (custom resource pack)
     * 
     * @param name 资源包名称 | Resource pack name
     * @param description 资源包描述 | Resource pack description
     * @param version 资源包版本 | Resource pack version
     * @param filePath 文件路径 | File path
     * @param hash 文件哈希 | File hash
     * @param isEnabled 是否启用 | Is enabled
     * @param priority 优先级 | Priority
     */
    public ResourcePack(String name, String description, String version, Path filePath, 
            String hash, boolean isEnabled, int priority) {
        this(name, description, version, filePath, hash, null, isEnabled, priority);
    }
    
    /**
     * 获取资源包名称
     * Get resource pack name
     * 
     * @return 资源包名称 | Resource pack name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置资源包名称
     * Set resource pack name
     * 
     * @param name 资源包名称 | Resource pack name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取资源包描述
     * Get resource pack description
     * 
     * @return 资源包描述 | Resource pack description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * 设置资源包描述
     * Set resource pack description
     * 
     * @param description 资源包描述 | Resource pack description
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 获取资源包版本
     * Get resource pack version
     * 
     * @return 资源包版本 | Resource pack version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * 设置资源包版本
     * Set resource pack version
     * 
     * @param version 资源包版本 | Resource pack version
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * 获取文件路径
     * Get file path
     * 
     * @return 文件路径 | File path
     */
    public Path getFilePath() {
        return filePath;
    }
    
    /**
     * 设置文件路径
     * Set file path
     * 
     * @param filePath 文件路径 | File path
     */
    public void setFilePath(Path filePath) {
        this.filePath = filePath;
    }
    
    /**
     * 获取文件哈希
     * Get file hash
     * 
     * @return 文件哈希 | File hash
     */
    public String getHash() {
        return hash;
    }
    
    /**
     * 设置文件哈希
     * Set file hash
     * 
     * @param hash 文件哈希 | File hash
     */
    public void setHash(String hash) {
        this.hash = hash;
    }
    
    /**
     * 获取所有者ID
     * Get owner ID
     * 
     * @return 所有者ID | Owner ID
     */
    public UUID getOwnerId() {
        return ownerId;
    }
    
    /**
     * 设置所有者ID
     * Set owner ID
     * 
     * @param ownerId 所有者ID | Owner ID
     */
    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }
    
    /**
     * 是否启用
     * Is enabled
     * 
     * @return 是否启用 | Is enabled
     */
    public boolean isEnabled() {
        return isEnabled;
    }
    
    /**
     * 设置是否启用
     * Set is enabled
     * 
     * @param enabled 是否启用 | Is enabled
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }
    
    /**
     * 获取优先级
     * Get priority
     * 
     * @return 优先级 | Priority
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * 设置优先级
     * Set priority
     * 
     * @param priority 优先级 | Priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
} 