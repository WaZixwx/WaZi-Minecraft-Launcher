/**
 * @file App.kt
 * @brief 包含应用程序 UI 结构和各个屏幕的主可组合 (Composable) 函数。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.ui

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.Window
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.unit.sp
import com.wazixwx.mc.launcher.core.VersionScanner
import com.wazixwx.mc.launcher.model.MinecraftVersion
import com.wazixwx.mc.launcher.vm.VersionsViewModel
import com.wazixwx.mc.launcher.vm.VersionInfoView
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.wazixwx.mc.launcher.model.VersionDetails
import com.wazixwx.mc.launcher.vm.VersionsScreenState
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

// 根据应用视觉风格定义自定义形状
private val AppShapes = Shapes(
    small = RoundedCornerShape(2.dp), // 为小型 UI 组件 (如按钮) 应用少量圆角
    medium = RoundedCornerShape(2.dp), // 为中型 UI 组件 (如卡片) 应用少量圆角
    large = RoundedCornerShape(2.dp)  // 为大型 UI 组件 (如对话框) 应用少量圆角
)

// 定义浅色主题的调色板 (可以根据需要进一步定制)
private val LightColors = lightColors(
    primary = Color(0xFF6200EE), // 主要颜色，用在关键元素和强调的地方
    primaryVariant = Color(0xFF3700B3), // 主要颜色的变体，通常更深
    secondary = Color(0xFF03DAC6), // 次要颜色，用在浮动操作按钮之类的元素上
    background = Color.Transparent, // 窗口背景保持透明，为了支持可能的毛玻璃或自定义背景效果
    surface = Color(0xFFFFFFFF), // 组件表面颜色 (比如卡片、菜单等)
    onPrimary = Color.White, // 在主颜色上显示的文本和图标的颜色
    onSecondary = Color.Black, // 在次要颜色上显示的文本和图标的颜色
    onBackground = Color.Black, // 在背景色上显示的文本和图标的颜色
    onSurface = Color.Black, // 在表面颜色上显示的文本和图标的颜色
    error = Color(0xFFB00020) // 用来指示错误的颜色
    /* 可以根据 Material Design 指南自定义更多颜色 */
)

// 定义深色主题的调色板 (可以根据需要进一步定制)
private val DarkColors = darkColors(
    primary = Color(0xFFBB86FC),
    primaryVariant = Color(0xFF3700B3),
    secondary = Color(0xFF03DAC6),
    background = Color.Transparent, // 窗口背景保持透明
    surface = Color(0xFF121212), // 深色主题的表面颜色
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    error = Color(0xFFCF6679)
)

// 定义应用程序里的主要屏幕或页面
enum class Screen {
    Home, // 主页
    Versions, // 游戏版本页面
    Settings // 设置页面
}

/**
 * @brief 应用程序 UI 的根 Composable 函数。
 *        负责设置 Material 主题、布局整体结构 (标题栏、导航栏、内容区域)
 *        以及根据当前选择的屏幕渲染不同的内容。
 *
 * @param windowScope 提供对窗口操作 (比如最小化、最大化) 的访问作用域。
 * @param onExit 一个回调函数，在用户请求关闭窗口时调用。
 */
@Composable
fun App(windowScope: WindowScope, onExit: () -> Unit) {
    // 使用 remember 记录当前显示的屏幕状态，初始为 Home
    var currentScreen by remember { mutableStateOf(Screen.Home) }

    // TODO: 实现主题切换逻辑，目前暂时固定为浅色主题
    val useDarkTheme = false
    val colors = if (useDarkTheme) DarkColors else LightColors

    // 应用 Material 主题，包括颜色、排版和形状
    MaterialTheme(
        colors = colors,
        typography = Typography(), // 使用默认排版
        shapes = AppShapes // 使用自定义形状
    ) {
        // Surface 作为应用内容的基础容器，填充整个窗口
        Surface(
            modifier = Modifier.fillMaxSize(),
            // 使用主题定义的表面颜色作为背景 (窗口本身的背景是透明的)
            color = MaterialTheme.colors.surface
        ) {
            // 整体使用垂直布局 (Column)
            Column(modifier = Modifier.fillMaxSize()) {
                // 自定义标题栏区域
                // WindowDraggableArea 允许用户通过拖动这个区域来移动窗口
                windowScope.WindowDraggableArea(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp) // 标题栏固定高度
                            // 给标题栏设置带透明度的背景，方便跟下方内容区分开
                            .background(MaterialTheme.colors.primaryVariant.copy(alpha = 0.8f))
                            .padding(horizontal = 16.dp), // 左右内边距
                        verticalAlignment = Alignment.CenterVertically, // 垂直居中对齐
                        horizontalArrangement = Arrangement.SpaceBetween // 子元素两端对齐
                    ) {
                        // 标题文本
                        Text(
                            text = "Wzs Minecraft Launcher",
                            style = MaterialTheme.typography.subtitle1, // 使用副标题样式
                            color = MaterialTheme.colors.onPrimary // 使用主颜色上的文本颜色
                        )
                        // 窗口控制按钮区域 (最小化, 最大化/还原, 关闭)
                        Row {
                            // 最小化按钮
                            IconButton(onClick = { (windowScope.window as ComposeWindow).isMinimized = true }) {
                                Icon(Icons.Default.Minimize, contentDescription = "Minimize", tint = MaterialTheme.colors.onPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // 按钮间距
                            // 获取当前窗口的放置状态 (最大化或浮动)
                            val currentPlacement = (windowScope.window as ComposeWindow).placement
                            // 根据状态选择不同的图标 (最大化图标或还原窗口图标)
                            val maximizeIcon: ImageVector = if (currentPlacement == WindowPlacement.Maximized)
                                Icons.Default.Window else Icons.Default.Maximize
                            // 最大化/还原按钮
                            IconButton(onClick = {
                                // 点击时切换窗口放置状态
                                (windowScope.window as ComposeWindow).placement = if (currentPlacement == WindowPlacement.Maximized)
                                    WindowPlacement.Floating else WindowPlacement.Maximized
                            }) {
                                Icon(imageVector = maximizeIcon, contentDescription = "Maximize/Restore", tint = MaterialTheme.colors.onPrimary)
                            }
                            Spacer(modifier = Modifier.width(8.dp)) // 按钮间距
                            // 关闭按钮
                            IconButton(onClick = onExit) { // 点击时调用传入的 onExit 回调
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colors.onPrimary)
                            }
                        }
                    }
                }

                // 主内容区域布局：水平排列 (Row)，包含左侧导航栏和右侧内容区域
                Row(modifier = Modifier.fillMaxSize()) {
                    // 左侧导航栏
                    Column(
                        modifier = Modifier
                            .fillMaxHeight() // 高度填充父容器
                            .width(200.dp)   // 固定宽度
                            // 设置浅色背景方便跟内容区域区分
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                            .padding(16.dp) // 内边距
                    ) {
                        Text("Navigation", style = MaterialTheme.typography.h6) // 导航标题
                        Spacer(modifier = Modifier.height(16.dp)) // 标题与按钮间距
                        // 导航按钮
                        Button(onClick = { currentScreen = Screen.Home }) { // 点击切换到主页
                            Text("Home")
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // 按钮间距
                        Button(onClick = { currentScreen = Screen.Versions }) { // 点击切换到版本页
                            Text("Game Versions")
                        }
                        Spacer(modifier = Modifier.height(8.dp)) // 按钮间距
                        Button(onClick = { currentScreen = Screen.Settings }) { // 点击切换到设置页
                            Text("Settings")
                        }
                    }

                    // 右侧内容区域
                    Box(
                        modifier = Modifier
                            .fillMaxHeight() // 高度填充父容器
                            .weight(1f) // 占据剩余的水平空间
                            .padding(16.dp), // 内边距
                        contentAlignment = Alignment.TopStart // 内容默认从左上角开始排列
                    ) {
                        // 根据 currentScreen 的状态显示不同的屏幕内容
                        when (currentScreen) {
                            Screen.Home -> HomeScreen()
                            Screen.Versions -> VersionsScreen()
                            Screen.Settings -> SettingsScreen()
                        }
                    }
                }
            }
        }
    }
}

/**
 * @brief 主页屏幕内容的 Composable 函数。
 *        当前只显示欢迎信息。
 */
@Composable
fun HomeScreen() {
    Column(
        modifier = Modifier.fillMaxSize(), // 填充整个可用空间
        horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
        verticalArrangement = Arrangement.Center // 垂直居中
    ) {
        Text("Welcome to Wzs Minecraft Launcher!", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Please select an option from the left navigation bar.")
        Spacer(modifier = Modifier.height(16.dp))
        // TODO: 以后可以在主页加更多内容，比如新闻、最近玩过的版本啥的
    }
}

/**
 * @brief "游戏版本"屏幕内容的 Composable 函数。
 *        使用 `VersionsViewModel` 来加载、管理和显示版本数据。
 */
@Composable
fun VersionsScreen() {
    // 获取或创建 VersionsViewModel 实例。remember 确保在重组时实例保持不变。
    val viewModel = remember { VersionsViewModel() }
    // 从 ViewModel 获取当前的 UI 状态
    val uiState = viewModel.uiState
    // 获取一个与 Composable 生命周期绑定的协程作用域
    val scope = rememberCoroutineScope() // <--- 获取 CoroutineScope

    // 使用 DisposableEffect 确保在 Composable 离开组合 (销毁) 时，
    // 调用 ViewModel 的 onCleared 方法以取消协程等资源清理操作。
    DisposableEffect(Unit) {
        onDispose {
            viewModel.onCleared()
        }
    }

    // // 可以在 LaunchedEffect 中执行一次性操作，比如初始加载。
    // // 但目前加载逻辑在 ViewModel 的 init 块里执行。
    // LaunchedEffect(Unit) {
    //     viewModel.loadVersions()
    // }

    // 根布局容器
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 显示标题
            Text("Available Minecraft Versions", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(16.dp))

            // 处理加载状态
            if (uiState.isLoading) {
                // 显示居中的加载指示器
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } 
            // 处理错误状态
            else if (uiState.error != null) {
                // 显示错误信息
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error loading versions: ${uiState.error}", color = MaterialTheme.colors.error)
                }
            } 
            // 处理成功加载版本列表的状态
            else {
                // 版本列表区域 (左侧)
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f) // 占据一部分宽度
                            .padding(end = 8.dp) // 与右侧详情区域的间距
                    ) {
                        items(uiState.versions) { version ->
                            // 为每个版本显示一个卡片项
                            VersionItem(version, uiState.downloadingVersionId, uiState.downloadProgress) {
                                // 当版本项或其按钮被点击时，根据状态调用 ViewModel 的方法
                                // 使用 scope.launch 启动协程来处理点击事件
                                scope.launch { // <--- 在 CoroutineScope 中启动
                                    when {
                                        uiState.downloadingVersionId == version.id -> {
                                            // 如果正在下载这个版本，点击无效或未来可实现取消
                                            println("Version ${version.id} is currently downloading, please wait...")
                                            // TODO: 实现取消下载功能？
                                        }
                                        version.isInstalled -> {
                                            // 如果已安装，就调用启动函数
                                            println("UI: Requesting launch for version ${version.id}")
                                            viewModel.launchVersion(version)
                                        }
                                        else -> {
                                            // 如果没安装且没下载，就调用下载函数
                                            println("UI: Requesting download for version ${version.id}")
                                            viewModel.downloadVersion(version)
                                        }
                                    }
                                } // <--- CoroutineScope launch 结束
                            }
                            Spacer(modifier = Modifier.height(8.dp)) // 版本项之间的间距
                        }
                    }
                    // 版本详情区域 (右侧，如果需要)
                    // TODO: 可以在这里加个显示所选版本详细信息的部分
                    // Box(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    //     if (uiState.isDetailsLoading) { ... }
                    //     else if (uiState.selectedVersionDetails != null) { ... }
                    // }
                }
            }
        } // Column 结束
    } // Box 结束
}

/**
 * @brief Composable 函数，用于显示版本列表中的单个版本项。
 *
 * @param version 要显示的版本信息。
 * @param downloadingVersionId 当前正在下载的版本 ID (如果有)。
 * @param downloadProgress 当前的下载进度 (0.0 到 1.0)，如果不在下载则为 null。
 * @param onClick 当这个版本项被点击时的回调函数。
 */
@Composable
fun VersionItem(
    version: VersionInfoView,
    downloadingVersionId: String?,
    downloadProgress: Float?,
    onClick: () -> Unit
) {
    // 判断当前是不是正在下载这个特定版本
    val isDownloadingThis = version.id == downloadingVersionId

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = 2.dp,
        border = BorderStroke(1.dp, Color.LightGray) // 加个边框区分一下
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 左侧信息列
            Column(modifier = Modifier.weight(1f)) {
                Text(version.id, fontWeight = FontWeight.Bold) // 版本 ID 加粗
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    // 便于国际化或后续处理
                    Text("Type: ${version.type ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Released: ${version.releaseTime ?: "Unknown"}", fontSize = 12.sp, color = Color.Gray)
                }
                // 如果正在下载这个版本，显示进度条
                if (isDownloadingThis && downloadProgress != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = downloadProgress, // 使用传入的进度值
                        modifier = Modifier.fillMaxWidth().height(4.dp) // 让进度条细一点
                    )
                }
            }
            // 右侧按钮
            Button(
                onClick = onClick, // 点击事件委托给外部
                enabled = !isDownloadingThis, // 如果正在下载这个版本，就禁用按钮
                colors = ButtonDefaults.buttonColors(
                    // 根据是不是已安装决定按钮颜色
                    backgroundColor = if (version.isInstalled) Color(0xFF4CAF50) else MaterialTheme.colors.secondary
                )
            ) {
                Text(
                    text = when { // 按钮文本
                        isDownloadingThis -> "Downloading..."
                        version.isInstalled -> "Launch"
                        else -> "Download"
                    },
                    color = Color.White // 按钮文字颜色
                )
            }
        }
    }
}

/**
 * @brief 设置屏幕内容的 Composable 函数。
 *        当前是占位符。
 */
@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Settings Page", style = MaterialTheme.typography.h5)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Various launcher settings can be placed here in the future.")
        // TODO: 添加实际的设置选项，比如：
        // - Minecraft 游戏目录选择
        // - Java 可执行文件路径配置
        // - JVM 内存分配设置
        // - 下载并发数设置
        // - 账户管理入口
        // - 主题切换
    }
}


// @Preview
// @Composable
// fun AppPreview() {
//     // 注意：预览可能无法完全模拟 WindowScope 或 ViewModel 行为
//     // 为了进行预览，可以创建模拟数据或空回调
//     App(fakeWindowScope, {}) 
// }
// 
// // 用于预览的伪 WindowScope 实现
// private val fakeWindowScope = object : WindowScope {
//     override val window: ComposeWindow
//         get() = error("Preview does not provide a real ComposeWindow")
// }
// 
// @Preview
// @Composable
// fun HomeScreenPreview() {
//     MaterialTheme {
//         HomeScreen()
//     }
// }
// 
// @Preview
// @Composable
// fun VersionsScreenPreview_Loading() {
//     val fakeViewModel = VersionsViewModel().apply {
//         // 手动设置状态以进行预览
//         // uiState = VersionsScreenState(isLoading = true) 
//         // 注意：直接修改私有 setter 可能不被允许，需要调整 ViewModel 或使用模拟框架
//     }
//     MaterialTheme {
//         // VersionsScreen(fakeViewModel) // 传递模拟的 ViewModel
//         Box(modifier=Modifier.fillMaxSize(), contentAlignment = Alignment.Center){ CircularProgressIndicator() }
//     }
// }
// 
// @Preview
// @Composable
// fun VersionsScreenPreview_Loaded() {
//     val fakeViewModel = VersionsViewModel().apply {
//         // uiState = VersionsScreenState(
//         //     isLoading = false,
//         //     versions = listOf(
//         //         VersionInfoView("1.20.4", "release", "2023-12-07T10:00:00+00:00", true, "url1"),
//         //         VersionInfoView("1.20.3", "release", "2023-11-15T10:00:00+00:00", false, "url2"),
//         //         VersionInfoView("23w51b", "snapshot", "2023-12-20T15:00:00+00:00", false, "url3")
//         //     )
//         // )
//     }
//     MaterialTheme {
//         // VersionsScreen(fakeViewModel)
//         // 简化预览，只显示列表项的示例
//         LazyColumn { 
//             item { VersionItem(VersionInfoView("1.20.4", "release", "2023-12-07", true, "url1"), null, null) {} }
//             item { VersionItem(VersionInfoView("1.20.3", "release", "2023-11-15", false, "url2'), null, null) {} }
//             item { VersionItem(VersionInfoView("23w51b", "snapshot", "2023-12-20", false, "url3"), "23w51b", 0.5f) {} }
//         }
//     }
// }
// 
// @Preview
// @Composable
// fun SettingsScreenPreview() {
//     MaterialTheme {
//         SettingsScreen()
//     }
// } 