/**
 * @file VersionManifest.kt
 * @brief 定义用于解析 Mojang 官方 `version_manifest.json` 文件结构的数据类。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @brief 代表从 Mojang API 获取的版本清单 (`version_manifest.json`) 的根结构。
 *
 * @property latest 包含最新稳定版和快照版 ID 的对象。
 * @property versions 包含所有可用版本基本信息的列表。
 */
@Serializable // 标记这个类可以被 Kotlinx Serialization 处理
data class VersionManifest(
    val latest: LatestVersions,
    val versions: List<VersionInfo>
)

/**
 * @brief 存储版本清单中 `latest` 字段下的信息，也就是最新的稳定版 (release)
 *        和快照版 (snapshot) 的版本 ID。
 *
 * @property release 最新稳定版的版本 ID 字符串。
 * @property snapshot 最新快照版的版本 ID 字符串。
 */
@Serializable
data class LatestVersions(
    val release: String,
    val snapshot: String
)

/**
 * @brief 代表版本清单 (`versions` 列表) 中单个 Minecraft 版本条目的信息。
 *
 * @property id 版本的唯一标识符 (比如 "1.21.5")。
 * @property type 版本类型 (比如 "release", "snapshot", "old_alpha", "old_beta")。
 * @property url 指向这个版本详细信息 JSON 文件 (比如 `1.21.5.json`) 的 URL。
 * @property time 这个版本信息最后更新的时间戳字符串 (ISO 8601 格式)。
 * @property releaseTime 版本的实际发布时间戳字符串 (ISO 8601 格式)。
 *                     注意 JSON 字段名是 `releaseTime`，用 `@SerialName` 来映射。
 * // 属性 `complianceLevel` (合规级别) 在 JSON 里有，但目前启动器没用到，所以忽略掉。
 */
@Serializable
data class VersionInfo(
    val id: String,
    val type: String,
    val url: String,
    val time: String,
    @SerialName("releaseTime") // 把 JSON 里的 releaseTime 字段映射到这个属性
    val releaseTime: String
) 