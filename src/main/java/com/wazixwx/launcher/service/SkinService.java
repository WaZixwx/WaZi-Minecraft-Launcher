package com.wazixwx.launcher.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.wazixwx.launcher.core.ConfigurationManager;
import com.wazixwx.launcher.core.LauncherCore;
import com.wazixwx.launcher.model.Skin;
import com.wazixwx.launcher.utils.LogUtils;
import com.wazixwx.launcher.utils.SkinUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 皮肤服务类
 * Skin Service Class
 * 
 * 负责管理离线模式下的自定义皮肤
 * Responsible for managing custom skins in offline mode
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SkinService {
    private static final String SKINS_DIR = "skins";
    private static final String SKINS_INDEX_FILE = "skins.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final TypeToken<List<Skin>> SKIN_LIST_TYPE = new TypeToken<List<Skin>>() {};
    
    private final ConfigurationManager configManager;
    private List<Skin> skins;
    private Skin selectedSkin;
    
    /**
     * 构造函数
     * Constructor
     */
    public SkinService() {
        this.configManager = LauncherCore.getInstance().getConfigManager();
        this.skins = new ArrayList<>();
        initialize();
    }
    
    /**
     * 初始化皮肤服务
     * Initialize skin service
     */
    private void initialize() {
        // 创建皮肤目录
        // Create skins directory
        createSkinsDirectory();
        
        // 加载皮肤列表
        // Load skin list
        loadSkins();
        
        // 如果没有默认皮肤，则添加
        // If no default skins, add them
        if (skins.isEmpty()) {
            addDefaultSkins();
        }
    }
    
    /**
     * 创建皮肤目录
     * Create skins directory
     */
    private void createSkinsDirectory() {
        File skinsDir = new File(SKINS_DIR);
        if (!skinsDir.exists()) {
            skinsDir.mkdirs();
        }
    }
    
    /**
     * 加载皮肤列表
     * Load skin list
     */
    private void loadSkins() {
        try {
            File indexFile = new File(SKINS_DIR, SKINS_INDEX_FILE);
            if (indexFile.exists()) {
                String json = FileUtils.readFileToString(indexFile, StandardCharsets.UTF_8);
                skins = GSON.fromJson(json, SKIN_LIST_TYPE.getType());
                
                // 验证皮肤文件是否存在
                // Verify if skin files exist
                skins = skins.stream()
                    .filter(skin -> Files.exists(skin.getFilePath()))
                    .collect(Collectors.toList());
                
                // 默认选择第一个皮肤
                // Select first skin by default
                if (!skins.isEmpty()) {
                    selectedSkin = skins.get(0);
                }
            }
        } catch (IOException e) {
            LogUtils.error("加载皮肤列表失败 | Failed to load skin list", e);
            skins = new ArrayList<>();
        }
    }
    
    /**
     * 添加默认皮肤
     * Add default skins
     */
    private void addDefaultSkins() {
        try {
            // 使用SkinUtils生成默认皮肤
            // Use SkinUtils to generate default skins
            SkinUtils.generateDefaultSkins();
            
            Path stevePath = Paths.get(SKINS_DIR, "steve.png");
            Path alexPath = Paths.get(SKINS_DIR, "alex.png");
            
            // 创建Steve皮肤对象
            // Create Steve skin object
            Skin steve = new Skin("Steve", "default", stevePath, calculateHash(stevePath));
            
            // 创建Alex皮肤对象
            // Create Alex skin object
            Skin alex = new Skin("Alex", "slim", alexPath, calculateHash(alexPath));
            
            // 添加到皮肤列表
            // Add to skin list
            skins.add(steve);
            skins.add(alex);
            
            // 默认选择Steve皮肤
            // Select Steve skin by default
            selectedSkin = steve;
            
            // 保存皮肤列表
            // Save skin list
            saveSkins();
        } catch (IOException e) {
            LogUtils.error("添加默认皮肤失败 | Failed to add default skins", e);
        }
    }
    
    /**
     * 保存皮肤列表
     * Save skin list
     */
    private void saveSkins() {
        try {
            String json = GSON.toJson(skins);
            FileUtils.writeStringToFile(new File(SKINS_DIR, SKINS_INDEX_FILE), 
                    json, StandardCharsets.UTF_8);
        } catch (IOException e) {
            LogUtils.error("保存皮肤列表失败 | Failed to save skin list", e);
        }
    }
    
    /**
     * 添加皮肤
     * Add skin
     * 
     * @param name 皮肤名称 | Skin name
     * @param type 皮肤类型 | Skin type (default/slim)
     * @param skinFile 皮肤文件 | Skin file
     * @return 添加的皮肤 | Added skin
     * @throws IOException 如果文件操作失败 | If file operation fails
     */
    public Skin addSkin(String name, String type, File skinFile) throws IOException {
        // 确保皮肤目录存在
        // Ensure skins directory exists
        createSkinsDirectory();
        
        // 复制皮肤文件到皮肤目录
        // Copy skin file to skins directory
        String fileName = UUID.randomUUID().toString() + ".png";
        Path targetPath = Paths.get(SKINS_DIR, fileName);
        Files.copy(skinFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        
        // 计算皮肤文件哈希
        // Calculate skin file hash
        String hash = calculateHash(targetPath);
        
        // 创建皮肤对象
        // Create skin object
        Skin skin = new Skin(name, type, targetPath, hash);
        
        // 添加到皮肤列表
        // Add to skin list
        skins.add(skin);
        
        // 保存皮肤列表
        // Save skin list
        saveSkins();
        
        return skin;
    }
    
    /**
     * 移除皮肤
     * Remove skin
     * 
     * @param skin 要移除的皮肤 | Skin to remove
     * @return 是否成功移除 | Whether removal was successful
     */
    public boolean removeSkin(Skin skin) {
        // 如果是选中的皮肤，则取消选中
        // If it's the selected skin, deselect it
        if (skin.equals(selectedSkin)) {
            selectedSkin = null;
        }
        
        // 从列表中移除
        // Remove from list
        boolean removed = skins.remove(skin);
        
        if (removed) {
            // 删除皮肤文件
            // Delete skin file
            try {
                Files.deleteIfExists(skin.getFilePath());
            } catch (IOException e) {
                LogUtils.error("删除皮肤文件失败 | Failed to delete skin file", e);
            }
            
            // 保存皮肤列表
            // Save skin list
            saveSkins();
        }
        
        return removed;
    }
    
    /**
     * 选择皮肤
     * Select skin
     * 
     * @param skin 要选择的皮肤 | Skin to select
     */
    public void selectSkin(Skin skin) {
        this.selectedSkin = skin;
    }
    
    /**
     * 获取所有皮肤
     * Get all skins
     * 
     * @return 皮肤列表 | Skin list
     */
    public List<Skin> getAllSkins() {
        return new ArrayList<>(skins);
    }
    
    /**
     * 获取选中的皮肤
     * Get selected skin
     * 
     * @return 选中的皮肤 | Selected skin
     */
    public Skin getSelectedSkin() {
        return selectedSkin;
    }
    
    /**
     * 根据类型获取皮肤
     * Get skins by type
     * 
     * @param type 皮肤类型 | Skin type (default/slim)
     * @return 皮肤列表 | Skin list
     */
    public List<Skin> getSkinsByType(String type) {
        return skins.stream()
                .filter(skin -> skin.getType().equals(type))
                .collect(Collectors.toList());
    }
    
    /**
     * 验证皮肤是否有效
     * Validate if skin is valid
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 是否有效 | Whether valid
     */
    public boolean validateSkin(File skinFile) {
        return SkinUtils.validateSkin(skinFile);
    }
    
    /**
     * 获取皮肤类型
     * Get skin type
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 皮肤类型 | Skin type
     */
    public String getSkinType(File skinFile) {
        return SkinUtils.determineSkinType(skinFile);
    }
    
    /**
     * 计算文件哈希值
     * Calculate file hash
     * 
     * @param path 文件路径 | File path
     * @return 哈希值 | Hash value
     */
    private String calculateHash(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(Files.readAllBytes(path));
            
            // 将哈希值转换为十六进制字符串
            // Convert hash to hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
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
} 