/**
 * @file Main.kt
 * @brief 应用程序入口点文件。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.Window
import com.wazixwx.mc.launcher.ui.App // 导入 App 可组合函数，这是主界面内容
import com.wazixwx.mc.launcher.core.MojangApiService // 导入网络服务，用于关闭客户端

/**
 * @brief 主函数，整个应用程序的入口点。
 *
 *        这个函数负责初始化并运行 Compose Multiplatform 的应用程序窗口。
 *        它设置窗口状态、大小、位置，并指定关闭窗口时的操作。
 */
fun main() = application {
    // 定义一个回调函数，在请求退出应用程序时被调用
    // 目的是在退出前执行清理操作，比如关闭网络连接
    val onExitRequest = {
        MojangApiService.closeClient() // 关闭 Ktor HTTP 客户端，释放网络资源
        exitApplication() // 正式退出应用程序进程
    }

    // 使用 rememberWindowState 来跨应用程序重启记住窗口的状态（比如大小和位置）
    // 这可以提升用户体验，下次打开时窗口恢复到上次关闭时的样子
    val windowState = rememberWindowState(
        size = DpSize(1024.dp, 768.dp),           // 设置窗口的初始尺寸 (单位: dp)
        position = WindowPosition(Alignment.Center) // 设置窗口的初始位置，让它居中显示
    )

    // 创建并显示主应用程序窗口
    Window(
        onCloseRequest = onExitRequest,      // 当用户尝试关闭窗口时（比如点击关闭按钮），执行这个回调
        state = windowState,                     // 把之前创建的窗口状态传递给 Window
        visible = true,                          // 让窗口在启动时可见
        title = "Wzs Minecraft Launcher",       // 设置窗口的标题栏文本
        // icon = painterResource("icon.png"), // 设置窗口图标（资源需要放在相应目录），暂时注释掉
        transparent = true,                      // 设置窗口背景透明。注意：需要配合 undecorated = true 使用
                                                 // 窗口必须是无边框的才能实现透明效果
        undecorated = true,                      // 移除操作系统的标准窗口边框和标题栏，用于完全自定义窗口外观
        resizable = true,                        // 允许用户调整窗口大小
        enabled = true,                          // 窗口是否接收用户输入事件
        focusable = true,                        // 窗口是否可以获得焦点
        alwaysOnTop = false                      // 窗口是否始终保持在其他窗口之上
        // 如果需要，可以在这里加其他窗口参数，比如键盘事件处理
    ) {
        // 在 Window 的内容 lambda 表达式里，'this' 关键字指向 WindowScope
        // WindowScope 提供了对窗口环境的访问，比如关闭窗口、设置标题等
        // 把 WindowScope 和退出回调函数传递给 App 可组合函数，以便 App 内部可以控制窗口或执行退出逻辑
        App(this, onExitRequest) // 渲染主应用程序界面内容
    }
} 