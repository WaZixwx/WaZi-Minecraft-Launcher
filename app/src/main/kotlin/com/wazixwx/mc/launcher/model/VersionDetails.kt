/**
 * @file VersionDetails.kt
 * @brief 定义用于解析详细 Minecraft 版本 JSON 文件（例如 `1.21.5.json`）结构的数据类。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement // 用于处理启动参数中可能混合字符串和对象的复杂结构

/**
 * @brief 代表详细版本 JSON 文件的根对象结构。
 *        包含了启动游戏所需的所有信息，如图书馆、资源、主类、参数等。
 */
@Serializable
data class VersionDetails(
    val arguments: Arguments? = null, // 启动参数，新版本格式。旧版本可能没有这个。
    @SerialName("assetIndex") // JSON 字段名为 assetIndex
    val assetIndexInfo: AssetIndexInfo, // 资源索引文件的信息
    val assets: String, // 资源索引的 ID (比如 "12")，用于确定资源索引文件名 (比如 "12.json")
    val downloads: DownloadsInfo, // 包含游戏核心文件（比如 client.jar）的下载信息
    val id: String, // 版本的唯一标识符 (比如 "1.21.5")
    val javaVersion: JavaVersionInfo? = null, // 推荐的 Java 版本信息，旧版本可能没有
    val libraries: List<LibraryInfo>, // 游戏运行所需的库列表
    val mainClass: String, // 游戏的主入口类 (比如 "net.minecraft.client.main.Main")
    val type: String, // 版本类型 (比如 "release", "snapshot")
    val minimumLauncherVersion: Int? = null, // 官方启动器所需的最低版本号，咱这启动器可以忽略
    val complianceLevel: Int? = null, // 合规级别，通常与官方服务相关，咱这启动器可以忽略
    // 其他字段如 logging, time, releaseTime 等，暂时对基础启动功能不是必需的，先忽略

    // --- 添加旧版参数字段 (官方 JSON 中主要是 minecraftArguments) ---
    val minecraftArguments: String? = null, // 旧版游戏参数 (单一字符串，用空格分隔)
    // 旧版 JSON 通常不包含独立的 jvmArguments 字段，JVM 参数多与游戏参数混合在 minecraftArguments 中或硬编码
    // 这里保留 jvmArguments 字段以防万一，但实际用到的概率比较低。
    // val jvmArguments: String? = null // 如果确实需要处理独立的旧版 JVM 参数字符串，取消此注释
)

/**
 * @brief 代表游戏启动参数 (`arguments` 字段)。
 *        包含游戏特定参数 (`game`) 和 JVM 参数 (`jvm`)。
 *
 * 注意：这里的结构比较复杂，参数列表可以是字符串，也可以是带规则的对象。
 *       目前用 `JsonElement` 来通用处理，后面可能需要更精细的解析逻辑来处理规则。
 */
@Serializable
data class Arguments(
    val game: List<JsonElement>, // 游戏参数列表，元素可能是字符串或带规则的对象
    val jvm: List<JsonElement>   // JVM 参数列表，同上
)

/**
 * @brief 存储资源索引文件 (`assets index`) 的相关信息。
 *        资源索引文件定义了所有游戏资源（贴图、声音等）的下载信息。
 */
@Serializable
data class AssetIndexInfo(
    val id: String, // 资源索引的版本 ID (比如 "12")
    val sha1: String, // 资源索引 JSON 文件本身的 SHA1 校验和
    val size: Long, // 资源索引 JSON 文件的大小（字节）
    val totalSize: Long? = null, // 所有资源文件的总大小（字节），新版本提供，旧版本可能没有
    val url: String // 资源索引 JSON 文件的下载 URL
)

/**
 * @brief 包含核心可下载文件（比如客户端 JAR、混淆映射等）的信息。
 */
@Serializable
data class DownloadsInfo(
    val client: DownloadItem? = null, // 客户端 JAR (client.jar) 的下载信息
    @SerialName("client_mappings")
    val clientMappings: DownloadItem? = null, // 客户端混淆映射文件的下载信息
    val server: DownloadItem? = null, // 服务端 JAR 的下载信息（启动器通常不需要）
    @SerialName("server_mappings")
    val serverMappings: DownloadItem? = null // 服务端混淆映射文件的下载信息（启动器通常不需要）
    // 可能还包含其他平台特定的下载项，比如 windows_server 等，暂时忽略
)

/**
 * @brief 代表一个具体的可下载项，可以是游戏 JAR、库文件或资源文件。
 */
@Serializable
data class DownloadItem(
    val sha1: String, // 文件的 SHA1 校验和
    val size: Long, // 文件的大小（字节）
    val url: String, // 文件的下载 URL
    val path: String? = null // 文件应该保存的相对路径（主要用于库文件）
)

/**
 * @brief 推荐运行这个游戏版本所需的 Java 版本信息。
 */
@Serializable
data class JavaVersionInfo(
    val component: String, // Java 组件名称 (比如 "java-runtime-gamma")
    val majorVersion: Int // 主要 Java 版本号 (比如 17, 21)
)

/**
 * @brief 代表一个库依赖项。
 *        游戏运行需要下载并加载这些库。
 */
@Serializable
data class LibraryInfo(
    val downloads: LibraryDownloadsInfo? = null, // 库文件的下载信息，纯原生库可能没有这个字段
    val name: String, // 库的 Maven 坐标 (比如 "com.google.code.gson:gson:2.11.0")
    val rules: List<LibraryRule>? = null, // 决定这个库是否应该被包含的规则列表 (比如仅 Windows 需要)
    val natives: Map<String, String>? = null, // 如果是原生库，这里指定不同系统对应的分类器 (比如 "natives-windows")
    @SerialName("extract")
    val extractRules: ExtractRules? = null // 如果是原生库，这里指定解压时的规则
)

/**
 * @brief 包含库的主文件 (`artifact`) 和可能的原生文件 (`classifiers`) 的下载信息。
 */
@Serializable
data class LibraryDownloadsInfo(
    val artifact: DownloadItem? = null, // 库的主 JAR 文件下载信息
    // 原生库分类器到下载信息的映射。
    // key 是原生库分类器 (比如 "natives-windows", "natives-linux")
    // value 是对应的原生库 JAR 文件下载信息
    val classifiers: Map<String, DownloadItem>? = null
)

/**
 * @brief 定义用于决定是否应用某个库、参数或规则的条件。
 */
@Serializable
data class LibraryRule(
    val action: String, // 规则动作，"allow" (允许) 或 "disallow" (禁止)
    val os: OsRule? = null, // 与操作系统相关的规则条件
    val features: Map<String, Boolean>? = null // 与特定游戏特性相关的规则条件 (比如 {"is_demo_user": true})
)

/**
 * @brief 规则中与操作系统相关的条件。
 */
@Serializable
data class OsRule(
    val name: String? = null, // 操作系统名称 ("windows", "osx", "linux")
    val version: String? = null, // 匹配操作系统版本的正则表达式
    val arch: String? = null // 操作系统架构 ("x86", "x64", "arm64" 等)
)

/**
 * @brief 定义从原生库 JAR 文件中提取内容时的排除规则。
 *        通常用于避免解压签名文件之类的东西。
 */
@Serializable
data class ExtractRules(
    // 提取时需要排除的文件或目录模式列表 (比如 "META-INF/")
    val exclude: List<String> = emptyList()
) 