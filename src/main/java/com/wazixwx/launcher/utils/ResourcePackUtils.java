package com.wazixwx.launcher.utils;

import com.wazixwx.launcher.model.ResourcePack;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 资源包工具类
 * Resource Pack Utils
 * 
 * 提供处理资源包的工具方法
 * Provides utility methods for resource packs
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ResourcePackUtils {
    private static final Logger logger = LoggerFactory.getLogger(ResourcePackUtils.class);
    private static final Image DEFAULT_ICON = new Image(ResourcePackUtils.class.getResourceAsStream("/images/default_pack.png"));
    private static final Map<String, Image> iconCache = new HashMap<>();
    
    /**
     * 加载资源包图标
     * Load resource pack icon
     * 
     * @param resourcePack 资源包 | Resource pack
     * @return 资源包图标图像 | Resource pack icon image
     */
    public static Image loadResourcePackIcon(ResourcePack resourcePack) {
        // 检查缓存
        // Check cache
        String hash = resourcePack.getHash();
        if (iconCache.containsKey(hash)) {
            return iconCache.get(hash);
        }
        
        try {
            Path packPath = resourcePack.getFilePath();
            Image icon = null;
            
            if (Files.isDirectory(packPath)) {
                // 如果是目录，直接从目录中加载
                // If it's a directory, load directly from directory
                Path iconPath = packPath.resolve("pack.png");
                if (Files.exists(iconPath)) {
                    icon = new Image(Files.newInputStream(iconPath));
                }
            } else {
                // 如果是ZIP文件，从ZIP中加载
                // If it's a ZIP file, load from ZIP
                try (ZipFile zipFile = new ZipFile(packPath.toFile())) {
                    ZipEntry entry = zipFile.getEntry("pack.png");
                    if (entry != null) {
                        try (InputStream stream = zipFile.getInputStream(entry)) {
                            // 读取到字节数组，然后创建Image
                            // Read to byte array, then create Image
                            byte[] iconData = stream.readAllBytes();
                            icon = new Image(new ByteArrayInputStream(iconData));
                        }
                    }
                }
            }
            
            // 如果找不到图标，使用默认图标
            // If icon not found, use default icon
            if (icon == null) {
                icon = DEFAULT_ICON;
            }
            
            // 添加到缓存
            // Add to cache
            iconCache.put(hash, icon);
            
            return icon;
        } catch (IOException e) {
            logger.error("加载资源包图标失败 | Failed to load resource pack icon: {}", resourcePack.getName(), e);
            return DEFAULT_ICON;
        }
    }
    
    /**
     * 清除图标缓存
     * Clear icon cache
     */
    public static void clearIconCache() {
        iconCache.clear();
    }
    
    /**
     * 移除资源包的图标缓存
     * Remove resource pack's icon cache
     * 
     * @param resourcePack 资源包 | Resource pack
     */
    public static void removeIconCache(ResourcePack resourcePack) {
        iconCache.remove(resourcePack.getHash());
    }
    
    /**
     * 获取资源包格式版本名称
     * Get resource pack format version name
     * 
     * @param formatVersion 格式版本 | Format version
     * @return 版本名称 | Version name
     */
    public static String getFormatVersionName(String formatVersion) {
        try {
            int format = Integer.parseInt(formatVersion);
            return switch (format) {
                case 1 -> "1.6.1 - 1.8.9";
                case 2 -> "1.9 - 1.10.2";
                case 3 -> "1.11 - 1.12.2";
                case 4 -> "1.13 - 1.14.4";
                case 5 -> "1.15 - 1.16.1";
                case 6 -> "1.16.2 - 1.16.5";
                case 7 -> "1.17 - 1.17.1";
                case 8 -> "1.18 - 1.18.2";
                case 9 -> "1.19 - 1.19.2";
                case 11 -> "1.19.3";
                case 13 -> "1.19.4";
                case 15 -> "1.20 - 1.20.5";
                default -> "未知版本 | Unknown version";
            };
        } catch (NumberFormatException e) {
            return "未知版本 | Unknown version";
        }
    }
    
    /**
     * 判断资源包是否与游戏版本兼容
     * Check if resource pack is compatible with game version
     * 
     * @param resourcePack 资源包 | Resource pack
     * @param gameVersion 游戏版本 | Game version
     * @return 是否兼容 | Whether compatible
     */
    public static boolean isCompatible(ResourcePack resourcePack, String gameVersion) {
        try {
            String formatVersionStr = resourcePack.getVersion();
            int formatVersion = Integer.parseInt(formatVersionStr);
            
            // 简化的版本比较逻辑
            // Simplified version comparison logic
            if (gameVersion.startsWith("1.20")) {
                return formatVersion >= 12;
            } else if (gameVersion.startsWith("1.19.4")) {
                return formatVersion >= 12 && formatVersion <= 14;
            } else if (gameVersion.startsWith("1.19.3")) {
                return formatVersion >= 10 && formatVersion <= 12;
            } else if (gameVersion.startsWith("1.19")) {
                return formatVersion >= 8 && formatVersion <= 10;
            } else if (gameVersion.startsWith("1.18")) {
                return formatVersion >= 7 && formatVersion <= 9;
            } else if (gameVersion.startsWith("1.17")) {
                return formatVersion >= 6 && formatVersion <= 8;
            } else if (gameVersion.startsWith("1.16.2") || 
                      gameVersion.startsWith("1.16.3") || 
                      gameVersion.startsWith("1.16.4") || 
                      gameVersion.startsWith("1.16.5")) {
                return formatVersion >= 5 && formatVersion <= 7;
            } else if (gameVersion.startsWith("1.16")) {
                return formatVersion >= 4 && formatVersion <= 6;
            } else if (gameVersion.startsWith("1.15")) {
                return formatVersion >= 4 && formatVersion <= 6;
            } else if (gameVersion.startsWith("1.14")) {
                return formatVersion >= 3 && formatVersion <= 5;
            } else if (gameVersion.startsWith("1.13")) {
                return formatVersion >= 3 && formatVersion <= 5;
            } else if (gameVersion.startsWith("1.12")) {
                return formatVersion >= 2 && formatVersion <= 4;
            } else if (gameVersion.startsWith("1.11")) {
                return formatVersion >= 2 && formatVersion <= 4;
            } else if (gameVersion.startsWith("1.10")) {
                return formatVersion >= 1 && formatVersion <= 3;
            } else if (gameVersion.startsWith("1.9")) {
                return formatVersion >= 1 && formatVersion <= 3;
            } else if (gameVersion.startsWith("1.8")) {
                return formatVersion >= 0 && formatVersion <= 2;
            }
            
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 