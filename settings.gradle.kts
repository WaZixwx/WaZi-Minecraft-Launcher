/**
 * @file settings.gradle.kts
 * @brief Gradle 配置文件。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */

import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        // 插件仓库配置放这儿。
        // 国内嘛，还是用腾讯云镜像快一点，不然 Gradle 下载插件能等到天荒地老。
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        google()
        gradlePluginPortal()
        mavenCentral()
        // Compose 的开发版仓库也加上，万一要用呢。
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    // 依赖解析管理，这块儿得搞好。
    // 官方推荐用 FAIL_ON_PROJECT_REPOS，说是更安全、更能保证构建结果一致，那就用它吧。
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 依赖仓库配置。
        // 同样，腾讯云镜像加速，嗖嗖的。
        maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
        google()
        mavenCentral()
        // Compose 开发版仓库。
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        // 下面这些是 Minecraft 相关的仓库，暂时注释掉，需要的时候再打开。
        // 比如官方库、Forge、Fabric 之类的。
        // maven("https://libraries.minecraft.net")
        // maven("https://maven.minecraftforge.net/")
        // maven("https://maven.fabricmc.net/")
    }
}

// 设置一下根项目的名字。
rootProject.name = "WzsMinecraftLauncher" // 这名字挺好，暂时不改了。

// 把主应用模块 `:app` 包含进来。
include(":app") 