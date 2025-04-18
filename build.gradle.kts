/**
 * @file build.gradle.kts
 * @brief 根 Gradle 构建脚本。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // 这里应用 Kotlin JVM 插件，为 Kotlin 提供支持。
    // 就算主要代码是 Java 写的，Compose Multiplatform 这玩意儿也严重依赖 Kotlin 的基础设施，所以这个插件得加上。
    kotlin("jvm") version "1.9.23" apply false // 版本号得跟 Compose 1.6.x 保持一致，这里用 1.9.23。`apply false` 表示只声明版本，具体应用交给子模块。

    // 应用 Compose Multiplatform 插件。
    // 用个新点的稳定版，目前是 1.6.10。
    id("org.jetbrains.compose") version "1.6.10" apply false // 同样 `apply false`。
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        // 配置 Kotlin 编译选项。
        // 按要求把 JVM 目标版本设置成 21。
        jvmTarget = "21"
    }
} 