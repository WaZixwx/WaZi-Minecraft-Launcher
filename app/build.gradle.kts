/**
 * @file app/build.gradle.kts
 * @brief 应用程序模块的 Gradle 构建脚本。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// 如果根项目那边定义了 Compose 的版本号，就用那个，不然就用默认的 1.6.2。
val composeVersion = extra.properties["compose.version"] as? String ?: "1.6.2"

plugins {
    // 应用 Kotlin JVM 插件，Compose 必须用这个。
    kotlin("jvm")
    // 还要应用 Kotlin Serialization 插件，用来处理序列化，比如 JSON。
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23" // 版本号最好跟 kotlin-jvm 插件保持一致。
    // 应用 Compose Multiplatform 插件，专门给桌面应用准备的。
    id("org.jetbrains.compose")
}

// 定义一下项目的组 ID 和初始版本号。
group = "com.wazixwx.mc.launcher"
version = "1.0.0"

// 在 Kotlin 脚本里用 'val' 定义变量。
// 定义一些依赖库的版本号，方便管理。
val ktorVersion = "2.3.10"
val serializationVersion = "1.6.3"

dependencies {
    // 这个是 Compose 桌面平台的聚合依赖，包含了当前操作系统需要的东西。
    implementation(compose.desktop.currentOs)

    // Material Design 风格的组件库，界面就靠它了。
    implementation(compose.material)

    // Material 图标的扩展库，有时候基础图标不够用，就得靠它。
    // 之前好像遇到过图标显示不全的问题，加上这个试试看能不能解决。
    implementation(compose.materialIconsExtended)

    // 下面这些基础依赖被注释掉了，因为 `compose.desktop.currentOs` 应该已经包含了它们。
    // 重复添加没必要，还可能引起冲突。
    // implementation(compose.runtime)
    // implementation(compose.foundation)
    // implementation(compose.ui)
    // implementation(compose.desktop.jvm)

    // Kotlin 序列化库，主要用它的 JSON 支持。
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")

    // Ktor 客户端，用来发网络请求，比如下载游戏文件、验证什么的。
    implementation("io.ktor:ktor-client-core:$ktorVersion") // Ktor 核心库。
    implementation("io.ktor:ktor-client-cio:$ktorVersion") // JVM 平台用的 CIO 引擎，异步网络 IO 就靠它了。
    // Ktor 的内容协商模块，这样它就能自动处理 JSON 解析了。
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion") // 让 Ktor 使用 Kotlinx Serialization 来处理 JSON。

    // Kotlin 协程的 Swing 调度器。
    // 在桌面应用里，如果想在主线程 (Dispatchers.Main) 更新 UI，就得用这个。
    // 版本号通常 Ktor 会带过来一个兼容的版本，或者我们可以明确指定一个跟协程核心库一致的版本。
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing")

    // Ktor 的日志记录依赖，方便调试网络请求。
    implementation("io.ktor:ktor-client-logging:$ktorVersion")

    // 这里先空着，以后有其他依赖再加进来，比如日志框架什么的。
}

java {
    // 明确指定 Java 的源代码和目标字节码兼容性都是 Java 21。
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// --- 明确配置一下 'run' 任务 (我猜它是 JavaExec 类型的) ---
tasks.withType<JavaExec>().configureEach {
    // 给所有 JavaExec 类型的任务都应用编码设置，当然也包括 'run' 任务。
    if (name == "run") { // 如果只想针对 'run' 任务，可以加个判断。
        // 唉，Windows 这默认 GBK 编码真是个坑，为了兼容某些旧环境或者确保开发时控制台输出暂时不乱码，先设成 GBK。
        // 不过讲真，UTF-8 才是未来，后面找机会必须统一成 UTF-8，不然跨平台或者遇到特殊字符迟早出问题。
        // --- 更新：决定了，现在就统一成 UTF-8！GBK 再见！---
        jvmArgs("-Dfile.encoding=UTF-8")
        // 打印一下日志，确认这个配置确实生效了，免得到时候乱码了抓瞎。
        println("JVM argument configured for 'run' task: -Dfile.encoding=UTF-8")
    }
}

compose.desktop {
    application {
        // 指定应用程序的入口点，也就是 main 函数所在的类。
        mainClass = "com.wazixwx.mc.launcher.MainKt"

        // 配置原生分发包的设置。
        nativeDistributions {
            // 指定要打包的目标格式，比如 Windows 的 Msi、Exe，macOS 的 Dmg，Linux 的 Deb。
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "WzsMinecraftLauncher" // 安装包的名字。
            packageVersion = project.version.toString() // 使用项目的版本号作为包的版本号。
            description = "A Modern Minecraft Launcher by WaZixwx" // 应用程序的描述
            copyright = "Copyright (c) 2025 WaZixwx. All rights reserved." // 版权声明
            vendor = "WaZixwx" // 开发商/供应商名称。
            // 这里可以为不同平台设置图标，暂时用占位符路径，后面得换成真实的图标文件路径。
            // windows.iconFile.set(project.file("src/main/resources/icons/icon.ico"))
            // macOS.iconFile.set(project.file("src/main/resources/icons/icon.icns"))
            // linux.iconFile.set(project.file("src/main/resources/icons/icon.png"))

            windows {
                // Windows 平台的特定设置。
                // menuGroup = "WaZixwx 应用" // 开始菜单里的文件夹名字。
                // shortcut = true // 是否创建桌面快捷方式。
                // 这个 UUID 是用来处理应用升级的，得保证每个项目唯一。
                upgradeUuid = "5f2d9a1b-8c7e-4b9f-8e3a-1c9d0f7b3e1a" // 注意：这里需要为你的项目生成一个新的 UUID！
            }
        }

        buildTypes.release {
            // 配置发布构建的选项，比如可以用 ProGuard 来压缩和混淆代码。
            // 这个先注释掉，等需要优化体积或者保护代码的时候再启用。
            //proguard {
            //    isEnabled.set(true)
            //    configurationFiles.from(project.file("proguard-rules.pro"))
            //}
        }
    }
} 