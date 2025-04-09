package com.wazixwx.launcher.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.imageio.ImageIO;

/**
 * 皮肤工具类
 * Skin Utility Class
 * 
 * 提供皮肤相关的工具方法
 * Provides skin-related utility methods
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SkinUtils {
    
    /**
     * 验证皮肤文件是否有效
     * Validate if a skin file is valid
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 是否有效 | Whether valid
     */
    public static boolean validateSkin(File skinFile) {
        try {
            // 验证文件格式
            // Validate file format
            BufferedImage image = ImageIO.read(skinFile);
            if (image == null) {
                LogUtils.error("无效的图片格式 | Invalid image format");
                return false;
            }
            
            // 验证图片尺寸
            // Validate image dimensions
            int width = image.getWidth();
            int height = image.getHeight();
            
            // Minecraft皮肤必须是64x64或64x32像素
            // Minecraft skins must be 64x64 or 64x32 pixels
            return (width == 64 && (height == 64 || height == 32));
        } catch (IOException e) {
            LogUtils.error("读取皮肤文件失败 | Failed to read skin file", e);
            return false;
        }
    }
    
    /**
     * 生成默认Steve皮肤
     * Generate default Steve skin
     * 
     * @param outputPath 输出路径 | Output path
     * @throws IOException 如果生成失败 | If generation fails
     */
    public static void generateDefaultSteveSkin(Path outputPath) throws IOException {
        // 创建64x64像素的图片
        // Create 64x64 pixel image
        BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = skin.createGraphics();
        
        // 填充基本颜色
        // Fill basic colors
        
        // 头部 | Head - 棕色 | Brown
        g.setColor(new Color(141, 85, 36));
        g.fillRect(8, 8, 8, 8);
        
        // 身体 | Body - 红色 | Red
        g.setColor(new Color(204, 0, 0));
        g.fillRect(20, 20, 8, 12);
        
        // 左臂 | Left arm - 蓝色 | Blue
        g.setColor(new Color(0, 0, 204));
        g.fillRect(36, 20, 4, 12);
        
        // 右臂 | Right arm - 蓝色 | Blue
        g.setColor(new Color(0, 0, 204));
        g.fillRect(44, 20, 4, 12);
        
        // 左腿 | Left leg - 深蓝色 | Dark blue
        g.setColor(new Color(0, 0, 102));
        g.fillRect(20, 36, 4, 12);
        
        // 右腿 | Right leg - 深蓝色 | Dark blue
        g.setColor(new Color(0, 0, 102));
        g.fillRect(28, 36, 4, 12);
        
        g.dispose();
        
        // 保存皮肤
        // Save skin
        Files.createDirectories(outputPath.getParent());
        ImageIO.write(skin, "PNG", outputPath.toFile());
    }
    
    /**
     * 生成默认Alex皮肤
     * Generate default Alex skin
     * 
     * @param outputPath 输出路径 | Output path
     * @throws IOException 如果生成失败 | If generation fails
     */
    public static void generateDefaultAlexSkin(Path outputPath) throws IOException {
        // 创建64x64像素的图片
        // Create 64x64 pixel image
        BufferedImage skin = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = skin.createGraphics();
        
        // 填充基本颜色
        // Fill basic colors
        
        // 头部 | Head - 橙色 | Orange
        g.setColor(new Color(204, 102, 0));
        g.fillRect(8, 8, 8, 8);
        
        // 身体 | Body - 绿色 | Green
        g.setColor(new Color(0, 153, 0));
        g.fillRect(20, 20, 8, 12);
        
        // 左臂 | Left arm - 淡绿色 | Light green (slim arms for Alex)
        g.setColor(new Color(102, 204, 102));
        g.fillRect(36, 20, 3, 12);
        
        // 右臂 | Right arm - 淡绿色 | Light green (slim arms for Alex)
        g.setColor(new Color(102, 204, 102));
        g.fillRect(44, 20, 3, 12);
        
        // 左腿 | Left leg - 深绿色 | Dark green
        g.setColor(new Color(0, 102, 0));
        g.fillRect(20, 36, 4, 12);
        
        // 右腿 | Right leg - 深绿色 | Dark green
        g.setColor(new Color(0, 102, 0));
        g.fillRect(28, 36, 4, 12);
        
        g.dispose();
        
        // 保存皮肤
        // Save skin
        Files.createDirectories(outputPath.getParent());
        ImageIO.write(skin, "PNG", outputPath.toFile());
    }
    
    /**
     * 生成并保存默认皮肤
     * Generate and save default skins
     * 
     * @throws IOException 如果生成失败 | If generation fails
     */
    public static void generateDefaultSkins() throws IOException {
        // 创建皮肤目录
        // Create skins directory
        Path skinsDir = Paths.get("skins");
        Files.createDirectories(skinsDir);
        
        // 生成默认Steve皮肤
        // Generate default Steve skin
        generateDefaultSteveSkin(skinsDir.resolve("steve.png"));
        
        // 生成默认Alex皮肤
        // Generate default Alex skin
        generateDefaultAlexSkin(skinsDir.resolve("alex.png"));
        
        LogUtils.info("默认皮肤生成完成 | Default skins generated");
    }
    
    /**
     * 获取皮肤类型（根据皮肤文件判断）
     * Get skin type (determined by skin file)
     * 
     * @param skinFile 皮肤文件 | Skin file
     * @return 皮肤类型 (default/slim) | Skin type (default/slim)
     */
    public static String determineSkinType(File skinFile) {
        try {
            BufferedImage image = ImageIO.read(skinFile);
            if (image == null) {
                return "default";
            }
            
            // 检查是否是Alex模型（检查左臂区域是否透明）
            // Check if it's Alex model (check if left arm area is transparent)
            int pixel = image.getRGB(54, 20) & 0xFF000000;
            return (pixel == 0) ? "slim" : "default";
        } catch (IOException e) {
            LogUtils.error("读取皮肤文件失败 | Failed to read skin file", e);
            return "default";
        }
    }
} 