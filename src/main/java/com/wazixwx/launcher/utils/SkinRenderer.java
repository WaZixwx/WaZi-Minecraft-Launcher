package com.wazixwx.launcher.utils;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;

/**
 * 皮肤渲染器工具类
 * Skin Renderer Utility Class
 * 
 * 负责在Canvas上渲染Minecraft皮肤的3D预览
 * Responsible for rendering 3D preview of Minecraft skins on Canvas
 * 
 * @author WaZixwx
 * @since 1.0.0
 */
public class SkinRenderer {
    private final Canvas canvas;
    private final GraphicsContext gc;
    private Image skinImage;
    private boolean isSlimModel = false;
    
    // 皮肤纹理贴图中每个面的坐标
    // Coordinates of each face in the skin texture map
    private static final int HEAD_TOP = 0;
    private static final int HEAD_BOTTOM = 1;
    private static final int HEAD_RIGHT = 2;
    private static final int HEAD_FRONT = 3;
    private static final int HEAD_LEFT = 4;
    private static final int HEAD_BACK = 5;
    
    private static final int TORSO_TOP = 6;
    private static final int TORSO_BOTTOM = 7;
    private static final int TORSO_RIGHT = 8;
    private static final int TORSO_FRONT = 9;
    private static final int TORSO_LEFT = 10;
    private static final int TORSO_BACK = 11;
    
    private static final int RIGHT_ARM_TOP = 12;
    private static final int RIGHT_ARM_BOTTOM = 13;
    private static final int RIGHT_ARM_RIGHT = 14;
    private static final int RIGHT_ARM_FRONT = 15;
    private static final int RIGHT_ARM_LEFT = 16;
    private static final int RIGHT_ARM_BACK = 17;
    
    private static final int LEFT_ARM_TOP = 18;
    private static final int LEFT_ARM_BOTTOM = 19;
    private static final int LEFT_ARM_RIGHT = 20;
    private static final int LEFT_ARM_FRONT = 21;
    private static final int LEFT_ARM_LEFT = 22;
    private static final int LEFT_ARM_BACK = 23;
    
    private static final int RIGHT_LEG_TOP = 24;
    private static final int RIGHT_LEG_BOTTOM = 25;
    private static final int RIGHT_LEG_RIGHT = 26;
    private static final int RIGHT_LEG_FRONT = 27;
    private static final int RIGHT_LEG_LEFT = 28;
    private static final int RIGHT_LEG_BACK = 29;
    
    private static final int LEFT_LEG_TOP = 30;
    private static final int LEFT_LEG_BOTTOM = 31;
    private static final int LEFT_LEG_RIGHT = 32;
    private static final int LEFT_LEG_FRONT = 33;
    private static final int LEFT_LEG_LEFT = 34;
    private static final int LEFT_LEG_BACK = 35;
    
    /**
     * 构造函数
     * Constructor
     * 
     * @param canvas 渲染用的画布 | Canvas for rendering
     */
    public SkinRenderer(Canvas canvas) {
        this.canvas = canvas;
        this.gc = canvas.getGraphicsContext2D();
    }
    
    /**
     * 设置皮肤图像
     * Set skin image
     * 
     * @param skinImage 皮肤图像 | Skin image
     * @param isSlimModel 是否为Alex模型（细手臂）| Whether it's Alex model (slim arms)
     */
    public void setSkinImage(Image skinImage, boolean isSlimModel) {
        this.skinImage = skinImage;
        this.isSlimModel = isSlimModel;
    }
    
    /**
     * 清除画布
     * Clear canvas
     */
    public void clear() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }
    
    /**
     * 渲染皮肤
     * Render skin
     * 
     * @param rotationY Y轴旋转角度 | Y-axis rotation angle
     */
    public void render(double rotationY) {
        if (skinImage == null) {
            return;
        }
        
        clear();
        
        // 保存当前图形上下文状态
        // Save current graphics context state
        gc.save();
        
        // 设置平滑模式
        // Set smooth mode
        gc.setImageSmoothing(false);
        
        try {
            // 居中放置模型
            // Center the model
            double centerX = canvas.getWidth() / 2;
            double centerY = canvas.getHeight() / 2;
            gc.translate(centerX, centerY);
            
            // 缩放因子（使模型适合画布）
            // Scale factor (to make the model fit the canvas)
            double scale = canvas.getHeight() / 250.0;
            gc.scale(scale, scale);
            
            // 应用Y轴旋转
            // Apply Y-axis rotation
            double radians = Math.toRadians(rotationY);
            
            // 绘制模型各部分
            // Draw model parts
            renderHead(radians);
            renderTorso(radians);
            renderArms(radians);
            renderLegs(radians);
            
        } finally {
            // 恢复图形上下文状态
            // Restore graphics context state
            gc.restore();
        }
    }
    
    /**
     * 渲染头部
     * Render head
     * 
     * @param rotationY Y轴旋转弧度 | Y-axis rotation in radians
     */
    private void renderHead(double rotationY) {
        // 头部尺寸和位置
        // Head size and position
        double headSize = 40;
        double headY = -95;
        
        // 渲染头部立方体
        // Render head cube
        renderCube(0, headY, 0, headSize, headSize, headSize, rotationY, new int[] {
            HEAD_TOP, HEAD_BOTTOM, HEAD_RIGHT, HEAD_FRONT, HEAD_LEFT, HEAD_BACK
        });
    }
    
    /**
     * 渲染躯干
     * Render torso
     * 
     * @param rotationY Y轴旋转弧度 | Y-axis rotation in radians
     */
    private void renderTorso(double rotationY) {
        // 躯干尺寸和位置
        // Torso size and position
        double torsoWidth = 40;
        double torsoHeight = 60;
        double torsoDepth = 20;
        double torsoY = -45;
        
        // 渲染躯干立方体
        // Render torso cube
        renderCube(0, torsoY, 0, torsoWidth, torsoHeight, torsoDepth, rotationY, new int[] {
            TORSO_TOP, TORSO_BOTTOM, TORSO_RIGHT, TORSO_FRONT, TORSO_LEFT, TORSO_BACK
        });
    }
    
    /**
     * 渲染手臂
     * Render arms
     * 
     * @param rotationY Y轴旋转弧度 | Y-axis rotation in radians
     */
    private void renderArms(double rotationY) {
        // 手臂尺寸和位置
        // Arm size and position
        double armWidth = isSlimModel ? 12 : 16;
        double armHeight = 60;
        double armDepth = 16;
        double armY = -45;
        double rightArmX = -28;
        double leftArmX = 28;
        
        // 渲染右臂立方体
        // Render right arm cube
        renderCube(rightArmX, armY, 0, armWidth, armHeight, armDepth, rotationY, new int[] {
            RIGHT_ARM_TOP, RIGHT_ARM_BOTTOM, RIGHT_ARM_RIGHT, RIGHT_ARM_FRONT, RIGHT_ARM_LEFT, RIGHT_ARM_BACK
        });
        
        // 渲染左臂立方体
        // Render left arm cube
        renderCube(leftArmX, armY, 0, armWidth, armHeight, armDepth, rotationY, new int[] {
            LEFT_ARM_TOP, LEFT_ARM_BOTTOM, LEFT_ARM_RIGHT, LEFT_ARM_FRONT, LEFT_ARM_LEFT, LEFT_ARM_BACK
        });
    }
    
    /**
     * 渲染腿部
     * Render legs
     * 
     * @param rotationY Y轴旋转弧度 | Y-axis rotation in radians
     */
    private void renderLegs(double rotationY) {
        // 腿部尺寸和位置
        // Leg size and position
        double legWidth = 16;
        double legHeight = 60;
        double legDepth = 16;
        double legY = 15;
        double rightLegX = -10;
        double leftLegX = 10;
        
        // 渲染右腿立方体
        // Render right leg cube
        renderCube(rightLegX, legY, 0, legWidth, legHeight, legDepth, rotationY, new int[] {
            RIGHT_LEG_TOP, RIGHT_LEG_BOTTOM, RIGHT_LEG_RIGHT, RIGHT_LEG_FRONT, RIGHT_LEG_LEFT, RIGHT_LEG_BACK
        });
        
        // 渲染左腿立方体
        // Render left leg cube
        renderCube(leftLegX, legY, 0, legWidth, legHeight, legDepth, rotationY, new int[] {
            LEFT_LEG_TOP, LEFT_LEG_BOTTOM, LEFT_LEG_RIGHT, LEFT_LEG_FRONT, LEFT_LEG_LEFT, LEFT_LEG_BACK
        });
    }
    
    /**
     * 渲染立方体
     * Render cube
     * 
     * @param x X坐标 | X coordinate
     * @param y Y坐标 | Y coordinate
     * @param z Z坐标 | Z coordinate
     * @param width 宽度 | Width
     * @param height 高度 | Height
     * @param depth 深度 | Depth
     * @param rotationY Y轴旋转弧度 | Y-axis rotation in radians
     * @param faceIndices 面索引 | Face indices
     */
    private void renderCube(double x, double y, double z, double width, double height, double depth,
                           double rotationY, int[] faceIndices) {
        // 半宽、半高、半深
        // Half width, half height, half depth
        double hw = width / 2;
        double hh = height / 2;
        double hd = depth / 2;
        
        // 立方体顶点（本地坐标）
        // Cube vertices (local coordinates)
        double[][] vertices = {
            {-hw, -hh, -hd},  // 0: 左上前
            { hw, -hh, -hd},  // 1: 右上前
            { hw,  hh, -hd},  // 2: 右下前
            {-hw,  hh, -hd},  // 3: 左下前
            {-hw, -hh,  hd},  // 4: 左上后
            { hw, -hh,  hd},  // 5: 右上后
            { hw,  hh,  hd},  // 6: 右下后
            {-hw,  hh,  hd}   // 7: 左下后
        };
        
        // 立方体面（顶点索引）
        // Cube faces (vertex indices)
        int[][] faces = {
            {0, 1, 5, 4},  // 顶面 | Top
            {3, 2, 6, 7},  // 底面 | Bottom
            {1, 2, 6, 5},  // 右面 | Right
            {0, 1, 2, 3},  // 前面 | Front
            {0, 3, 7, 4},  // 左面 | Left
            {4, 5, 6, 7}   // 后面 | Back
        };
        
        // 计算变换后的顶点
        // Calculate transformed vertices
        double[][] transformedVertices = new double[vertices.length][3];
        
        for (int i = 0; i < vertices.length; i++) {
            // 应用Y轴旋转
            // Apply Y-axis rotation
            double vx = vertices[i][0];
            double vy = vertices[i][1];
            double vz = vertices[i][2];
            
            double rotatedX = vx * Math.cos(rotationY) - vz * Math.sin(rotationY);
            double rotatedZ = vx * Math.sin(rotationY) + vz * Math.cos(rotationY);
            
            // 平移到指定位置
            // Translate to specified position
            transformedVertices[i][0] = rotatedX + x;
            transformedVertices[i][1] = vy + y;
            transformedVertices[i][2] = rotatedZ + z;
        }
        
        // 计算每个面的深度（用于深度排序）
        // Calculate depth of each face (for depth sorting)
        double[] faceDepths = new double[faces.length];
        for (int i = 0; i < faces.length; i++) {
            double avgZ = 0;
            for (int j = 0; j < faces[i].length; j++) {
                avgZ += transformedVertices[faces[i][j]][2];
            }
            faceDepths[i] = avgZ / faces[i].length;
        }
        
        // 按深度排序面（从远到近）
        // Sort faces by depth (from far to near)
        Integer[] faceOrder = new Integer[faces.length];
        for (int i = 0; i < faceOrder.length; i++) {
            faceOrder[i] = i;
        }
        
        java.util.Arrays.sort(faceOrder, (a, b) -> Double.compare(faceDepths[b], faceDepths[a]));
        
        // 渲染每个面
        // Render each face
        for (int i : faceOrder) {
            int[] face = faces[i];
            int textureIndex = faceIndices[i];
            
            // 获取面的纹理坐标
            // Get texture coordinates for the face
            double[] texCoords = getTextureCoordinates(textureIndex);
            
            // 绘制面
            // Draw face
            drawFace(transformedVertices, face, texCoords);
        }
    }
    
    /**
     * 获取纹理坐标
     * Get texture coordinates
     * 
     * @param faceIndex 面索引 | Face index
     * @return 纹理坐标 | Texture coordinates
     */
    private double[] getTextureCoordinates(int faceIndex) {
        double[] coords = new double[8]; // x1, y1, x2, y2, x3, y3, x4, y4
        
        // 64x64像素皮肤
        // 64x64 pixel skin
        double texWidth = 64.0;
        double texHeight = 64.0;
        
        // 根据面索引确定纹理坐标
        // Determine texture coordinates based on face index
        switch (faceIndex) {
            // 头部
            case HEAD_TOP:
                coords = new double[] {8/texWidth, 0/texHeight, 16/texWidth, 0/texHeight, 16/texWidth, 8/texHeight, 8/texWidth, 8/texHeight};
                break;
            case HEAD_BOTTOM:
                coords = new double[] {16/texWidth, 0/texHeight, 24/texWidth, 0/texHeight, 24/texWidth, 8/texHeight, 16/texWidth, 8/texHeight};
                break;
            case HEAD_RIGHT:
                coords = new double[] {0/texWidth, 8/texHeight, 8/texWidth, 8/texHeight, 8/texWidth, 16/texHeight, 0/texWidth, 16/texHeight};
                break;
            case HEAD_FRONT:
                coords = new double[] {8/texWidth, 8/texHeight, 16/texWidth, 8/texHeight, 16/texWidth, 16/texHeight, 8/texWidth, 16/texHeight};
                break;
            case HEAD_LEFT:
                coords = new double[] {16/texWidth, 8/texHeight, 24/texWidth, 8/texHeight, 24/texWidth, 16/texHeight, 16/texWidth, 16/texHeight};
                break;
            case HEAD_BACK:
                coords = new double[] {24/texWidth, 8/texHeight, 32/texWidth, 8/texHeight, 32/texWidth, 16/texHeight, 24/texWidth, 16/texHeight};
                break;
                
            // 躯干
            case TORSO_TOP:
                coords = new double[] {20/texWidth, 16/texHeight, 28/texWidth, 16/texHeight, 28/texWidth, 20/texHeight, 20/texWidth, 20/texHeight};
                break;
            case TORSO_BOTTOM:
                coords = new double[] {28/texWidth, 16/texHeight, 36/texWidth, 16/texHeight, 36/texWidth, 20/texHeight, 28/texWidth, 20/texHeight};
                break;
            case TORSO_RIGHT:
                coords = new double[] {16/texWidth, 20/texHeight, 20/texWidth, 20/texHeight, 20/texWidth, 32/texHeight, 16/texWidth, 32/texHeight};
                break;
            case TORSO_FRONT:
                coords = new double[] {20/texWidth, 20/texHeight, 28/texWidth, 20/texHeight, 28/texWidth, 32/texHeight, 20/texWidth, 32/texHeight};
                break;
            case TORSO_LEFT:
                coords = new double[] {28/texWidth, 20/texHeight, 32/texWidth, 20/texHeight, 32/texWidth, 32/texHeight, 28/texWidth, 32/texHeight};
                break;
            case TORSO_BACK:
                coords = new double[] {32/texWidth, 20/texHeight, 40/texWidth, 20/texHeight, 40/texWidth, 32/texHeight, 32/texWidth, 32/texHeight};
                break;
                
            // 右臂
            case RIGHT_ARM_TOP:
                coords = new double[] {44/texWidth, 16/texHeight, 48/texWidth, 16/texHeight, 48/texWidth, 20/texHeight, 44/texWidth, 20/texHeight};
                break;
            case RIGHT_ARM_BOTTOM:
                coords = new double[] {48/texWidth, 16/texHeight, 52/texWidth, 16/texHeight, 52/texWidth, 20/texHeight, 48/texWidth, 20/texHeight};
                break;
            case RIGHT_ARM_RIGHT:
                coords = new double[] {40/texWidth, 20/texHeight, 44/texWidth, 20/texHeight, 44/texWidth, 32/texHeight, 40/texWidth, 32/texHeight};
                break;
            case RIGHT_ARM_FRONT:
                coords = new double[] {44/texWidth, 20/texHeight, 48/texWidth, 20/texHeight, 48/texWidth, 32/texHeight, 44/texWidth, 32/texHeight};
                break;
            case RIGHT_ARM_LEFT:
                coords = new double[] {48/texWidth, 20/texHeight, 52/texWidth, 20/texHeight, 52/texWidth, 32/texHeight, 48/texWidth, 32/texHeight};
                break;
            case RIGHT_ARM_BACK:
                coords = new double[] {52/texWidth, 20/texHeight, 56/texWidth, 20/texHeight, 56/texWidth, 32/texHeight, 52/texWidth, 32/texHeight};
                break;
                
            // 左臂
            case LEFT_ARM_TOP:
                coords = new double[] {36/texWidth, 48/texHeight, 40/texWidth, 48/texHeight, 40/texWidth, 52/texHeight, 36/texWidth, 52/texHeight};
                break;
            case LEFT_ARM_BOTTOM:
                coords = new double[] {40/texWidth, 48/texHeight, 44/texWidth, 48/texHeight, 44/texWidth, 52/texHeight, 40/texWidth, 52/texHeight};
                break;
            case LEFT_ARM_RIGHT:
                coords = new double[] {32/texWidth, 52/texHeight, 36/texWidth, 52/texHeight, 36/texWidth, 64/texHeight, 32/texWidth, 64/texHeight};
                break;
            case LEFT_ARM_FRONT:
                coords = new double[] {36/texWidth, 52/texHeight, 40/texWidth, 52/texHeight, 40/texWidth, 64/texHeight, 36/texWidth, 64/texHeight};
                break;
            case LEFT_ARM_LEFT:
                coords = new double[] {40/texWidth, 52/texHeight, 44/texWidth, 52/texHeight, 44/texWidth, 64/texHeight, 40/texWidth, 64/texHeight};
                break;
            case LEFT_ARM_BACK:
                coords = new double[] {44/texWidth, 52/texHeight, 48/texWidth, 52/texHeight, 48/texWidth, 64/texHeight, 44/texWidth, 64/texHeight};
                break;
                
            // 右腿
            case RIGHT_LEG_TOP:
                coords = new double[] {4/texWidth, 16/texHeight, 8/texWidth, 16/texHeight, 8/texWidth, 20/texHeight, 4/texWidth, 20/texHeight};
                break;
            case RIGHT_LEG_BOTTOM:
                coords = new double[] {8/texWidth, 16/texHeight, 12/texWidth, 16/texHeight, 12/texWidth, 20/texHeight, 8/texWidth, 20/texHeight};
                break;
            case RIGHT_LEG_RIGHT:
                coords = new double[] {0/texWidth, 20/texHeight, 4/texWidth, 20/texHeight, 4/texWidth, 32/texHeight, 0/texWidth, 32/texHeight};
                break;
            case RIGHT_LEG_FRONT:
                coords = new double[] {4/texWidth, 20/texHeight, 8/texWidth, 20/texHeight, 8/texWidth, 32/texHeight, 4/texWidth, 32/texHeight};
                break;
            case RIGHT_LEG_LEFT:
                coords = new double[] {8/texWidth, 20/texHeight, 12/texWidth, 20/texHeight, 12/texWidth, 32/texHeight, 8/texWidth, 32/texHeight};
                break;
            case RIGHT_LEG_BACK:
                coords = new double[] {12/texWidth, 20/texHeight, 16/texWidth, 20/texHeight, 16/texWidth, 32/texHeight, 12/texWidth, 32/texHeight};
                break;
                
            // 左腿
            case LEFT_LEG_TOP:
                coords = new double[] {20/texWidth, 48/texHeight, 24/texWidth, 48/texHeight, 24/texWidth, 52/texHeight, 20/texWidth, 52/texHeight};
                break;
            case LEFT_LEG_BOTTOM:
                coords = new double[] {24/texWidth, 48/texHeight, 28/texWidth, 48/texHeight, 28/texWidth, 52/texHeight, 24/texWidth, 52/texHeight};
                break;
            case LEFT_LEG_RIGHT:
                coords = new double[] {16/texWidth, 52/texHeight, 20/texWidth, 52/texHeight, 20/texWidth, 64/texHeight, 16/texWidth, 64/texHeight};
                break;
            case LEFT_LEG_FRONT:
                coords = new double[] {20/texWidth, 52/texHeight, 24/texWidth, 52/texHeight, 24/texWidth, 64/texHeight, 20/texWidth, 64/texHeight};
                break;
            case LEFT_LEG_LEFT:
                coords = new double[] {24/texWidth, 52/texHeight, 28/texWidth, 52/texHeight, 28/texWidth, 64/texHeight, 24/texWidth, 64/texHeight};
                break;
            case LEFT_LEG_BACK:
                coords = new double[] {28/texWidth, 52/texHeight, 32/texWidth, 52/texHeight, 32/texWidth, 64/texHeight, 28/texWidth, 64/texHeight};
                break;
        }
        
        return coords;
    }
    
    /**
     * 绘制面
     * Draw face
     * 
     * @param vertices 变换后的顶点 | Transformed vertices
     * @param face 面（顶点索引）| Face (vertex indices)
     * @param texCoords 纹理坐标 | Texture coordinates
     */
    private void drawFace(double[][] vertices, int[] face, double[] texCoords) {
        // 准备多边形点
        // Prepare polygon points
        double[] xPoints = new double[face.length];
        double[] yPoints = new double[face.length];
        
        for (int i = 0; i < face.length; i++) {
            xPoints[i] = vertices[face[i]][0];
            yPoints[i] = vertices[face[i]][1];
        }
        
        // 创建纹理四边形变换
        // Create texture quad transformation
        Affine textureTransform = new Affine();
        
        // 设置纹理坐标
        // Set texture coordinates
        double srcX1 = texCoords[0] * skinImage.getWidth();
        double srcY1 = texCoords[1] * skinImage.getHeight();
        double srcX2 = texCoords[2] * skinImage.getWidth();
        double srcY2 = texCoords[3] * skinImage.getHeight();
        double srcX3 = texCoords[4] * skinImage.getWidth();
        double srcY3 = texCoords[5] * skinImage.getHeight();
        double srcX4 = texCoords[6] * skinImage.getWidth();
        double srcY4 = texCoords[7] * skinImage.getHeight();
        
        // 设置目标坐标
        // Set destination coordinates
        double dstX1 = xPoints[0];
        double dstY1 = yPoints[0];
        double dstX2 = xPoints[1];
        double dstY2 = yPoints[1];
        double dstX3 = xPoints[2];
        double dstY3 = yPoints[2];
        double dstX4 = xPoints[3];
        double dstY4 = yPoints[3];
        
        // 设置裁剪区域
        // Set clipping region
        gc.beginPath();
        gc.moveTo(dstX1, dstY1);
        gc.lineTo(dstX2, dstY2);
        gc.lineTo(dstX3, dstY3);
        gc.lineTo(dstX4, dstY4);
        gc.closePath();
        gc.clip();
        
        // 绘制纹理
        // Draw texture
        gc.save();
        
        // 计算变换矩阵
        // Calculate transformation matrix
        double dx1 = dstX2 - dstX1;
        double dy1 = dstY2 - dstY1;
        double dx2 = dstX4 - dstX1;
        double dy2 = dstY4 - dstY1;
        double sx1 = srcX2 - srcX1;
        double sy1 = srcY2 - srcY1;
        double sx2 = srcX4 - srcX1;
        double sy2 = srcY4 - srcY1;
        
        // 这里使用一个简化的变换
        // Using a simplified transformation here
        double det = sx1 * sy2 - sx2 * sy1;
        if (Math.abs(det) < 1e-10) {
            // 行列式接近0，不能计算逆矩阵
            // Determinant close to 0, cannot calculate inverse matrix
            gc.setFill(Color.MAGENTA); // 使用品红色表示错误 | Use magenta to indicate error
            gc.fillPolygon(xPoints, yPoints, face.length);
        } else {
            double a = ( dy2 * sx1 - dy1 * sx2) / det;
            double b = (-dx2 * sx1 + dx1 * sx2) / det;
            double c = dstX1 - a * srcX1 - b * srcX4;
            double d = ( dy1 * sy2 - dy2 * sy1) / det;
            double e = (-dx1 * sy2 + dx2 * sy1) / det;
            double f = dstY1 - d * srcX1 - e * srcX4;
            
            textureTransform.setToTransform(a, d, c, b, e, f);
            gc.setTransform(textureTransform);
            
            // 绘制图像
            // Draw image
            gc.drawImage(skinImage, 0, 0);
        }
        
        gc.restore();
        gc.setClip(null);
    }
} 