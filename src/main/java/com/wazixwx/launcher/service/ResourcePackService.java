package com.wazixwx.launcher.service;

import com.wazixwx.launcher.core.ResourcePackManager;
import com.wazixwx.launcher.model.ResourcePack;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 资源包服务类
 * Resource Pack Service Class
 * 
 * 提供资源包的管理功能
 * Provides resource pack management functionality
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ResourcePackService {
    private static ResourcePackService instance;
    private final ResourcePackManager resourcePackManager;
    
    private ResourcePackService() {
        this.resourcePackManager = ResourcePackManager.getInstance();
    }
    
    /**
     * 获取单例实例
     * Get singleton instance
     * 
     * @return 资源包服务实例 | Resource pack service instance
     */
    public static synchronized ResourcePackService getInstance() {
        if (instance == null) {
            instance = new ResourcePackService();
        }
        return instance;
    }
    
    /**
     * 加载资源包
     * Load resource pack
     * 
     * @param filePath 资源包文件路径 | Resource pack file path
     * @return 资源包对象 | Resource pack object
     */
    public CompletableFuture<ResourcePack> loadResourcePack(Path filePath) {
        return resourcePackManager.loadResourcePack(filePath);
    }
    
    /**
     * 保存资源包
     * Save resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否保存成功 | Whether save was successful
     */
    public CompletableFuture<Boolean> saveResourcePack(ResourcePack resourcePack) {
        return resourcePackManager.saveResourcePack(resourcePack);
    }
    
    /**
     * 删除资源包
     * Delete resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否删除成功 | Whether deletion was successful
     */
    public CompletableFuture<Boolean> deleteResourcePack(ResourcePack resourcePack) {
        return resourcePackManager.deleteResourcePack(resourcePack);
    }
    
    /**
     * 获取所有资源包
     * Get all resource packs
     * 
     * @return 资源包列表 | Resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getAllResourcePacks() {
        return resourcePackManager.getAllResourcePacks();
    }
    
    /**
     * 获取启用的资源包
     * Get enabled resource packs
     * 
     * @return 启用的资源包列表 | Enabled resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getEnabledResourcePacks() {
        return resourcePackManager.getEnabledResourcePacks();
    }
    
    /**
     * 获取用户资源包
     * Get user resource packs
     * 
     * @param userId 用户ID | User ID
     * @return 用户资源包列表 | User resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getUserResourcePacks(UUID userId) {
        return resourcePackManager.getUserResourcePacks(userId);
    }
    
    /**
     * 启用资源包
     * Enable resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否启用成功 | Whether enabling was successful
     */
    public CompletableFuture<Boolean> enableResourcePack(ResourcePack resourcePack) {
        return resourcePackManager.enableResourcePack(resourcePack);
    }
    
    /**
     * 禁用资源包
     * Disable resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否禁用成功 | Whether disabling was successful
     */
    public CompletableFuture<Boolean> disableResourcePack(ResourcePack resourcePack) {
        return resourcePackManager.disableResourcePack(resourcePack);
    }
    
    /**
     * 设置资源包优先级
     * Set resource pack priority
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @param priority 优先级 | Priority
     * @return 是否设置成功 | Whether setting was successful
     */
    public CompletableFuture<Boolean> setResourcePackPriority(ResourcePack resourcePack, int priority) {
        return resourcePackManager.setResourcePackPriority(resourcePack, priority);
    }
    
    /**
     * 验证资源包
     * Validate resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否验证成功 | Whether validation was successful
     */
    public CompletableFuture<Boolean> validateResourcePack(ResourcePack resourcePack) {
        return resourcePackManager.validateResourcePack(resourcePack);
    }
    
    /**
     * 扫描并加载所有资源包
     * Scan and load all resource packs
     * 
     * @return 资源包列表 | Resource pack list
     */
    public CompletableFuture<List<ResourcePack>> scanAndLoadResourcePacks() {
        return resourcePackManager.scanAndLoadResourcePacks();
    }
    
    /**
     * 获取资源包目录路径
     * Get resource packs directory path
     * 
     * @return 资源包目录路径 | Resource packs directory path
     */
    public Path getResourcePacksDir() {
        return resourcePackManager.getResourcePacksDir();
    }
} 