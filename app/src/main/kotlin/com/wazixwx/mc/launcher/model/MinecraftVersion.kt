/**
 * @file MinecraftVersion.kt
 * @brief 代表 Minecraft 版本基本信息的数据类。
 *        这个数据类主要是给本地版本扫描 `VersionScanner` 的结果用的。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.model

/**
 * @brief 表示通过本地扫描发现的 Minecraft 版本的基本信息。
 *
 * @property id 版本的唯一标识符，通常对应 `versions` 目录下的子目录名称
 *             (比如 "1.20.4", "fabric-loader-0.15.7-1.20.4")。
 * @property versionDirectoryPath 指向这个版本文件所在目录的绝对路径字符串。
 *             (比如 "C:/Users/Admin/.minecraft/versions/1.20.4")
 * // 注意：这个数据类只包含最基本的信息。更详细的信息 (比如版本类型、资源、库等)
 * // 需要通过解析版本对应的 JSON 文件 (`<id>.json`) 来获取，那些信息定义在 VersionDetails 数据类里。
 */
data class MinecraftVersion(
    val id: String,
    val versionDirectoryPath: String
) 