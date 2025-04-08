package com.wazixwx.launcher.utils;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

/**
 * 动画效果工具类
 */
public class AnimationUtils {
    
    /**
     * 创建淡入动画
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @return 淡入动画
     */
    public static FadeTransition createFadeIn(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(0.0);
        fadeTransition.setToValue(1.0);
        return fadeTransition;
    }
    
    /**
     * 创建淡出动画
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @return 淡出动画
     */
    public static FadeTransition createFadeOut(Node node, Duration duration) {
        FadeTransition fadeTransition = new FadeTransition(duration, node);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        return fadeTransition;
    }
    
    /**
     * 创建缩放动画
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @param fromScale 起始缩放比例
     * @param toScale 目标缩放比例
     * @return 缩放动画
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
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @param fromAngle 起始角度
     * @param toAngle 目标角度
     * @return 旋转动画
     */
    public static RotateTransition createRotate(Node node, Duration duration, double fromAngle, double toAngle) {
        RotateTransition rotateTransition = new RotateTransition(duration, node);
        rotateTransition.setFromAngle(fromAngle);
        rotateTransition.setToAngle(toAngle);
        return rotateTransition;
    }
    
    /**
     * 创建弹跳动画
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @return 弹跳动画
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
     * @param node 要添加动画的节点
     * @param duration 动画持续时间
     * @param count 闪烁次数
     * @return 闪烁动画
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
     * 停止节点上的所有动画
     * @param node 要停止动画的节点
     */
    public static void stopAllAnimations(Node node) {
        node.getTransforms().clear();
        if (node.getEffect() != null) {
            node.setEffect(null);
        }
    }
} 