/**
 * @file MojangApiService.kt
 * @brief 用于与 Mojang 官方 API 端点交互的服务。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.core

import com.wazixwx.mc.launcher.model.VersionDetails
import com.wazixwx.mc.launcher.model.VersionManifest
import io.ktor.client.* // Ktor HTTP 客户端核心
import io.ktor.client.call.* // 用于获取响应体 (body)
import io.ktor.client.engine.cio.* // CIO 引擎，适用于 JVM 桌面应用
import io.ktor.client.plugins.contentnegotiation.* // 内容协商插件，用于处理 JSON 等格式
import io.ktor.client.request.* // 用于构建 HTTP 请求 (例如 get)
import io.ktor.http.* // HTTP 相关的定义 (例如状态码检查 isSuccess)
import io.ktor.serialization.kotlinx.json.* // Ktor 对 Kotlinx Serialization 的集成
import kotlinx.serialization.json.Json
// import io.ktor.client.plugins.logging.* // 如果需要详细的网络日志，可以取消注释
import io.ktor.client.plugins.HttpTimeout // 超时配置插件

/**
 * @brief 提供与 Mojang API 交互的功能，比如获取版本清单和版本详情。
 *        这是一个单例对象 (object)，提供全局访问点。
 */
object MojangApiService {

    // Mojang 官方版本清单 JSON 文件的 URL
    private const val VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json"

    // 配置 Ktor HTTP 客户端实例
    private val client = HttpClient(CIO) { // 使用 CIO 引擎，适合在 JVM 环境下运行
        // // 配置日志记录插件 (如果需要调试网络请求，取消注释)
        // install(Logging) {
        //     logger = Logger.DEFAULT // 使用默认日志记录器
        //     level = LogLevel.HEADERS // 设置日志级别 (例如 INFO, HEADERS, BODY)
        // }

        // 安装并配置内容协商插件，让它能自动处理 JSON 响应
        install(ContentNegotiation) {
            json(Json { // 使用 Kotlinx Serialization 作为 JSON 处理器
                isLenient = true // 允许 JSON 格式不严格 (比如，末尾逗号)
                ignoreUnknownKeys = true // 忽略 JSON 里有但数据类里没有定义的字段
            })
        }

        // // 配置默认请求参数 (比如，可以给所有请求加个 User-Agent 头)
        // defaultRequest {
        //     header("User-Agent", "WazsMinecraftLauncher/1.0") // 设置自定义 User-Agent
        // }

        // 配置 HTTP 超时参数
        install(HttpTimeout) {
            requestTimeoutMillis = 120000 // 整个请求（包括连接、发送、接收）的最大允许时间 (120 秒)
            connectTimeoutMillis = 60000 // 建立 TCP 连接的最大允许时间 (60 秒)
            socketTimeoutMillis = 60000  // 读取数据时，两个数据包之间的最大允许间隔时间 (60 秒)
            // 之前的默认超时太短了，稍微给长一点，增强网络容错性。
        }

        // // 如果需要通过代理访问网络，可以在这儿配置代理
        // engine {
        //     proxy = ProxyBuilder.http("http://proxy.example.com:8080")
        // }
    }

    /**
     * @brief 从 Mojang API 异步获取并解析官方的 Minecraft 版本清单。
     *
     * @return 解析成功就返回 `VersionManifest` 对象，里面包含所有可用版本的信息；
     *         如果网络请求失败或 JSON 解析出错，就返回 `null`。
     */
    suspend fun getVersionManifest(): VersionManifest? {
        return try {
            println("MojangApiService: Fetching version manifest from Mojang...") // 控制台打印简单日志
            // 发起 GET 请求到版本清单 URL
            val response = client.get(VERSION_MANIFEST_URL)
            // 检查 HTTP 响应状态码是不是表示成功 (2xx)
            if (!response.status.isSuccess()) {
                println("MojangApiService: Failed to fetch version manifest, HTTP status: ${response.status}")
                return null // 请求失败
            }
            // 使用 Ktor 的内容协商功能自动将响应体解析为 VersionManifest 对象
            val manifest = response.body<VersionManifest>()
            println("MojangApiService: Successfully fetched and parsed version manifest.")
            manifest // 返回解析后的对象
        } catch (e: Exception) {
            // 捕获网络请求或 JSON 解析过程中可能出现的任何异常
            println("MojangApiService: Error fetching or parsing version manifest: ${e.message}")
            e.printStackTrace() // 打印详细的错误堆栈信息，方便调试
            null // 返回 null 表示失败
        }
    }

    /**
     * @brief 根据给定的 URL，从 Mojang API 异步获取并解析特定 Minecraft 版本的详细信息。
     *
     * @param url 指向特定版本 JSON 文件的 URL (通常来自版本清单)。
     * @return 解析成功就返回包含版本详细信息的 `VersionDetails` 对象；
     *         如果 URL 无效、网络请求失败或 JSON 解析出错，就返回 `null`。
     */
    suspend fun getVersionDetails(url: String): VersionDetails? {
        // 对传入的 URL 进行简单的格式校验
        if (!url.startsWith("https://") || !url.endsWith(".json")) {
             println("MojangApiService: Error - Invalid version details URL format: $url")
             return null // URL 格式不符合预期
        }
        return try {
            println("MojangApiService: Fetching version details from URL: $url")
            // 发起 GET 请求到指定的版本详情 URL
            val response = client.get(url)
            // 检查 HTTP 响应状态码
            if (!response.status.isSuccess()) {
                println("MojangApiService: Failed to fetch version details from $url, HTTP status: ${response.status}")
                return null // 请求失败
            }
            // 自动将响应体解析为 VersionDetails 对象
            val details = response.body<VersionDetails>()
            println("MojangApiService: Successfully fetched and parsed version details (ID: ${details.id}).")
            details // 返回解析后的对象
        } catch (e: Exception) {
            // 捕获网络或解析异常
            println("MojangApiService: Error fetching or parsing version details from $url: ${e.message}")
            e.printStackTrace()
            null // 返回 null 表示失败
        }
    }

    /**
     * @brief 提供对内部共享的 Ktor HttpClient 实例的访问。
     *        允许其他模块 (比如 DownloadManager) 复用同一个配置好的 HTTP 客户端。
     *
     * @return 配置好的 `HttpClient` 实例。
     */
    fun getClient(): HttpClient = client

    /**
     * @brief 关闭共享的 Ktor HttpClient 实例，释放网络资源。
     *        应该在应用程序退出的时候调用这个方法。
     */
    fun closeClient() {
        client.close() // 关闭 Ktor 客户端
        println("MojangApiService: Ktor HTTP client closed.")
    }

} // object MojangApiService 结束