/**
 * @file VersionScanner.kt
 * @brief 提供扫描本地已安装 Minecraft 版本的功能。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.core

import com.wazixwx.mc.launcher.model.MinecraftVersion
import java.io.File

/**
 * @brief 负责扫描本地 Minecraft 游戏安装目录，查找已安装的版本。
 *        这是一个单例对象 (object)。
 */
object VersionScanner {

    /**
     * @brief 根据当前运行的操作系统，推断并返回默认的 Minecraft 游戏根目录路径。
     *        比如，Windows 上通常是 `%APPDATA%/.minecraft`，macOS 是 `~/Library/Application Support/minecraft`。
     *
     * @return 一个 `File` 对象，指向推断出的默认 Minecraft 目录；
     *         如果无法识别操作系统或获取用户主目录失败，就返回 `null`。
     */
    private fun getDefaultMinecraftDirectory(): File? {
        val osName = System.getProperty("os.name").lowercase() // 获取操作系统名称并转为小写
        val userHome = System.getProperty("user.home") // 获取用户主目录

        return when {
            // Windows 系统：优先使用 APPDATA 环境变量，如果失败则回退到 user.home 下的 AppData/Roaming
            osName.contains("win") -> File(System.getenv("APPDATA") ?: "$userHome/AppData/Roaming", ".minecraft")
            // macOS 系统
            osName.contains("mac") -> File(userHome, "Library/Application Support/minecraft")
            // Linux 及其他类 Unix 系统
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> File(userHome, ".minecraft")
            // 其他无法识别的操作系统
            else -> null
        }
    }

    /**
     * @brief 扫描默认的 Minecraft `versions` 目录，查找已安装的游戏版本。
     *
     * 当前实现说明:
     * -   这个函数目前只通过检查 `versions` 目录下是否存在与版本 ID 同名的**子目录**来判断版本是否"已安装"。
     * -   它 不会 验证该目录下是否包含有效的版本 JSON 文件 (`<version_id>.json`) 或核心 JAR 文件 (`<version_id>.jar`)。
     * -   所以，扫描结果可能包含不完整或已损坏的版本。
     *
     * @return 一个包含已发现的本地版本信息 (`MinecraftVersion`) 的列表。
     *         列表会根据版本 ID (目录名) 进行降序排序。
     *         如果找不到 versions 目录，就返回空列表。
     */
    fun scanLocalVersions(): List<MinecraftVersion> {
        val mcDir = getDefaultMinecraftDirectory() // 获取默认的 .minecraft 目录
        // 检查获取到的目录是不是有效的
        if (mcDir == null || !mcDir.isDirectory) {
            println("VersionScanner: Warning - Could not find default Minecraft directory.") // 使用 println 输出警告信息
            return emptyList() // 返回空列表表示没找到或无效
        }

        val versionsDir = File(mcDir, "versions") // 定位到 versions 子目录
        // 检查 versions 目录是否存在且确实是一个目录
        if (!versionsDir.isDirectory) {
            println("VersionScanner: Warning - Minecraft versions directory not found at: ${versionsDir.absolutePath}")
            return emptyList() // 返回空列表
        }

        val foundVersions = mutableListOf<MinecraftVersion>() // 初始化用于存储扫描结果的列表
        // 安全地遍历 versions 目录下的所有文件和子目录 (listFiles 可能返回 null)
        versionsDir.listFiles()?.forEach { versionDir ->
            // 当前逻辑：只要是子目录，就认为它代表一个已安装的版本
            if (versionDir.isDirectory) {
                // TODO：将来可以增加更严格的检查，确认版本的完整性，比如：
                // 1. 检查是否存在对应的 <versionDir.name>.json 文件
                // 2. 尝试解析 JSON 文件获取更详细的版本信息 (类型, 发布时间等)
                // 3. 检查是否存在对应的 <versionDir.name>.jar 文件
                // 示例代码片段:
                // val versionJsonFile = File(versionDir, "${versionDir.name}.json")
                // if (versionJsonFile.isFile) { /* 版本 JSON 存在 */ }

                // 把目录名作为版本 ID，目录的绝对路径存起来，创建一个 MinecraftVersion 对象
                foundVersions.add(
                    MinecraftVersion(
                        id = versionDir.name, // 使用目录名作为版本唯一标识符
                        versionDirectoryPath = versionDir.absolutePath // 存储该版本文件的根目录路径
                    )
                )
            }
        } // forEach 结束
        // 返回找到的版本列表，并按版本 ID (字符串形式) 进行降序排序
        // 这使得比较新的版本通常显示在列表前面
        return foundVersions.sortedByDescending { it.id }
    }

} // object VersionScanner 结束 