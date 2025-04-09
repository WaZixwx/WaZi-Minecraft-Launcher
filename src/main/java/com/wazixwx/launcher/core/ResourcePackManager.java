package com.wazixwx.launcher.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wazixwx.launcher.model.ResourcePack;
import com.wazixwx.launcher.utils.ConfigManager;
import com.wazixwx.launcher.utils.LogUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 资源包管理器类
 * Resource Pack Manager Class
 * 
 * 提供资源包解析和操作的核心功能
 * Provides core functionality for resource pack parsing and operations
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ResourcePackManager {
    private static final Gson GSON = new Gson();
    private static ResourcePackManager instance;
    private final Path resourcePacksDir;
    private final Path enabledPacksConfigPath;
    private final List<ResourcePack> resourcePacks = new ArrayList<>();
    private final Map<UUID, List<ResourcePack>> userResourcePacks = new HashMap<>();
    
    /**
     * 私有构造函数
     * Private constructor
     */
    private ResourcePackManager() {
        // 获取配置的Minecraft目录
        // Get configured Minecraft directory
        Path minecraftDir = Paths.get(ConfigManager.getInstance().getMinecraftDirectory());
        
        // 创建资源包目录
        // Create resource packs directory
        resourcePacksDir = minecraftDir.resolve("resourcepacks");
        try {
            Files.createDirectories(resourcePacksDir);
        } catch (IOException e) {
            LogUtils.error("创建资源包目录失败 | Failed to create resource packs directory", e);
        }
        
        // 启用的资源包配置文件路径
        // Path to enabled resource packs configuration
        enabledPacksConfigPath = minecraftDir.resolve("launcher").resolve("enabled_resource_packs.json");
        try {
            Files.createDirectories(enabledPacksConfigPath.getParent());
        } catch (IOException e) {
            LogUtils.error("创建启动器配置目录失败 | Failed to create launcher config directory", e);
        }
    }
    
    /**
     * 获取单例实例
     * Get singleton instance
     * 
     * @return 资源包管理器实例 | Resource pack manager instance
     */
    public static synchronized ResourcePackManager getInstance() {
        if (instance == null) {
            instance = new ResourcePackManager();
        }
        return instance;
    }
    
    /**
     * 获取资源包目录路径
     * Get resource packs directory path
     * 
     * @return 资源包目录路径 | Resource packs directory path
     */
    public Path getResourcePacksDir() {
        return resourcePacksDir;
    }
    
    /**
     * 加载资源包
     * Load resource pack
     * 
     * @param filePath 资源包文件路径 | Resource pack file path
     * @return 资源包对象 | Resource pack object
     */
    public CompletableFuture<ResourcePack> loadResourcePack(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(filePath)) {
                    LogUtils.error("资源包文件不存在 | Resource pack file does not exist: {}", filePath);
                    return null;
                }
                
                // 计算文件哈希
                // Calculate file hash
                String hash = calculateFileHash(filePath);
                
                // 读取pack.mcmeta文件
                // Read pack.mcmeta file
                JsonObject packInfo = readPackMcmeta(filePath);
                if (packInfo == null) {
                    LogUtils.error("无效的资源包，缺少pack.mcmeta文件 | Invalid resource pack, missing pack.mcmeta file: {}", filePath);
                    return null;
                }
                
                JsonObject packData = packInfo.getAsJsonObject("pack");
                String format = packData.has("pack_format") ? packData.get("pack_format").getAsString() : "unknown";
                String description = packData.has("description") ? packData.get("description").getAsString() : "No description";
                
                // 获取资源包名称（通常是文件名）
                // Get resource pack name (usually the file name)
                String name = filePath.getFileName().toString();
                if (name.endsWith(".zip")) {
                    name = name.substring(0, name.length() - 4);
                }
                
                // 创建资源包对象
                // Create resource pack object
                ResourcePack resourcePack = new ResourcePack(
                    name,
                    description,
                    format,
                    filePath,
                    hash,
                    false,  // 默认未启用 | Disabled by default
                    0       // 默认优先级 | Default priority
                );
                
                return resourcePack;
            } catch (Exception e) {
                LogUtils.error("加载资源包失败 | Failed to load resource pack: {}", filePath, e);
                return null;
            }
        });
    }
    
    /**
     * 读取pack.mcmeta文件
     * Read pack.mcmeta file
     * 
     * @param packPath 资源包路径 | Resource pack path
     * @return pack.mcmeta的JSON对象 | JSON object of pack.mcmeta
     */
    private JsonObject readPackMcmeta(Path packPath) {
        try {
            if (Files.isDirectory(packPath)) {
                // 如果是目录，直接读取pack.mcmeta文件
                // If it's a directory, read pack.mcmeta file directly
                Path mcmetaPath = packPath.resolve("pack.mcmeta");
                if (Files.exists(mcmetaPath)) {
                    String content = new String(Files.readAllBytes(mcmetaPath), StandardCharsets.UTF_8);
                    return JsonParser.parseString(content).getAsJsonObject();
                }
            } else {
                // 如果是ZIP文件，从ZIP中读取pack.mcmeta
                // If it's a ZIP file, read pack.mcmeta from the ZIP
                try (ZipFile zipFile = new ZipFile(packPath.toFile())) {
                    ZipEntry entry = zipFile.getEntry("pack.mcmeta");
                    if (entry != null) {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry), StandardCharsets.UTF_8))) {
                            StringBuilder content = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                content.append(line);
                            }
                            return JsonParser.parseString(content.toString()).getAsJsonObject();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.error("读取pack.mcmeta失败 | Failed to read pack.mcmeta", e);
        }
        return null;
    }
    
    /**
     * 计算文件哈希值
     * Calculate file hash
     * 
     * @param filePath 文件路径 | File path
     * @return 文件SHA-256哈希值 | File SHA-256 hash
     */
    private String calculateFileHash(Path filePath) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = digest.digest(fileBytes);
            
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            LogUtils.error("计算文件哈希失败 | Failed to calculate file hash", e);
            return "";
        }
    }
    
    /**
     * 扫描并加载所有资源包
     * Scan and load all resource packs
     * 
     * @return 资源包列表 | Resource pack list
     */
    public CompletableFuture<List<ResourcePack>> scanAndLoadResourcePacks() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 清空当前列表
                // Clear current list
                resourcePacks.clear();
                
                // 读取启用的资源包配置
                // Read enabled resource packs configuration
                Map<String, Integer> enabledPacks = loadEnabledPacksConfig();
                
                // 扫描资源包目录
                // Scan resource packs directory
                List<Path> packPaths = Files.list(resourcePacksDir)
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return Files.isDirectory(path) || fileName.endsWith(".zip");
                    })
                    .collect(Collectors.toList());
                
                // 加载所有资源包
                // Load all resource packs
                List<CompletableFuture<ResourcePack>> futures = new ArrayList<>();
                for (Path packPath : packPaths) {
                    futures.add(loadResourcePack(packPath));
                }
                
                // 等待所有资源包加载完成
                // Wait for all resource packs to be loaded
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                
                // 收集结果
                // Collect results
                for (CompletableFuture<ResourcePack> future : futures) {
                    ResourcePack pack = future.join();
                    if (pack != null) {
                        // 设置启用状态和优先级
                        // Set enabled state and priority
                        if (enabledPacks.containsKey(pack.getHash())) {
                            pack.setEnabled(true);
                            pack.setPriority(enabledPacks.get(pack.getHash()));
                        }
                        resourcePacks.add(pack);
                    }
                }
                
                // 按优先级排序
                // Sort by priority
                resourcePacks.sort(Comparator.comparingInt(ResourcePack::getPriority).reversed());
                
                return resourcePacks;
            } catch (Exception e) {
                LogUtils.error("扫描资源包失败 | Failed to scan resource packs", e);
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * 保存资源包
     * Save resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否保存成功 | Whether save was successful
     */
    public CompletableFuture<Boolean> saveResourcePack(ResourcePack resourcePack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path sourcePath = resourcePack.getFilePath();
                Path targetPath = resourcePacksDir.resolve(sourcePath.getFileName());
                
                // 如果源文件不在资源包目录中，复制到资源包目录
                // If source file is not in resource packs directory, copy it to resource packs directory
                if (!sourcePath.startsWith(resourcePacksDir)) {
                    Files.copy(sourcePath, targetPath);
                    resourcePack.setFilePath(targetPath);
                }
                
                // 添加到资源包列表
                // Add to resource pack list
                if (!resourcePacks.contains(resourcePack)) {
                    resourcePacks.add(resourcePack);
                }
                
                // 保存启用的资源包配置
                // Save enabled resource packs configuration
                saveEnabledPacksConfig();
                
                return true;
            } catch (Exception e) {
                LogUtils.error("保存资源包失败 | Failed to save resource pack", e);
                return false;
            }
        });
    }
    
    /**
     * 删除资源包
     * Delete resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否删除成功 | Whether deletion was successful
     */
    public CompletableFuture<Boolean> deleteResourcePack(ResourcePack resourcePack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 从列表中移除
                // Remove from list
                resourcePacks.remove(resourcePack);
                
                // 如果资源包所在路径在资源包目录中，则删除文件
                // If resource pack is in resource packs directory, delete the file
                Path packPath = resourcePack.getFilePath();
                if (packPath.startsWith(resourcePacksDir)) {
                    Files.deleteIfExists(packPath);
                }
                
                // 保存启用的资源包配置
                // Save enabled resource packs configuration
                saveEnabledPacksConfig();
                
                return true;
            } catch (Exception e) {
                LogUtils.error("删除资源包失败 | Failed to delete resource pack", e);
                return false;
            }
        });
    }
    
    /**
     * 获取所有资源包
     * Get all resource packs
     * 
     * @return 资源包列表 | Resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getAllResourcePacks() {
        return CompletableFuture.supplyAsync(() -> new ArrayList<>(resourcePacks));
    }
    
    /**
     * 获取启用的资源包
     * Get enabled resource packs
     * 
     * @return 启用的资源包列表 | Enabled resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getEnabledResourcePacks() {
        return CompletableFuture.supplyAsync(() -> 
            resourcePacks.stream()
                .filter(ResourcePack::isEnabled)
                .sorted(Comparator.comparingInt(ResourcePack::getPriority).reversed())
                .collect(Collectors.toList())
        );
    }
    
    /**
     * 获取用户资源包
     * Get user resource packs
     * 
     * @param userId 用户ID | User ID
     * @return 用户资源包列表 | User resource pack list
     */
    public CompletableFuture<List<ResourcePack>> getUserResourcePacks(UUID userId) {
        return CompletableFuture.supplyAsync(() -> {
            if (userId == null) {
                return new ArrayList<>();
            }
            
            return resourcePacks.stream()
                .filter(pack -> userId.equals(pack.getOwnerId()))
                .collect(Collectors.toList());
        });
    }
    
    /**
     * 启用资源包
     * Enable resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否启用成功 | Whether enabling was successful
     */
    public CompletableFuture<Boolean> enableResourcePack(ResourcePack resourcePack) {
        return CompletableFuture.supplyAsync(() -> {
            resourcePack.setEnabled(true);
            
            // 如果优先级为0，设置为当前最高优先级+1
            // If priority is 0, set it to current highest priority + 1
            if (resourcePack.getPriority() == 0) {
                int highestPriority = resourcePacks.stream()
                    .filter(ResourcePack::isEnabled)
                    .map(ResourcePack::getPriority)
                    .max(Integer::compareTo)
                    .orElse(0);
                resourcePack.setPriority(highestPriority + 1);
            }
            
            saveEnabledPacksConfig();
            return true;
        });
    }
    
    /**
     * 禁用资源包
     * Disable resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否禁用成功 | Whether disabling was successful
     */
    public CompletableFuture<Boolean> disableResourcePack(ResourcePack resourcePack) {
        return CompletableFuture.supplyAsync(() -> {
            resourcePack.setEnabled(false);
            saveEnabledPacksConfig();
            return true;
        });
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
        return CompletableFuture.supplyAsync(() -> {
            resourcePack.setPriority(priority);
            
            // 重新排序
            // Re-sort
            resourcePacks.sort(Comparator.comparingInt(ResourcePack::getPriority).reversed());
            
            saveEnabledPacksConfig();
            return true;
        });
    }
    
    /**
     * 验证资源包
     * Validate resource pack
     * 
     * @param resourcePack 资源包对象 | Resource pack object
     * @return 是否验证成功 | Whether validation was successful
     */
    public CompletableFuture<Boolean> validateResourcePack(ResourcePack resourcePack) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path packPath = resourcePack.getFilePath();
                
                // 验证文件是否存在
                // Verify file exists
                if (!Files.exists(packPath)) {
                    LogUtils.error("资源包文件不存在 | Resource pack file does not exist: {}", packPath);
                    return false;
                }
                
                // 验证是否包含pack.mcmeta
                // Verify contains pack.mcmeta
                JsonObject packInfo = readPackMcmeta(packPath);
                if (packInfo == null) {
                    LogUtils.error("无效的资源包，缺少pack.mcmeta文件 | Invalid resource pack, missing pack.mcmeta file: {}", packPath);
                    return false;
                }
                
                // 验证资源包版本格式
                // Verify resource pack format version
                JsonObject packData = packInfo.getAsJsonObject("pack");
                if (!packData.has("pack_format")) {
                    LogUtils.error("无效的资源包，pack.mcmeta中缺少pack_format | Invalid resource pack, missing pack_format in pack.mcmeta: {}", packPath);
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                LogUtils.error("验证资源包失败 | Failed to validate resource pack", e);
                return false;
            }
        });
    }
    
    /**
     * 加载启用的资源包配置
     * Load enabled resource packs configuration
     * 
     * @return 启用的资源包配置 | Enabled resource packs configuration
     */
    private Map<String, Integer> loadEnabledPacksConfig() {
        Map<String, Integer> enabledPacks = new HashMap<>();
        try {
            if (Files.exists(enabledPacksConfigPath)) {
                String json = new String(Files.readAllBytes(enabledPacksConfigPath), StandardCharsets.UTF_8);
                JsonObject config = JsonParser.parseString(json).getAsJsonObject();
                
                for (Map.Entry<String, com.google.gson.JsonElement> entry : config.entrySet()) {
                    String hash = entry.getKey();
                    int priority = entry.getValue().getAsInt();
                    enabledPacks.put(hash, priority);
                }
            }
        } catch (Exception e) {
            LogUtils.error("加载启用的资源包配置失败 | Failed to load enabled resource packs configuration", e);
        }
        return enabledPacks;
    }
    
    /**
     * 保存启用的资源包配置
     * Save enabled resource packs configuration
     */
    private void saveEnabledPacksConfig() {
        try {
            JsonObject config = new JsonObject();
            
            for (ResourcePack pack : resourcePacks) {
                if (pack.isEnabled()) {
                    config.addProperty(pack.getHash(), pack.getPriority());
                }
            }
            
            Files.writeString(enabledPacksConfigPath, GSON.toJson(config), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LogUtils.error("保存启用的资源包配置失败 | Failed to save enabled resource packs configuration", e);
        }
    }
} 