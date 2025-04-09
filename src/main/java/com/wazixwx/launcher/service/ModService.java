package com.wazixwx.launcher.service;

import com.wazixwx.launcher.core.ModManager;
import com.wazixwx.launcher.model.Mod;
import com.wazixwx.launcher.utils.LogUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 模组服务类
 * Mod Service Class
 * 
 * 提供模组的管理服务，包括安装、卸载、启用、禁用等功能
 * Provides mod management services, including installation, uninstallation, enabling, disabling, etc.
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ModService {
    private final ModManager modManager;
    
    /**
     * 构造函数
     * Constructor
     */
    public ModService() {
        this.modManager = ModManager.getInstance();
    }
    
    /**
     * 安装模组
     * Install mod
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 安装结果 | Installation result
     */
    public CompletableFuture<Boolean> installMod(Path modPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(modPath)) {
                    LogUtils.error("模组文件不存在 | Mod file does not exist: {}", modPath);
                    return false;
                }
                
                // 解析并加载模组
                CompletableFuture<Mod> modFuture = modManager.loadMod(modPath);
                Mod mod = modFuture.join();
                
                if (mod == null) {
                    LogUtils.error("无法加载模组 | Cannot load mod: {}", modPath);
                    return false;
                }
                
                // 保存模组到模组目录
                CompletableFuture<Boolean> saveFuture = modManager.saveMod(mod);
                return saveFuture.join();
                
            } catch (Exception e) {
                LogUtils.error("安装模组失败 | Failed to install mod: {}", modPath, e);
                return false;
            }
        });
    }
    
    /**
     * 卸载模组
     * Uninstall mod
     * 
     * @param mod 要卸载的模组 | Mod to uninstall
     * @return 卸载结果 | Uninstallation result
     */
    public CompletableFuture<Boolean> uninstallMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return modManager.deleteMod(mod).join();
            } catch (Exception e) {
                LogUtils.error("卸载模组失败 | Failed to uninstall mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 启用模组
     * Enable mod
     * 
     * @param mod 要启用的模组 | Mod to enable
     * @return 启用结果 | Enabling result
     */
    public CompletableFuture<Boolean> enableMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return modManager.enableMod(mod).join();
            } catch (Exception e) {
                LogUtils.error("启用模组失败 | Failed to enable mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 禁用模组
     * Disable mod
     * 
     * @param mod 要禁用的模组 | Mod to disable
     * @return 禁用结果 | Disabling result
     */
    public CompletableFuture<Boolean> disableMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return modManager.disableMod(mod).join();
            } catch (Exception e) {
                LogUtils.error("禁用模组失败 | Failed to disable mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 获取所有已安装的模组
     * Get all installed mods
     * 
     * @return 模组列表 | Mod list
     */
    public CompletableFuture<List<Mod>> getAllMods() {
        return modManager.getAllMods();
    }
    
    /**
     * 获取所有已启用的模组
     * Get all enabled mods
     * 
     * @return 已启用的模组列表 | Enabled mod list
     */
    public CompletableFuture<List<Mod>> getEnabledMods() {
        return modManager.getEnabledMods();
    }
    
    /**
     * 获取指定Minecraft版本兼容的模组
     * Get mods compatible with the specified Minecraft version
     * 
     * @param minecraftVersion Minecraft版本 | Minecraft version
     * @return 兼容的模组列表 | Compatible mod list
     */
    public CompletableFuture<List<Mod>> getCompatibleMods(String minecraftVersion) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Mod> allMods = modManager.getAllMods().join();
                return allMods.stream()
                    .filter(mod -> mod.isCompatibleWith(minecraftVersion))
                    .toList();
            } catch (Exception e) {
                LogUtils.error("获取兼容模组失败 | Failed to get compatible mods", e);
                return List.of();
            }
        });
    }
    
    /**
     * 检查模组和Minecraft版本的兼容性
     * Check compatibility between mod and Minecraft version
     * 
     * @param mod 模组 | Mod
     * @param minecraftVersion Minecraft版本 | Minecraft version
     * @return 是否兼容 | Whether compatible
     */
    public boolean checkCompatibility(Mod mod, String minecraftVersion) {
        return mod.isCompatibleWith(minecraftVersion);
    }
    
    /**
     * 识别模组类型
     * Identify mod type
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 模组类型 | Mod type
     */
    public CompletableFuture<Mod.ModType> identifyModType(Path modPath) {
        return modManager.identifyModType(modPath);
    }
    
    /**
     * 识别模组加载器类型
     * Identify mod loader type
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 模组加载器类型 | Mod loader type
     */
    public CompletableFuture<String> identifyModLoaderType(Path modPath) {
        return modManager.identifyModLoaderType(modPath);
    }
    
    /**
     * 从模组中提取元数据信息
     * Extract metadata from mod
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 模组数据 | Mod data
     */
    public CompletableFuture<Mod> extractModMetadata(Path modPath) {
        return modManager.loadMod(modPath);
    }
} 