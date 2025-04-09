package com.wazixwx.launcher.model;

import java.nio.file.Path;
import java.util.UUID;

/**
 * 皮肤模型类
 * Skin Model Class
 * 
 * 表示一个Minecraft皮肤
 * Represents a Minecraft skin
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class Skin {
    private String name;          // 皮肤名称 | Skin name
    private String type;          // 皮肤类型 | Skin type (default/slim)
    private Path filePath;        // 文件路径 | File path
    private String hash;          // 文件哈希 | File hash
    private UUID ownerId;         // 所有者ID | Owner ID
    private boolean isCustom;     // 是否为自定义皮肤 | Is custom skin
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param name 皮肤名称 | Skin name
     * @param type 皮肤类型 | Skin type (default/slim)
     * @param filePath 文件路径 | File path
     * @param hash 文件哈希 | File hash
     * @param ownerId 所有者ID | Owner ID
     * @param isCustom 是否为自定义皮肤 | Is custom skin
     */
    public Skin(String name, String type, Path filePath, String hash, UUID ownerId, boolean isCustom) {
        this.name = name;
        this.type = type;
        this.filePath = filePath;
        this.hash = hash;
        this.ownerId = ownerId;
        this.isCustom = isCustom;
    }
    
    /**
     * 构造函数（自定义皮肤）
     * Constructor (custom skin)
     * 
     * @param name 皮肤名称 | Skin name
     * @param type 皮肤类型 | Skin type (default/slim)
     * @param filePath 文件路径 | File path
     * @param hash 文件哈希 | File hash
     */
    public Skin(String name, String type, Path filePath, String hash) {
        this(name, type, filePath, hash, null, true);
    }
    
    /**
     * 获取皮肤名称
     * Get skin name
     * 
     * @return 皮肤名称 | Skin name
     */
    public String getName() {
        return name;
    }
    
    /**
     * 设置皮肤名称
     * Set skin name
     * 
     * @param name 皮肤名称 | Skin name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 获取皮肤类型
     * Get skin type
     * 
     * @return 皮肤类型 | Skin type (default/slim)
     */
    public String getType() {
        return type;
    }
    
    /**
     * 设置皮肤类型
     * Set skin type
     * 
     * @param type 皮肤类型 | Skin type (default/slim)
     */
    public void setType(String type) {
        this.type = type;
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
     * 是否为自定义皮肤
     * Is custom skin
     * 
     * @return 是否为自定义皮肤 | Is custom skin
     */
    public boolean isCustom() {
        return isCustom;
    }
    
    /**
     * 设置是否为自定义皮肤
     * Set is custom skin
     * 
     * @param custom 是否为自定义皮肤 | Is custom skin
     */
    public void setCustom(boolean custom) {
        isCustom = custom;
    }
    
    /**
     * 检查皮肤是否为Alex模型（苗条手臂）
     * Check if the skin is Alex model (slim arms)
     * 
     * @return 是否为Alex模型 | Is Alex model
     */
    public boolean isSlimModel() {
        return "slim".equals(type);
    }
    
    /**
     * 获取皮肤类型显示名称
     * Get skin type display name
     * 
     * @return 皮肤类型显示名称 | Skin type display name
     */
    public String getTypeDisplayName() {
        return isSlimModel() ? "Alex" : "Steve";
    }
} 