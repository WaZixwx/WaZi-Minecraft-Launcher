package com.wazixwx.launcher.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * 动画效果工具类
 * Animation Effects Utility Class
 * 
 * 提供各种UI动画效果的工具方法
 * Provides utility methods for various UI animation effects
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class AnimationUtils {
    
    /**
     * 创建淡入动画
     * Create fade in animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @return 淡入动画 Fade in animation
     */
    public static FadeTransition createFadeIn(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        return fadeTransition;
    }
    
    /**
     * 创建淡出动画
     * Create fade out animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @return 淡出动画 Fade out animation
     */
    public static FadeTransition createFadeOut(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        return fadeTransition;
    }
    
    /**
     * 创建缩放动画
     * Create scale animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @param fromScale 起始缩放比例 Start scale
     * @param toScale 目标缩放比例 Target scale
     * @return 缩放动画 Scale animation
     */
    public static ScaleTransition createScale(Node node, Duration duration, double fromScale, double toScale) {
        ScaleTransition scaleTransition = new ScaleTransition(duration, node);
        scaleTransition.setFromX(fromScale);
        scaleTransition.setFromY(fromScale);
        scaleTransition.setToX(toScale);
        scaleTransition.setToY(toScale);
        return scaleTransition;
    }
    
    /**
     * 创建旋转动画
     * Create rotate animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @param fromAngle 起始角度 Start angle
     * @param toAngle 目标角度 Target angle
     * @return 旋转动画 Rotate animation
     */
    public static RotateTransition createRotate(Node node, Duration duration, double fromAngle, double toAngle) {
        RotateTransition rotateTransition = new RotateTransition(duration, node);
        rotateTransition.setFromAngle(fromAngle);
        rotateTransition.setToAngle(toAngle);
        return rotateTransition;
    }
    
    /**
     * 创建弹跳动画
     * Create bounce animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @return 弹跳动画 Bounce animation
     */
    public static Timeline createBounce(Node node, Duration duration) {
        Timeline timeline = new Timeline();
        KeyFrame kf1 = new KeyFrame(Duration.ZERO, new KeyValue(node.scaleXProperty(), 1));
        KeyFrame kf2 = new KeyFrame(duration.multiply(0.5), new KeyValue(node.scaleXProperty(), 1.2));
        KeyFrame kf3 = new KeyFrame(duration, new KeyValue(node.scaleXProperty(), 1));
        timeline.getKeyFrames().addAll(kf1, kf2, kf3);
        return timeline;
    }
    
    /**
     * 创建闪烁动画
     * Create blink animation
     * 
     * @param node 要添加动画的节点 Node to animate
     * @param duration 动画持续时间 Animation duration
     * @param count 闪烁次数 Blink count
     * @return 闪烁动画 Blink animation
     */
    public static Timeline createBlink(Node node, Duration duration, int count) {
        Timeline timeline = new Timeline();
        for (int i = 0; i < count * 2; i++) {
            double opacity = i % 2 == 0 ? 1.0 : 0.0;
            KeyFrame kf = new KeyFrame(duration.multiply(i / (double)(count * 2)), 
                                     new KeyValue(node.opacityProperty(), opacity));
            timeline.getKeyFrames().add(kf);
        }
        return timeline;
    }
    
    /**
     * 执行淡入动画
     * Perform fade in animation
     * 
     * @param node 要淡入的节点 Node to fade in
     * @param durationMs 动画持续时间（毫秒） Animation duration in milliseconds
     */
    public static void fadeIn(Node node, int durationMs) {
        node.setOpacity(0);
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(durationMs), node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        fadeTransition.play();
    }
    
    /**
     * 执行抖动动画
     * Perform shake animation
     * 
     * @param node 要抖动的节点 Node to shake
     * @param intensity 抖动强度 Shake intensity
     * @param count 抖动次数 Shake count
     */
    public static void shakeNode(Node node, double intensity, int count) {
        Timeline timeline = new Timeline();
        node.setTranslateX(0);
        
        for (int i = 0; i < count; i++) {
            double direction = (i % 2 == 0) ? 1 : -1;
            double time = (double) i / count;
            
            KeyFrame right = new KeyFrame(
                    Duration.millis(100 + (time * 400)), 
                    new KeyValue(node.translateXProperty(), intensity * direction, Interpolator.EASE_BOTH));
            
            KeyFrame left = new KeyFrame(
                    Duration.millis(200 + (time * 400)), 
                    new KeyValue(node.translateXProperty(), -intensity * direction, Interpolator.EASE_BOTH));
            
            timeline.getKeyFrames().addAll(right, left);
        }
        
        // 添加结束帧，回到原始位置
        // Add ending keyframe to return to original position
        KeyFrame endFrame = new KeyFrame(
                Duration.millis(300 + (count * 400)), 
                new KeyValue(node.translateXProperty(), 0, Interpolator.EASE_OUT));
        
        timeline.getKeyFrames().add(endFrame);
        timeline.play();
    }
    
    /**
     * 停止节点上的所有动画
     * Stop all animations on a node
     * 
     * @param node 要停止动画的节点 Node to stop animations
     */
    public static void stopAllAnimations(Node node) {
        node.getTransforms().clear();
        if (node.getEffect() != null) {
            node.setEffect(null);
        }
    }
} 