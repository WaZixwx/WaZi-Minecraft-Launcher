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
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

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
     * @param targetPath 皮肤文件路径 | Skin file path
     * @return 添加的皮肤 | Added skin
     * @throws IOException 如果文件操作失败 | If file operation fails
     */
    public Skin addSkin(String name, String type, Path targetPath) throws IOException {
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
     * 删除皮肤
     * Delete skin
     * 
     * @param skin 要删除的皮肤 | Skin to delete
     * @return 是否成功删除 | Whether deletion was successful
     */
    public boolean deleteSkin(Skin skin) {
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
     * 设置选中的皮肤
     * Set selected skin
     * 
     * @param skin 要选中的皮肤 | Skin to select
     */
    public void setSelectedSkin(Skin skin) {
        this.selectedSkin = skin;
        saveSkins();
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
     * 按类型获取皮肤
     * Get skins by type
     * 
     * @param type 皮肤类型 | Skin type
     * @return 匹配类型的皮肤列表 | List of skins matching the type
     */
    public List<Skin> getSkinsByType(String type) {
        return skins.stream()
                .filter(skin -> skin.getType().equals(type))
                .collect(Collectors.toList());
    }
    
    /**
     * 验证皮肤文件
     * Validate skin file
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 皮肤文件是否有效 | Whether skin file is valid
     */
    public boolean validateSkin(File skinFile) {
        try {
            if (!skinFile.exists() || !skinFile.isFile()) {
                LogUtils.error("皮肤文件不存在或不是文件 | Skin file doesn't exist or is not a file");
                return false;
            }
            
            BufferedImage image = ImageIO.read(skinFile);
            if (image == null) {
                LogUtils.error("无法读取皮肤文件 | Cannot read skin file");
                return false;
            }
            
            int width = image.getWidth();
            int height = image.getHeight();
            
            // Minecraft皮肤必须是64x64像素或64x32像素
            // Minecraft skins must be 64x64 pixels or 64x32 pixels
            boolean validDimensions = (width == 64 && (height == 64 || height == 32));
            
            if (!validDimensions) {
                LogUtils.error("皮肤尺寸无效: " + width + "x" + height + 
                        " | Invalid skin dimensions: " + width + "x" + height);
            }
            
            return validDimensions;
        } catch (IOException e) {
            LogUtils.error("读取皮肤文件失败 | Failed to read skin file", e);
            return false;
        }
    }
    
    /**
     * 确定皮肤类型
     * Determine skin type
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 皮肤类型 (default/slim) | Skin type (default/slim)
     */
    public String determineSkinType(File skinFile) {
        try {
            BufferedImage image = ImageIO.read(skinFile);
            if (image == null) {
                return "default"; // 默认为Steve模型 | Default to Steve model
            }
            
            // 检查Alex模型特征（细手臂）
            // Check for Alex model features (slim arms)
            // 在64x64的皮肤中，检查手臂区域的透明度
            // In a 64x64 skin, check transparency in the arm region
            
            // 如果是64x64的皮肤，检查左手臂区域
            // If it's a 64x64 skin, check the left arm region
            if (image.getHeight() == 64) {
                boolean hasTransparency = false;
                
                // 检查左手臂区域的透明度
                // Check transparency in the left arm region
                for (int x = 32; x < 48; x++) {
                    for (int y = 52; y < 64; y++) {
                        int pixel = image.getRGB(x, y);
                        int alpha = (pixel >> 24) & 0xff;
                        if (alpha < 128) {
                            hasTransparency = true;
                            break;
                        }
                    }
                    if (hasTransparency) break;
                }
                
                // 如果有透明区域，可能是Alex模型
                // If there's a transparency, it might be an Alex model
                if (hasTransparency) {
                    return "slim";
                }
            }
            
            // 默认为Steve模型
            // Default to Steve model
            return "default";
        } catch (IOException e) {
            LogUtils.error("确定皮肤类型失败 | Failed to determine skin type", e);
            return "default"; // 默认为Steve模型 | Default to Steve model
        }
    }
    
    /**
     * 计算文件哈希
     * Calculate file hash
     * 
     * @param path 文件路径 | File path
     * @return 文件哈希 | File hash
     */
    private String calculateHash(Path path) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] digest = md.digest(Files.readAllBytes(path));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            LogUtils.error("计算文件哈希失败 | Failed to calculate file hash", e);
            return UUID.randomUUID().toString(); // 失败时使用随机UUID | Use random UUID on failure
        }
    }
} 