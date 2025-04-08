package com.wazixwx.launcher.utils;

import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.effect.Effect;
import javafx.scene.layout.Region;

/**
 * 毛玻璃效果工具类
 */
public class BlurUtils {
    
    /**
     * 为区域添加毛玻璃效果
     * @param region 要添加效果的区域
     * @param radius 模糊半径
     * @param iterations 迭代次数
     */
    public static void addBoxBlur(Region region, double radius, int iterations) {
        BoxBlur blur = new BoxBlur(radius, radius, iterations);
        region.setEffect(blur);
    }
    
    /**
     * 为区域添加高斯模糊效果
     * @param region 要添加效果的区域
     * @param radius 模糊半径
     */
    public static void addGaussianBlur(Region region, double radius) {
        GaussianBlur blur = new GaussianBlur(radius);
        region.setEffect(blur);
    }
    
    /**
     * 移除区域的效果
     * @param region 要移除效果的区域
     */
    public static void removeEffect(Region region) {
        region.setEffect(null);
    }
    
    /**
     * 获取当前的模糊效果
     * @param region 要获取效果的区域
     * @return 当前的效果，如果没有则返回null
     */
    public static Effect getCurrentEffect(Region region) {
        return region.getEffect();
    }
    
    /**
     * 检查区域是否有模糊效果
     * @param region 要检查的区域
     * @return 是否有模糊效果
     */
    public static boolean hasBlurEffect(Region region) {
        Effect effect = region.getEffect();
        return effect instanceof BoxBlur || effect instanceof GaussianBlur;
    }
} 