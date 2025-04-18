# WaZi Minecraft Launcher

[![许可证](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![GitHub 仓库](https://img.shields.io/badge/GitHub-仓库-blue?logo=github)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher)
[![项目页面](https://img.shields.io/badge/项目页面-mc.wazixwx.com-brightgreen)](https://mc.wazixwx.com)
[![构建状态](https://img.shields.io/badge/构建-待配置-lightgrey.svg)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/actions) 
[![最新发布](https://img.shields.io/github/v/release/WaZixwx/WaZi-Minecraft-Launcher?display_name=tag&logo=github&color=orange)](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/releases/latest)

WaZi Minecraft Launcher 是一个使用 Kotlin 和 Compose Multiplatform 构建的开源 Minecraft 启动器。旨在提供一个现代化、跨平台的 Minecraft 游戏启动解决方案。

## 目录

*   [功能特性](#功能特性)
*   [技术栈](#技术栈)
*   [系统要求](#系统要求)
*   [开始使用](#开始使用)
*   [构建项目](#构建项目)
*   [贡献指南](#贡献指南)
*   [行为准则](#行为准则)
*   [许可证](#许可证)
*   [联系方式](#联系方式)

## 功能特性

*   **版本管理**:
    *   从 Mojang 官方 API 获取并显示可用的游戏版本列表 (Release, Snapshot 等)。
    *   自动扫描本地已安装的游戏版本。
    *   提供版本筛选与排序功能。
*   **游戏下载**:
    *   下载指定 Minecraft 版本的核心文件 (Client Jar)。
    *   下载所需的库文件 (Libraries)。
    *   下载资源索引文件 (Asset Index)。
    *   根据资源索引下载游戏资源文件 (Asset Objects)。
    *   支持下载进度显示（整体进度与文件级进度）。
    *   支持基本的下载断点续传。
    *   对下载的文件进行 SHA1 哈希校验以确保文件完整性。
    *   通过并发控制优化下载效率与稳定性。
*   **游戏启动**:
    *   自动提取游戏所需的本地库文件 (Natives)。
    *   根据版本信息构建正确的游戏 Classpath。
    *   依据官方规范组装 JVM 启动参数和游戏启动参数。
    *   调用系统安装的 Java 环境启动游戏进程。
*   **用户界面**:
    *   基于 Compose Multiplatform 构建的现代化图形用户界面。
    *   提供清晰的版本列表展示。
    *   直观的下载/启动按钮及状态反馈。
    *   下载进度条显示。

## 技术栈

*   **编程语言**: [Kotlin](https://kotlinlang.org/)
*   **UI 框架**: [Compose Multiplatform (Desktop)](https://github.com/JetBrains/compose-multiplatform)
*   **构建工具**: [Gradle](https://gradle.org/) (版本 8.7)
*   **网络请求**: [Ktor Client](https://ktor.io/docs/client-create-new-application.html)
*   **JSON 解析**: [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
*   **异步处理**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)

## 系统要求

*   **操作系统**: Windows, macOS, Linux (已在 Windows 10 测试)
*   **Java 开发工具包 (JDK)**: 版本 21 或更高版本。确保已正确配置 `JAVA_HOME` 环境变量或 `java` 命令在系统 PATH 中可用。

## 开始使用

1.  **克隆仓库**:
    ```bash
    git clone https://github.com/WaZixwx/WaZi-Minecraft-Launcher.git
    cd WaZi-Minecraft-Launcher
    ```
2.  **运行启动器**:
    在项目根目录下执行以下命令：
    *   Linux / macOS:
        ```bash
        ./gradlew :app:run
        ```
    *   Windows:
        ```bash
        gradlew.bat :app:run
        ```
    Gradle 将会自动下载依赖并启动应用程序。

## 构建项目

你可以使用 Gradle 构建项目并打包。

*   **构建**:
    ```bash
    # Linux / macOS
    ./gradlew build
    # Windows
    gradlew.bat build
    ```
*   **打包为可执行 Jar (示例)**:
    (具体的打包任务可能需要在 `build.gradle.kts` 文件中配置，例如使用 `shadowJar` 或 `compose.desktop.packageUberJarForCurrentOS` 任务)
    ```bash
    # Linux / macOS (如果配置了 UberJar 任务)
    ./gradlew packageUberJarForCurrentOS 
    # Windows (如果配置了 UberJar 任务)
    gradlew.bat packageUberJarForCurrentOS 
    ```
    打包后的文件通常位于 `app/build/compose/jars/` 目录下。

## 贡献指南

我们欢迎各种形式的贡献！无论是报告 Bug、提出功能建议还是提交代码，都对项目的发展至关重要。请参考我们的 [贡献指南](CONTRIBUTING.md) 了解详细信息。

你可以在 [GitHub Issues](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/issues) 页面查看当前的 Bug 和功能请求，并参与讨论。

## 行为准则

为了营造一个开放和友好的社区环境，我们期望所有参与者都能遵守 [行为准则](CODE_OF_CONDUCT.md)。

## 许可证

本项目采用 [MIT 许可证](LICENSE)。

## 联系方式

*   **GitHub 仓库**: [https://github.com/WaZixwx/WaZi-Minecraft-Launcher](https://github.com/WaZixwx/WaZi-Minecraft-Launcher)
*   **项目页面**: [https://mc.wazixwx.com](https://mc.wazixwx.com)
*   **问题追踪**: [GitHub Issues](https://github.com/WaZixwx/WaZi-Minecraft-Launcher/issues) 