package com.wazixwx.launcher.core;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wazixwx.launcher.model.Mod;
import com.wazixwx.launcher.utils.ConfigManager;
import com.wazixwx.launcher.utils.LogUtils;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 模组管理器类
 * Mod Manager Class
 * 
 * 提供模组的底层管理功能，读取、保存模组文件，解析模组元数据等
 * Provides low-level mod management capabilities, reading, saving mod files, parsing mod metadata, etc.
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class ModManager {
    private static final Gson GSON = new Gson();
    private static ModManager instance;
    private final Path modsDir;
    private final Path enabledModsConfigPath;
    private final List<Mod> mods = new ArrayList<>();
    
    /**
     * 私有构造函数
     * Private constructor
     */
    private ModManager() {
        // 获取配置的Minecraft目录
        // Get configured Minecraft directory
        Path minecraftDir = Paths.get(ConfigManager.getInstance().getMinecraftDirectory());
        
        // 创建模组目录
        // Create mods directory
        modsDir = minecraftDir.resolve("mods");
        try {
            Files.createDirectories(modsDir);
        } catch (IOException e) {
            LogUtils.error("创建模组目录失败 | Failed to create mods directory", e);
        }
        
        // 启用的模组配置文件路径
        // Path to enabled mods configuration
        enabledModsConfigPath = minecraftDir.resolve("launcher").resolve("enabled_mods.json");
        try {
            Files.createDirectories(enabledModsConfigPath.getParent());
        } catch (IOException e) {
            LogUtils.error("创建启动器配置目录失败 | Failed to create launcher config directory", e);
        }
    }
    
    /**
     * 获取单例实例
     * Get singleton instance
     * 
     * @return 模组管理器实例 | Mod manager instance
     */
    public static synchronized ModManager getInstance() {
        if (instance == null) {
            instance = new ModManager();
        }
        return instance;
    }
    
    /**
     * 获取模组目录路径
     * Get mods directory path
     * 
     * @return 模组目录路径 | Mods directory path
     */
    public Path getModsDir() {
        return modsDir;
    }
    
    /**
     * 加载模组
     * Load mod
     * 
     * @param filePath 模组文件路径 | Mod file path
     * @return 模组对象 | Mod object
     */
    public CompletableFuture<Mod> loadMod(Path filePath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (!Files.exists(filePath)) {
                    LogUtils.error("模组文件不存在 | Mod file does not exist: {}", filePath);
                    return null;
                }
                
                // 计算文件哈希
                // Calculate file hash
                String hash = calculateFileHash(filePath);
                
                // 提取模组元数据
                // Extract mod metadata
                Map<String, String> metadata = extractModMetadata(filePath);
                if (metadata.isEmpty()) {
                    LogUtils.error("无法提取模组元数据 | Cannot extract mod metadata: {}", filePath);
                    return null;
                }
                
                // 识别模组加载器类型
                // Identify mod loader type
                String modLoaderType = identifyModLoaderTypeFromMetadata(metadata);
                
                // 识别模组类型
                // Identify mod type
                Mod.ModType modType = identifyModTypeFromMetadata(metadata);
                
                // 获取模组名称（如果元数据中没有，则使用文件名）
                // Get mod name (use file name if not in metadata)
                String name = metadata.getOrDefault("name", filePath.getFileName().toString());
                if (name.endsWith(".jar")) {
                    name = name.substring(0, name.length() - 4);
                }
                
                // 获取兼容的Minecraft版本
                // Get compatible Minecraft versions
                List<String> compatibleVersions = parseCompatibleVersions(metadata.getOrDefault("mcversion", ""));
                
                // 创建模组对象
                // Create mod object
                Mod mod = new Mod(
                    name,
                    metadata.getOrDefault("description", "No description"),
                    metadata.getOrDefault("version", "Unknown"),
                    metadata.getOrDefault("author", metadata.getOrDefault("authors", "Unknown")),
                    compatibleVersions,
                    filePath,
                    hash,
                    false,  // 默认未启用 | Disabled by default
                    modType,
                    modLoaderType
                );
                
                return mod;
            } catch (Exception e) {
                LogUtils.error("加载模组失败 | Failed to load mod: {}", filePath, e);
                return null;
            }
        });
    }
    
    /**
     * 提取模组元数据
     * Extract mod metadata
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 元数据映射 | Metadata map
     */
    private Map<String, String> extractModMetadata(Path modPath) {
        Map<String, String> metadata = new HashMap<>();
        
        try {
            if (Files.isDirectory(modPath)) {
                // 如果是目录，检查不同类型的元数据文件
                // If it's a directory, check different types of metadata files
                extractMetadataFromDirectory(modPath, metadata);
            } else if (modPath.toString().endsWith(".jar")) {
                // 如果是JAR文件，检查内部的元数据文件
                // If it's a JAR file, check metadata files inside
                extractMetadataFromJar(modPath, metadata);
            }
        } catch (Exception e) {
            LogUtils.error("提取模组元数据失败 | Failed to extract mod metadata", e);
        }
        
        return metadata;
    }
    
    /**
     * 从目录中提取元数据
     * Extract metadata from directory
     * 
     * @param modPath 模组目录路径 | Mod directory path
     * @param metadata 元数据映射 | Metadata map
     */
    private void extractMetadataFromDirectory(Path modPath, Map<String, String> metadata) {
        // 检查Forge mod.toml
        Path forgeModToml = modPath.resolve("META-INF").resolve("mods.toml");
        if (Files.exists(forgeModToml)) {
            try {
                String content = new String(Files.readAllBytes(forgeModToml), StandardCharsets.UTF_8);
                // 简单解析TOML（实际应使用TOML解析库）
                // Simple TOML parsing (should use a TOML parsing library in real implementation)
                parseToml(content, metadata);
            } catch (IOException e) {
                LogUtils.error("读取Forge mods.toml失败 | Failed to read Forge mods.toml", e);
            }
        }
        
        // 检查Fabric fabric.mod.json
        Path fabricModJson = modPath.resolve("fabric.mod.json");
        if (Files.exists(fabricModJson)) {
            try {
                String content = new String(Files.readAllBytes(fabricModJson), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                extractJsonMetadata(json, metadata);
            } catch (IOException e) {
                LogUtils.error("读取Fabric fabric.mod.json失败 | Failed to read Fabric fabric.mod.json", e);
            }
        }
    }
    
    /**
     * 从JAR文件中提取元数据
     * Extract metadata from JAR file
     * 
     * @param modPath 模组JAR文件路径 | Mod JAR file path
     * @param metadata 元数据映射 | Metadata map
     */
    private void extractMetadataFromJar(Path modPath, Map<String, String> metadata) {
        try (ZipFile zipFile = new ZipFile(modPath.toFile())) {
            // 检查Forge mods.toml
            ZipEntry forgeEntry = zipFile.getEntry("META-INF/mods.toml");
            if (forgeEntry != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(forgeEntry), StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    parseToml(content.toString(), metadata);
                    metadata.put("type", "forge");
                }
            }
            
            // 检查Fabric fabric.mod.json
            ZipEntry fabricEntry = zipFile.getEntry("fabric.mod.json");
            if (fabricEntry != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(fabricEntry), StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    JsonObject json = JsonParser.parseString(content.toString()).getAsJsonObject();
                    extractJsonMetadata(json, metadata);
                    metadata.put("type", "fabric");
                }
            }
            
            // 检查旧版Forge mcmod.info
            ZipEntry oldForgeEntry = zipFile.getEntry("mcmod.info");
            if (oldForgeEntry != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(oldForgeEntry), StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line);
                    }
                    extractLegacyForgeMetadata(content.toString(), metadata);
                    metadata.put("type", "forge-legacy");
                }
            }
        } catch (IOException e) {
            LogUtils.error("从JAR提取元数据失败 | Failed to extract metadata from JAR", e);
        }
    }
    
    /**
     * 简单解析TOML格式
     * Simple TOML parsing
     * 
     * @param tomlContent TOML内容 | TOML content
     * @param metadata 元数据映射 | Metadata map
     */
    private void parseToml(String tomlContent, Map<String, String> metadata) {
        // 这是一个非常简化的TOML解析，实际应使用库
        // This is a very simplified TOML parsing, should use a library
        String[] lines = tomlContent.split("\n");
        String currentSection = "";
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            
            if (line.startsWith("[") && line.endsWith("]")) {
                currentSection = line.substring(1, line.length() - 1);
                continue;
            }
            
            if (currentSection.equals("mods") && line.contains("=")) {
                String[] parts = line.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].trim();
                
                // 移除值两端的引号
                // Remove quotes from value
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                
                // 将TOML键映射到标准键
                // Map TOML keys to standard keys
                switch (key) {
                    case "displayName" -> metadata.put("name", value);
                    case "version" -> metadata.put("version", value);
                    case "description" -> metadata.put("description", value);
                    case "authors" -> metadata.put("authors", value);
                    case "displayURL" -> metadata.put("url", value);
                }
            }
            
            if (currentSection.equals("dependencies") && line.contains("minecraft")) {
                String[] parts = line.split("=", 2);
                if (parts.length > 1 && parts[1].contains("version")) {
                    String versionStr = parts[1].trim();
                    int start = versionStr.indexOf('"');
                    int end = versionStr.lastIndexOf('"');
                    if (start != -1 && end != -1 && end > start) {
                        metadata.put("mcversion", versionStr.substring(start + 1, end));
                    }
                }
            }
        }
    }
    
    /**
     * 从JSON中提取元数据
     * Extract metadata from JSON
     * 
     * @param json JSON对象 | JSON object
     * @param metadata 元数据映射 | Metadata map
     */
    private void extractJsonMetadata(JsonObject json, Map<String, String> metadata) {
        if (json.has("name")) {
            metadata.put("name", json.get("name").getAsString());
        }
        
        if (json.has("version")) {
            metadata.put("version", json.get("version").getAsString());
        }
        
        if (json.has("description")) {
            metadata.put("description", json.get("description").getAsString());
        }
        
        if (json.has("authors")) {
            try {
                // 处理作者可能是数组或字符串的情况
                // Handle author may be array or string
                Object authors = json.get("authors");
                if (authors.toString().startsWith("[")) {
                    metadata.put("authors", authors.toString().replace("[", "").replace("]", "").replace("\"", ""));
                } else {
                    metadata.put("authors", json.get("authors").getAsString());
                }
            } catch (Exception e) {
                LogUtils.debug("解析作者信息失败 | Failed to parse author information");
            }
        }
        
        if (json.has("contact") && json.getAsJsonObject("contact").has("homepage")) {
            metadata.put("url", json.getAsJsonObject("contact").get("homepage").getAsString());
        }
        
        if (json.has("depends") && json.getAsJsonObject("depends").has("minecraft")) {
            metadata.put("mcversion", json.getAsJsonObject("depends").get("minecraft").getAsString());
        }
    }
    
    /**
     * 提取旧版Forge元数据
     * Extract legacy Forge metadata
     * 
     * @param jsonContent JSON内容 | JSON content
     * @param metadata 元数据映射 | Metadata map
     */
    private void extractLegacyForgeMetadata(String jsonContent, Map<String, String> metadata) {
        try {
            JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
            
            // 旧版mcmod.info可能是数组
            // Legacy mcmod.info may be an array
            if (json.has("modList") && json.getAsJsonArray("modList").size() > 0) {
                JsonObject modInfo = json.getAsJsonArray("modList").get(0).getAsJsonObject();
                
                if (modInfo.has("name")) {
                    metadata.put("name", modInfo.get("name").getAsString());
                }
                
                if (modInfo.has("version")) {
                    metadata.put("version", modInfo.get("version").getAsString());
                }
                
                if (modInfo.has("description")) {
                    metadata.put("description", modInfo.get("description").getAsString());
                }
                
                if (modInfo.has("authorList")) {
                    metadata.put("authors", modInfo.get("authorList").toString()
                            .replace("[", "").replace("]", "").replace("\"", ""));
                }
                
                if (modInfo.has("url")) {
                    metadata.put("url", modInfo.get("url").getAsString());
                }
                
                if (modInfo.has("mcversion")) {
                    metadata.put("mcversion", modInfo.get("mcversion").getAsString());
                }
            }
        } catch (Exception e) {
            LogUtils.error("解析旧版Forge元数据失败 | Failed to parse legacy Forge metadata", e);
        }
    }
    
    /**
     * 识别模组加载器类型
     * Identify mod loader type
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 模组加载器类型 | Mod loader type
     */
    public CompletableFuture<String> identifyModLoaderType(Path modPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> metadata = extractModMetadata(modPath);
                return identifyModLoaderTypeFromMetadata(metadata);
            } catch (Exception e) {
                LogUtils.error("识别模组加载器类型失败 | Failed to identify mod loader type", e);
                return "Unknown";
            }
        });
    }
    
    /**
     * 从元数据识别模组加载器类型
     * Identify mod loader type from metadata
     * 
     * @param metadata 元数据映射 | Metadata map
     * @return 模组加载器类型 | Mod loader type
     */
    private String identifyModLoaderTypeFromMetadata(Map<String, String> metadata) {
        // 检查metadata中的type字段
        // Check type field in metadata
        String type = metadata.getOrDefault("type", "Unknown");
        
        if (type.contains("forge")) {
            return "Forge";
        } else if (type.contains("fabric")) {
            return "Fabric";
        } else if (type.contains("quilt")) {
            return "Quilt";
        } else if (type.contains("liteloader")) {
            return "LiteLoader";
        }
        
        return "Unknown";
    }
    
    /**
     * 识别模组类型
     * Identify mod type
     * 
     * @param modPath 模组文件路径 | Mod file path
     * @return 模组类型 | Mod type
     */
    public CompletableFuture<Mod.ModType> identifyModType(Path modPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, String> metadata = extractModMetadata(modPath);
                return identifyModTypeFromMetadata(metadata);
            } catch (Exception e) {
                LogUtils.error("识别模组类型失败 | Failed to identify mod type", e);
                return Mod.ModType.UNKNOWN;
            }
        });
    }
    
    /**
     * 从元数据识别模组类型
     * Identify mod type from metadata
     * 
     * @param metadata 元数据映射 | Metadata map
     * @return 模组类型 | Mod type
     */
    private Mod.ModType identifyModTypeFromMetadata(Map<String, String> metadata) {
        // 根据元数据特征识别模组类型
        // Identify mod type based on metadata characteristics
        String name = metadata.getOrDefault("name", "").toLowerCase();
        String description = metadata.getOrDefault("description", "").toLowerCase();
        
        // 检查是否为核心模组
        // Check if it's a core mod
        if (name.contains("optifine") || name.contains("core") || description.contains("core mod")) {
            return Mod.ModType.CORE;
        }
        
        // 检查是否为API模组
        // Check if it's an API mod
        if (name.contains("api") || description.contains("api")) {
            return Mod.ModType.API;
        }
        
        // 检查是否为资源模组
        // Check if it's a resource mod
        if (name.contains("resource") || description.contains("resource") || 
            name.contains("texture") || description.contains("texture")) {
            return Mod.ModType.RESOURCE;
        }
        
        // 检查是否为加载器
        // Check if it's a loader
        if (name.contains("forge") || name.contains("fabric") || name.contains("quilt") ||
            description.contains("mod loader") || description.contains("modloader")) {
            return Mod.ModType.LOADER;
        }
        
        // 默认为普通模组
        // Default to normal mod
        return Mod.ModType.NORMAL;
    }
    
    /**
     * 解析兼容的Minecraft版本
     * Parse compatible Minecraft versions
     * 
     * @param versionString 版本字符串 | Version string
     * @return 兼容版本列表 | Compatible version list
     */
    private List<String> parseCompatibleVersions(String versionString) {
        if (versionString.isEmpty()) {
            return List.of();
        }
        
        // 处理版本范围，例如"1.16.x"或"1.16-1.18"
        // Handle version ranges, e.g. "1.16.x" or "1.16-1.18"
        versionString = versionString.replace(" ", "");
        
        if (versionString.contains(",")) {
            // 处理逗号分隔的多个版本
            // Handle comma-separated versions
            return Arrays.asList(versionString.split(","));
        } else if (versionString.contains("-")) {
            // 处理版本范围，例如"1.16-1.18"
            // Handle version range, e.g. "1.16-1.18"
            String[] parts = versionString.split("-");
            if (parts.length == 2) {
                List<String> versions = new ArrayList<>();
                try {
                    String startVersion = parts[0];
                    String endVersion = parts[1];
                    
                    // 简化处理，只考虑主要版本号
                    // Simplified processing, only consider major version numbers
                    int start = Integer.parseInt(startVersion.split("\\.")[1]);
                    int end = Integer.parseInt(endVersion.split("\\.")[1]);
                    
                    for (int i = start; i <= end; i++) {
                        versions.add("1." + i);
                    }
                    
                    return versions;
                } catch (Exception e) {
                    LogUtils.debug("解析版本范围失败 | Failed to parse version range: {}", versionString);
                }
            }
        } else if (versionString.contains("x") || versionString.contains("*")) {
            // 处理通配符，例如"1.16.x"或"1.16.*"
            // Handle wildcards, e.g. "1.16.x" or "1.16.*"
            String baseVersion = versionString.replaceAll("[x\\*]", "");
            if (baseVersion.endsWith(".")) {
                baseVersion = baseVersion.substring(0, baseVersion.length() - 1);
            }
            return List.of(baseVersion);
        }
        
        // 单一版本
        // Single version
        return List.of(versionString);
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
     * 保存模组
     * Save mod
     * 
     * @param mod 模组对象 | Mod object
     * @return 保存结果 | Save result
     */
    public CompletableFuture<Boolean> saveMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path sourcePath = mod.getFilePath();
                Path targetPath = modsDir.resolve(sourcePath.getFileName());
                
                // 如果源文件就在mods目录，则无需复制
                // If source file is already in mods directory, no need to copy
                if (!sourcePath.equals(targetPath)) {
                    Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LogUtils.info("模组已复制到模组目录 | Mod copied to mods directory: {}", targetPath);
                }
                
                // 添加到模组列表
                // Add to mod list
                synchronized (mods) {
                    mods.add(mod);
                }
                
                return true;
            } catch (IOException e) {
                LogUtils.error("保存模组失败 | Failed to save mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 删除模组
     * Delete mod
     * 
     * @param mod 模组对象 | Mod object
     * @return 删除结果 | Delete result
     */
    public CompletableFuture<Boolean> deleteMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Path modPath = mod.getFilePath();
                
                // 如果已启用，先禁用
                // If enabled, disable first
                if (mod.isEnabled()) {
                    disableMod(mod).join();
                }
                
                // 从模组列表中移除
                // Remove from mod list
                synchronized (mods) {
                    mods.remove(mod);
                }
                
                // 如果文件在mods目录，删除文件
                // If file is in mods directory, delete the file
                if (modPath.startsWith(modsDir)) {
                    Files.deleteIfExists(modPath);
                    LogUtils.info("模组已删除 | Mod deleted: {}", modPath);
                }
                
                return true;
            } catch (IOException e) {
                LogUtils.error("删除模组失败 | Failed to delete mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 获取所有模组
     * Get all mods
     * 
     * @return 模组列表 | Mod list
     */
    public CompletableFuture<List<Mod>> getAllMods() {
        return scanAndLoadMods();
    }
    
    /**
     * 获取已启用的模组
     * Get enabled mods
     * 
     * @return 已启用的模组列表 | Enabled mod list
     */
    public CompletableFuture<List<Mod>> getEnabledMods() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                List<Mod> allMods = scanAndLoadMods().join();
                return allMods.stream()
                    .filter(Mod::isEnabled)
                    .collect(Collectors.toList());
            } catch (Exception e) {
                LogUtils.error("获取已启用模组失败 | Failed to get enabled mods", e);
                return List.of();
            }
        });
    }
    
    /**
     * 启用模组
     * Enable mod
     * 
     * @param mod 要启用的模组 | Mod to enable
     * @return 启用结果 | Enable result
     */
    public CompletableFuture<Boolean> enableMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mod.setEnabled(true);
                saveEnabledModsConfig();
                LogUtils.info("模组已启用 | Mod enabled: {}", mod.getName());
                return true;
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
     * @return 禁用结果 | Disable result
     */
    public CompletableFuture<Boolean> disableMod(Mod mod) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                mod.setEnabled(false);
                saveEnabledModsConfig();
                LogUtils.info("模组已禁用 | Mod disabled: {}", mod.getName());
                return true;
            } catch (Exception e) {
                LogUtils.error("禁用模组失败 | Failed to disable mod: {}", mod.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * 扫描并加载所有模组
     * Scan and load all mods
     * 
     * @return 模组列表 | Mod list
     */
    public CompletableFuture<List<Mod>> scanAndLoadMods() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 清空当前列表
                // Clear current list
                synchronized (mods) {
                    mods.clear();
                    
                    // 读取启用的模组配置
                    // Read enabled mods configuration
                    Map<String, Boolean> enabledModsMap = loadEnabledModsConfig();
                    
                    // 扫描模组目录
                    // Scan mods directory
                    List<Path> modPaths = Files.list(modsDir)
                        .filter(path -> path.toString().endsWith(".jar"))
                        .collect(Collectors.toList());
                    
                    // 加载所有模组
                    // Load all mods
                    List<CompletableFuture<Mod>> futures = new ArrayList<>();
                    for (Path modPath : modPaths) {
                        futures.add(loadMod(modPath));
                    }
                    
                    // 等待所有模组加载完成
                    // Wait for all mods to be loaded
                    CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                    
                    // 收集结果并设置启用状态
                    // Collect results and set enabled status
                    for (CompletableFuture<Mod> future : futures) {
                        Mod mod = future.join();
                        if (mod != null) {
                            // 检查是否在启用列表中
                            // Check if it's in the enabled list
                            String modId = mod.getFileHash();
                            if (enabledModsMap.containsKey(modId)) {
                                mod.setEnabled(enabledModsMap.get(modId));
                            }
                            
                            mods.add(mod);
                        }
                    }
                    
                    LogUtils.info("已加载{}个模组 | Loaded {} mods", mods.size(), mods.size());
                }
                
                return new ArrayList<>(mods);
            } catch (IOException e) {
                LogUtils.error("扫描模组失败 | Failed to scan mods", e);
                return List.of();
            }
        });
    }
    
    /**
     * 加载启用的模组配置
     * Load enabled mods configuration
     * 
     * @return 启用的模组映射 | Enabled mods map
     */
    private Map<String, Boolean> loadEnabledModsConfig() {
        Map<String, Boolean> enabledMods = new HashMap<>();
        
        try {
            if (Files.exists(enabledModsConfigPath)) {
                String content = new String(Files.readAllBytes(enabledModsConfigPath), StandardCharsets.UTF_8);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                
                json.entrySet().forEach(entry -> {
                    String modId = entry.getKey();
                    boolean enabled = entry.getValue().getAsBoolean();
                    enabledMods.put(modId, enabled);
                });
            }
        } catch (IOException e) {
            LogUtils.error("加载启用的模组配置失败 | Failed to load enabled mods configuration", e);
        }
        
        return enabledMods;
    }
    
    /**
     * 保存启用的模组配置
     * Save enabled mods configuration
     */
    private void saveEnabledModsConfig() {
        try {
            JsonObject json = new JsonObject();
            
            synchronized (mods) {
                for (Mod mod : mods) {
                    json.addProperty(mod.getFileHash(), mod.isEnabled());
                }
            }
            
            String content = GSON.toJson(json);
            Files.write(enabledModsConfigPath, content.getBytes(StandardCharsets.UTF_8));
            
            LogUtils.debug("已保存启用的模组配置 | Saved enabled mods configuration");
        } catch (IOException e) {
            LogUtils.error("保存启用的模组配置失败 | Failed to save enabled mods configuration", e);
        }
    }
} 