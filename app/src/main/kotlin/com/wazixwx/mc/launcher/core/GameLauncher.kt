package com.wazixwx.mc.launcher.core

import com.wazixwx.mc.launcher.model.VersionDetails
import java.io.File
import com.wazixwx.mc.launcher.model.LibraryRule
import com.wazixwx.mc.launcher.model.OsRule
import java.util.zip.ZipFile
import java.io.FileOutputStream
import java.io.IOException
import kotlinx.serialization.json.*
import kotlinx.serialization.Serializable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * @file GameLauncher.kt
 * @brief 处理启动特定 Minecraft 版本的过程。
 * @author WaZixwx
 * @date 2025-04-14
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
object GameLauncher {

    /**
     * @brief 启动指定的 Minecraft 版本。
     *
     *        这个函数负责协调启动游戏所需的各种步骤，
     *        比如构建类路径、组装参数、解压本地库、最后执行进程。
     *
     * @param versionDetails 要启动的那个版本的详细信息。
     * @param gameDir 游戏文件的根目录 (比如 ".minecraft")。
     * @param nativesDir 本地库 (natives) 应该解压到哪个目录。
     * @param username 用哪个用户名启动游戏。
     * @param jvmArgs 用户或者设置里提供的自定义 JVM 参数。
     * @param maxMemoryMb 分配给 JVM 的最大内存 (单位：兆字节)。
     * @return 布尔值，表示游戏进程是不是成功启动了。
     *         **注意**：返回 `true` 只代表进程启动了，可不保证游戏本身能跑起来没毛病。
     */
    suspend fun launchGame(
        versionDetails: VersionDetails,
        gameDir: File,
        nativesDir: File, // 指定用于存放本地库 (natives) 的目录
        username: String,
        jvmArgs: List<String>,
        maxMemoryMb: Int
    ): Boolean {
        println("GameLauncher: Preparing to launch version ${versionDetails.id}...")
        // 确保目标目录存在，如果没有则创建
        gameDir.mkdirs()
        nativesDir.mkdirs()

        try {
            // 步骤 1: 解压缩本地库 (Natives)
            println("GameLauncher: Extracting natives...")
            val nativesExtracted = extractNatives(versionDetails, gameDir, nativesDir)
            if (!nativesExtracted) {
                 println("GameLauncher: Failed to extract natives.")
                 return false
            }
            println("GameLauncher: Natives successfully extracted to ${nativesDir.absolutePath}")

            // 步骤 2: 构建 Java 类路径 (Classpath)
            println("GameLauncher: Building Classpath...")
            val classpath = buildClasspath(versionDetails, gameDir)
            if (classpath.isEmpty()) {
                println("GameLauncher: Failed to build Classpath (missing core JAR?)")
                return false
            }
            println("GameLauncher: Classpath built successfully.") // 如果需要调试，可以在此打印 Classpath 内容

            // 步骤 3: 获取游戏主类名
            val mainClass = versionDetails.mainClass
            println("GameLauncher: Main class: $mainClass")

            // 步骤 4: 组装 Java 虚拟机 (JVM) 参数
            println("GameLauncher: Assembling JVM arguments...")
            val finalJvmArgs = assembleJvmArguments(
                versionDetails = versionDetails,
                nativesDir = nativesDir,
                classpath = classpath,
                customArgs = jvmArgs,
                maxMemoryMb = maxMemoryMb
            )
            println("GameLauncher: JVM arguments assembled.") // 如果需要调试，可以打印参数

            // 步骤 5: 组装游戏参数
            println("GameLauncher: Assembling game arguments...")
            val gameArguments = assembleGameArguments(
                versionDetails = versionDetails,
                gameDir = gameDir,
                username = username
            )
            println("GameLauncher: Game arguments assembled.") // 如果需要调试，可以打印参数

            // 步骤 6: 查找 Java 可执行文件路径
            val javaPath = findJavaExecutable()
            if (javaPath == null) {
                println("GameLauncher: Java executable not found. Ensure JAVA_HOME is set or 'java' is in the system PATH.")
                return false
            }
             println("GameLauncher: Using Java executable: $javaPath")

            // 步骤 7: 构建完整的启动命令
             val command = mutableListOf<String>()
             command.add(javaPath)
             command.addAll(finalJvmArgs)
             command.add(mainClass)
             command.addAll(gameArguments)

             println("GameLauncher: Final launch command: ${command.joinToString(" ")}") // 打印完整命令，方便调试

             val processBuilder = ProcessBuilder(command)
                 .directory(gameDir) // 设置游戏进程的工作目录
                 .redirectErrorStream(true) // 把错误流重定向到标准输出流，方便统一处理

            // 步骤 8: 启动进程
            println("GameLauncher: Starting game process...")
            val process = processBuilder.start()
            println("GameLauncher: Game process started (PID: ${process.pid()}).") // 试试打印进程 ID

            // 恢复并完善异步输出读取逻辑
            // 使用 CoroutineScope 在 IO 调度器上启动两个协程来分别读取标准输出和标准错误流
            val outputScope = CoroutineScope(Dispatchers.IO) // 创建一个新的协程作用域

            // 启动一个协程来读取标准输出
            outputScope.launch {
                try {
                    process.inputStream.bufferedReader().useLines { lines ->
                        // 逐行读取并打印，可以加个前缀区分一下
                        lines.forEach { println("[Game/${versionDetails.id}/OUT]: $it") }
                    }
                    println("GameLauncher: Standard output stream reading finished for game [${versionDetails.id}].")
                } catch (e: IOException) {
                     println("GameLauncher: IOException while reading standard output for game [${versionDetails.id}]: ${e.message}")
                     // 可以根据需要处理异常，比如记个日志啥的
                }
            }

            // 启动另一个协程来读取标准错误（因为 redirectErrorStream(true)，错误流实际上也会合并到标准输出流）
            // 但为了代码清晰和以后万一不合并流了，还是保留读取错误流的逻辑吧
            // 要是确定永远合并流，可以简化只读取 inputStream
            outputScope.launch {
                 try {
                    // 注意：如果 redirectErrorStream(true) 生效，错误流可能是空的或者没数据
                    process.errorStream.bufferedReader().useLines { lines ->
                         lines.forEach { System.err.println("[Game/${versionDetails.id}/ERR]: $it") } // 打印到标准错误
                    }
                     println("GameLauncher: Standard error stream reading finished for game [${versionDetails.id}].")
                 } catch (e: IOException) {
                     println("GameLauncher: IOException while reading standard error for game [${versionDetails.id}]: ${e.message}")
                 }
            }

            // 可选：加个等待进程结束的逻辑（如果需要的话）
            // val exitCode = process.waitFor() // 这会阻塞当前线程直到游戏进程结束
            // println("GameLauncher: Game process [${versionDetails.id}] exited with code: $exitCode")
            // 对于启动器来说，通常不需要阻塞等待游戏退出，让游戏在后台跑就行了。

            // 之前的逻辑是启动了就算成功
            println("GameLauncher: Process for version ${versionDetails.id} started successfully.")
            // 后面可以考虑加个对进程的等待或者监控机制

            return true // 表明进程启动了 (不代表游戏运行正常)

        } catch (e: Exception) {
            println("GameLauncher: Failed to launch game: ${e.message}")
            e.printStackTrace()
            return false
        }
    }

    /**
     * @brief 将所需的本地库 (例如 .dll, .so, .dylib) 提取到指定目录。
     *
     * @param versionDetails 包含库信息的版本详情对象。
     * @param gameDir 存储库文件的游戏根目录。
     * @param nativesDir 提取出的本地库文件的目标目录。
     * @return 如果所有需要的本地库都成功提取 (或无需提取)，则返回 `true`，否则返回 `false`。
     */
    private fun extractNatives(versionDetails: VersionDetails, gameDir: File, nativesDir: File): Boolean {
        println("GameLauncher: Starting to extract natives for version ${versionDetails.id}...")
        var allExtracted = true // 标记整体提取是不是成功了

        // 确定当前操作系统的本地库分类标识符 (例如 "natives-windows")
        val nativeClassifier = getNativeClassifier()
        if (nativeClassifier == null) {
            println("GameLauncher: Unknown OS ($osName), cannot determine native classifier. Skipping extraction.")
            // 这里可以考虑是返回错误还是仅记录警告。当前假设未知操作系统不需要本地库。
            return true
        }
        println("GameLauncher: Using native classifier: $nativeClassifier")

        // 遍历版本详情中的所有库信息
        for (library in versionDetails.libraries) { // 直接遍历非空列表
            // 1. 检查库的应用规则 (例如，是否仅适用于特定操作系统)
            if (!checkRules(library.rules)) {
                // println("GameLauncher: 根据规则跳过库: ${library.name}") // 可选的调试日志
                continue // 跳过当前库
            }

            // 2. 查找适用于当前操作系统的本地库构件信息
            val nativeArtifact = library.downloads?.classifiers?.get(nativeClassifier)
            if (nativeArtifact == null) {
                // 该库没有适用于当前操作系统的本地库
                continue // 跳过当前库
            }

            // 3. 获取本地库 JAR 文件的路径并检查文件是否存在
            val nativeName = library.natives?.get(nativeClassifier) ?: nativeClassifier // 获取正确的分类器名称
            val nativeJarPath = DownloadManager.getLibraryPath(library.name, nativeName)
            if (nativeJarPath == null) {
                println("GameLauncher: Error - Could not generate native artifact path for library ${library.name} (classifier: $nativeName).") // 修正错误信息
                allExtracted = false
                continue // 缺少路径，无法处理
            }

            val nativeJarFile = File(gameDir, "libraries/$nativeJarPath")
            if (!nativeJarFile.isFile) {
                println("GameLauncher: Error - Native library JAR file not found: ${nativeJarFile.absolutePath}")
                allExtracted = false
                continue // JAR 文件缺失，无法提取
            }

            // 4. 从本地库 JAR 文件中提取内容
            println("GameLauncher: Extracting natives from ${nativeJarFile.name}...")
            try {
                // 使用 ZipFile 安全地打开和读取 JAR 文件
                ZipFile(nativeJarFile).use { zipFile ->
                    // 获取此库的排除规则 (如果有)
                    val exclusions = library.extractRules?.exclude ?: emptyList()
                    var extractedCount = 0 // 记录成功提取的文件数

                    // 遍历 JAR 文件中的所有条目
                    zipFile.entries().asSequence().forEach { entry -> // 内层循环用 forEach 没问题
                        // 检查当前条目是否匹配排除规则
                        val isExcluded = exclusions.any { entry.name.startsWith(it) }
                        if (isExcluded) {
                            // println("跳过被排除的条目: ${entry.name}") // 用于调试
                            return@forEach // 跳过此 ZipEntry
                        }

                        val outputFile = File(nativesDir, entry.name) // 计算输出文件路径

                        try {
                             // 如果是目录，则创建对应的目录结构
                             if (entry.isDirectory) {
                                 if (!outputFile.exists()) {
                                    outputFile.mkdirs()
                                 }
                             } else {
                                 // 如果是文件，确保其父目录存在
                                 outputFile.parentFile?.mkdirs()
                                 // 从 JAR 中读取输入流并写入到输出文件
                                 zipFile.getInputStream(entry).use { input ->
                                     FileOutputStream(outputFile).use { output ->
                                         input.copyTo(output) // 执行文件复制
                                     }
                                 }
                                 extractedCount++ // 增加成功提取计数
                             }
                         } catch (ioe: IOException) {
                            println("GameLauncher: IOException while extracting ${entry.name} from ${nativeJarFile.name}: ${ioe.message}")
                            // 这里可以考虑是否一次提取失败就应中止整个过程
                            allExtracted = false // 标记整体提取过程失败
                            // 可以记录更详细的信息，但目前选择继续尝试提取其他文件
                         } catch(se: SecurityException){
                            println("GameLauncher: Security exception while creating ${outputFile.path}: ${se.message}")
                            allExtracted = false // 标记整体提取过程失败
                         }
                    } // 内层 forEach 结束 (遍历 ZipEntry)
                    println("GameLauncher: Extracted $extractedCount files from ${nativeJarFile.name}.")
                } // ZipFile.use 结束
            } catch (e: IOException) {
                println("GameLauncher: IOException while opening or reading native library JAR ${nativeJarFile.name}: ${e.message}")
                allExtracted = false
            } catch (e: SecurityException){ // 捕获打开 ZipFile 时可能的安全异常
                println("GameLauncher: Security exception while opening native library JAR ${nativeJarFile.name}: ${e.message}")
                allExtracted = false
            }
        } // 外层 for 循环结束 (遍历 libraries)

        if (!allExtracted) {
            println("GameLauncher: Errors encountered during native library extraction.")
        }

        return allExtracted
    }

    /**
     * @brief 构造启动游戏所需的 Java 类路径 (classpath) 列表。
     *        只包含必要的普通库 JAR 文件，不包含 Native 库的 JAR。
     *
     * @param versionDetails 包含库信息的版本详情对象。
     * @param gameDir 存储库文件和客户端核心 JAR 文件的游戏根目录。
     * @return 一个包含所有需要的库文件和客户端核心 JAR 文件绝对路径的列表。
     *         如果发生错误或缺少必要文件 (如核心 JAR)，则返回空列表。
     */
    private fun buildClasspath(versionDetails: VersionDetails, gameDir: File): List<String> {
        println("GameLauncher: Starting to build Classpath for version ${versionDetails.id}...")
        val classpathEntries = mutableListOf<String>() // 用于存储 Classpath 条目的列表

        // 步骤 1: 添加客户端核心 JAR 文件
        val clientJarPath = "versions/${versionDetails.id}/${versionDetails.id}.jar"
        val clientJarFile = File(gameDir, clientJarPath)
        if (!clientJarFile.isFile) { // 检查文件是否存在且确实是一个文件
             println("GameLauncher: Critical error - Client core JAR file not found: ${clientJarFile.absolutePath}")
             return emptyList() // 缺少核心 JAR，无法启动
        }
        classpathEntries.add(clientJarFile.absolutePath)
        println("GameLauncher: Added client core JAR: ${clientJarFile.name}")

        // 步骤 2: 添加所需的库文件
        var missingLibs = false // 标记是不是缺少库文件
        val currentNativeClassifier = getNativeClassifier() // 获取当前系统分类器
        println("GameLauncher: Current Native Classifier: $currentNativeClassifier")

        for (library in versionDetails.libraries) { // 直接遍历非空列表
             println("--- Processing Library: ${library.name} ---") // <-- 记录库名
            // 检查库的应用规则
            if (!checkRules(library.rules)) {
                 println("GameLauncher: Skipping library due to rules: ${library.name}")
                continue // 跳过当前库
            }

            // --- 判断库是否包含当前系统的 Natives --- 
            val hasNativesForCurrentOS = currentNativeClassifier != null &&
                (library.natives?.containsKey(currentNativeClassifier) == true ||
                 library.downloads?.classifiers?.containsKey(currentNativeClassifier) == true)
            println("GameLauncher: Has Natives for current OS ($currentNativeClassifier)? $hasNativesForCurrentOS") // <-- 记录 native 检查结果

            // --- 根据是不是 Natives 决定处理方式 --- 
            if (hasNativesForCurrentOS) {
                 // 明确跳过包含 Natives 的库
                 println("GameLauncher: Skipping native library for classpath: ${library.name}")
                 continue 
            } else {
                // --- 处理非 Natives 库的主构件 --- 
                val artifact = library.downloads?.artifact
                println("GameLauncher: Artifact path: ${artifact?.path}") // <-- 记录 artifact 路径
                if (artifact?.path == null) {
                    // 没有主构件路径，跳过
                    println("GameLauncher: Skipping library with no main artifact path: ${library.name}")
                    continue 
                }

                // --- 检查普通库文件是否存在 --- 
                val libraryPath = "libraries/${artifact.path}" 
                val libraryFile = File(gameDir, libraryPath)
                println("GameLauncher: Checking for non-native library file: ${libraryFile.absolutePath}") // <-- 记录文件检查

                if (!libraryFile.isFile) {
                    // 普通库文件缺失，记录错误
                    println("GameLauncher: ERROR - Required library file not found: ${libraryFile.absolutePath}")
                    missingLibs = true // 标记发现缺失库
                } else {
                    // 普通库文件存在，添加到 Classpath
                    println("GameLauncher: Adding library to classpath: ${libraryFile.name}")
                    classpathEntries.add(libraryFile.absolutePath)
                }
            }
             println("--- Finished Library: ${library.name} ---") // <-- 记录完成
        } // for 循环 (遍历 libraries) 结束

        // 如果在遍历过程中发现有库文件缺失，则中止启动
        if (missingLibs) {
             println("GameLauncher: Aborting launch due to missing library files.")
             return emptyList()
        }

        println("GameLauncher: Classpath built successfully with ${classpathEntries.size} entries.")
        return classpathEntries
    }

    /**
     * @brief 组装启动游戏所需的 Java 虚拟机 (JVM) 参数列表。
     *
     * @param versionDetails 包含 JVM 参数模板 (arguments.jvm 或旧版 jvmArguments) 的版本详情。
     * @param nativesDir 包含已提取本地库的目录路径。
     * @param classpath 构建好的 Java 类路径列表。
     * @param customArgs 用户通过设置添加的额外 JVM 参数。
     * @param maxMemoryMb 分配给 JVM 的最大内存 (单位：MB)。
     * @return 一个包含所有最终 JVM 参数的字符串列表。
     */
    private fun assembleJvmArguments(
        versionDetails: VersionDetails,
        nativesDir: File,
        classpath: List<String>,
        customArgs: List<String>,
        maxMemoryMb: Int
    ): List<String> {
        println("GameLauncher: Assembling JVM arguments...")
        val finalArgs = mutableListOf<String>() // 存储最终的 JVM 参数

        // 步骤 1: 定义参数模板中可能用到的占位符及其替换值
        val placeholderValues = mapOf(
            "natives_directory" to nativesDir.absolutePath,
            "launcher_name" to "WzsMinecraftLauncher", // TODO: 把启动器名字设成可配置的
            "launcher_version" to "1.0", // TODO: 把启动器版本设成可配置或动态获取
            "classpath" to classpath.joinToString(File.pathSeparator) // 把 Classpath 列表连接成系统路径分隔符的字符串
            // 以后可以根据需要加更多占位符 (比如用户认证信息啥的)
        )

        // 步骤 2: 添加内存设置参数
        finalArgs.add("-Xmx${maxMemoryMb}M") // 设置最大堆内存
        // finalArgs.add("-Xms...") // 可以考虑加个初始堆内存设置 (-Xms)

        // 步骤 3: 处理版本特定的 JVM 参数 (来自 version.json)
        // 优先用现代的 arguments.jvm 格式 (列表形式，支持规则)
        if (versionDetails.arguments?.jvm != null) {
            println("GameLauncher: Using new arguments.jvm format.")
            versionDetails.arguments.jvm.forEach { arg ->
                // 用辅助函数处理单个参数 (可能是字符串或带规则的对象)
                processArgument(arg, placeholderValues)?.let { processedArgs ->
                    finalArgs.addAll(processedArgs) // 把处理后的参数添加到最终列表
                }
            }
        } 
        /* // <-- 注释掉整个 else if 块，因为它引用了不存在的 jvmArguments
        else if (versionDetails.jvmArguments != null) {
            // 回退方案：处理旧版 jvmArguments (单一字符串格式)
            // 注意：旧版格式通常只存在于非常老的版本 JSON 中，现在已经很少见了
            // 且官方并未明确定义旧版字符串参数格式，这里的解析基于常见实践
             println("GameLauncher: Detected legacy jvmArguments format, attempting to parse.")
             // 简单的按空格分割（可能对带空格的参数有问题，但旧格式通常不这么用）
             val legacyArgs = versionDetails.jvmArguments.split(" ").filter { it.isNotBlank() }
             legacyArgs.forEach { legacyArg ->
                 finalArgs.add(replacePlaceholders(legacyArg, placeholderValues))
             }
        }
        */
        else {
            println("GameLauncher: Warning - JVM arguments definition not found (neither new nor legacy format).")
            // 就算没有版本特定的 JVM 参数，核心参数（比如内存、classpath）还是会加的
        }
        // 已基本实现回退逻辑 (回退部分已注释掉)

        // 步骤 4: 确保核心 JVM 参数存在 (主要是 -Djava.library.path 和 -cp)
        // 检查本地库路径是不是已经通过占位符替换加进去了，如果没有，手动加
        if (finalArgs.none { it.startsWith("-Djava.library.path=") }) {
             println("GameLauncher: Manually adding -Djava.library.path argument.")
             finalArgs.add("-Djava.library.path=${nativesDir.absolutePath}")
        }
        // 检查 Classpath 是不是已经通过占位符替换加进去了，如果没有，手动加
         if (finalArgs.none { it == "-cp" }) {
             println("GameLauncher: Manually adding -cp argument.")
             finalArgs.add("-cp")
             finalArgs.add(classpath.joinToString(File.pathSeparator))
        }

        // 步骤 5: 添加用户自定义的 JVM 参数
        // 进行简单过滤，避免重复设置核心参数 (比如 -Xmx)
        customArgs.forEach { customArg ->
             val key = customArg.split("=")[0].split(" ")[0] // 提取参数键 (比如 -Xmx, -Dsome.prop)
             if (finalArgs.none { it.startsWith(key) }) { // 如果最终参数列表里不存在以此键开头的参数
                 finalArgs.add(customArg) // 就添加自定义参数
             } else {
                 println("GameLauncher: Skipping duplicate custom JVM argument: $customArg (already provided by version or defaults)")
             }
         }
        // // 或者简单粗暴地直接加所有自定义参数，让用户自己负责覆盖问题
        // finalArgs.addAll(customArgs)

        println("GameLauncher: Final assembled JVM arguments: ${finalArgs.joinToString(" ")}")
        return finalArgs
    }

    /**
     * @brief 组装 Minecraft 游戏本身需要的参数列表。
     *
     * @param versionDetails 包含游戏参数模板 (arguments.game 或旧版 minecraftArguments) 的版本详情。
     * @param gameDir 游戏根目录。
     * @param username 玩家用户名。
     * @return 一个包含所有最终游戏参数的字符串列表。
     */
    private fun assembleGameArguments(
        versionDetails: VersionDetails,
        gameDir: File,
        username: String
    ): List<String> {
         println("GameLauncher: Assembling game arguments...")
        val finalArgs = mutableListOf<String>() // 存储最终的游戏参数

        // 步骤 1: 定义参数模板中可能用到的占位符及其替换值
        val assetsDir = File(gameDir, "assets") // 计算资源目录路径
        val placeholderValues = mapOf(
            "auth_player_name" to username, // 玩家名
            "version_name" to versionDetails.id, // 版本 ID
            "game_directory" to gameDir.absolutePath, // 游戏根目录绝对路径
            "assets_root" to assetsDir.absolutePath, // 资源根目录绝对路径
            "assets_index_name" to versionDetails.assetIndexInfo.id, // 资源索引 ID (例如 "3")
            // --- 以下是身份验证相关的占位符，目前用默认值或空值 ---
            // TODO: 在实现账户系统后，用真实值替换这些
            "auth_uuid" to "00000000-0000-0000-0000-000000000000", // 玩家 UUID (离线模式)
            "auth_access_token" to "0", // 访问令牌 (离线模式)
            "clientid" to "clientId_placeholder", // 客户端 ID (可能用于 Mojang 服务)
            "auth_xuid" to "xuid_placeholder", // Xbox 用户 ID (用于 MSA 账户)
            "user_type" to "legacy", // 用户类型 ('msa' 或 'legacy')，当前是离线模式，设成 legacy
            // --- 其他常用占位符 ---
            "version_type" to versionDetails.type, // 版本类型 (如 "release", "snapshot")
            "resolution_width" to "854", // 游戏窗口宽度 - TODO: 设成可配置的
            "resolution_height" to "480" // 游戏窗口高度 - TODO: 设成可配置的
             // 以后可能需要根据版本或需求加更多占位符
        )

        // 步骤 2: 处理版本特定的游戏参数 (来自 version.json)
        // 优先用现代的 arguments.game 格式 (列表形式，支持规则)
        if (versionDetails.arguments?.game != null) {
             println("GameLauncher: Using new arguments.game format.")
             versionDetails.arguments.game.forEach { arg ->
                 // 复用处理 JVM 参数的辅助函数，传入游戏相关的占位符
                 processArgument(arg, placeholderValues)?.let { processedArgs ->
                     finalArgs.addAll(processedArgs) // 把处理后的参数添加到最终列表
                 }
             }
        } else if (versionDetails.minecraftArguments != null) {
             // 回退方案：处理旧版 minecraftArguments (单一字符串格式)
             println("GameLauncher: Detected legacy minecraftArguments format, attempting to parse.")
             // 按空格分割，然后替换占位符
             val legacyArgs = versionDetails.minecraftArguments.split(" ").filter { it.isNotBlank() }
             legacyArgs.forEach { legacyArg ->
                 finalArgs.add(replacePlaceholders(legacyArg, placeholderValues))
             }
        } else {
            println("GameLauncher: Warning - Game arguments definition not found (neither new nor legacy format).")
        }

        // // TODO: 添加对旧版 versionDetails.minecraftArguments (单一字符串格式) 的支持作为回退方案
        // // 这需要先将字符串按空格分割，然后对每个部分调用 replacePlaceholders
        // 已基本实现回退逻辑

        println("GameLauncher: Final assembled game arguments: ${finalArgs.joinToString(" ")}")
        return finalArgs
    }

    /**
     * @brief 查找 Java 可执行文件的路径。
     * 会依次尝试以下方法：
     * 1. 检查 `JAVA_HOME` 环境变量指定的路径下的 `bin/javaw.exe` (Windows) 或 `bin/java` (其他系统)。
     * 2. 在系统的 `PATH` 环境变量包含的各个目录中查找 `javaw.exe` 或 `java`。
     *
     * @return Java 可执行文件的绝对路径字符串，如果未找到则返回 `null`。
     */
    private fun findJavaExecutable(): String? {
        val javaHome = System.getenv("JAVA_HOME") // 获取 JAVA_HOME 环境变量
        // 根据操作系统确定 Java 可执行文件的名称 (Windows 下使用 javaw.exe 避免命令行窗口)
        val executableName = if (System.getProperty("os.name").startsWith("Windows")) "javaw.exe" else "java"

        // 尝试使用 JAVA_HOME
        if (!javaHome.isNullOrBlank()) {
            val javaPath = File(javaHome, "bin/$executableName")
            // 检查文件是否存在且可执行
            if (javaPath.isFile && javaPath.canExecute()) {
                return javaPath.absolutePath // 找到，返回绝对路径
            }
        }

        // 如果 JAVA_HOME 未设置或无效，则回退到检查 PATH 环境变量
        val pathEnv = System.getenv("PATH") ?: "" // 获取 PATH 环境变量，为空则使用空字符串
        // 使用系统路径分隔符分割 PATH 变量 (使用 Char 更安全)
        val paths = pathEnv.split(File.pathSeparatorChar)
        for (pathDir in paths) { // 遍历 PATH 中的每个目录
            val javaPath = File(pathDir, executableName)
            // 检查文件是否存在且可执行
            if (javaPath.isFile && javaPath.canExecute()) {
                return javaPath.absolutePath // 找到，返回绝对路径
            }
        }

        return null // 在 JAVA_HOME 和 PATH 中都未找到
    }

    // --- 规则检查逻辑 (Rule Checking Logic) ---
    // 这部分逻辑用来判断特定规则（比如库或参数规则）是不是适用于当前运行环境

    // 使用 lazy 初始化来延迟计算当前操作系统的名称，直到第一次需要时
    private val osName: String by lazy {
        val os = System.getProperty("os.name", "generic").lowercase() // 获取系统名称并转为小写
        when {
            os.contains("mac") || os.contains("darwin") -> "osx"
            os.contains("win") -> "windows"
            os.contains("nux") || os.contains("linux") -> "linux" // 统一识别为 linux
            else -> "unknown" // 未知系统
        }
    }
    // 使用 lazy 初始化来延迟计算当前操作系统的架构
    private val osArch: String by lazy {
        val arch = System.getProperty("os.arch", "generic").lowercase() // 获取系统架构并转为小写
        when {
            arch == "x86" || arch == "i386" || arch == "i686" -> "x86" // 32位 x86
            arch == "amd64" || arch == "x86_64" -> "x64" // 64位 x86
            arch.startsWith("arm") -> { // ARM 架构
                // 尝试判断是 32 位还是 64 位 ARM
                if (System.getProperty("sun.arch.data.model", "32") == "64") "arm64" else "arm32"
            }
            else -> "unknown" // 未知架构
        }
    }

    /**
     * @brief 检查一组规则（通常来自库或参数定义）是不是允许在当前操作系统和架构上应用。
     *
     * @param rules 要检查的规则列表。可以为 `null` 或空。
     * @return 如果规则允许操作 (或者没有规则，默认允许)，返回 `true`，否则返回 `false`。
     */
    private fun checkRules(rules: List<LibraryRule>?): Boolean {
        if (rules == null || rules.isEmpty()) {
            return true // 没有规则 = 默认允许
        }

        var allowed = false // 规则存在时的默认状态：不允许。需要有明确的 allow 规则来覆盖。
                            // 规则的评估顺序和最终结果取决于最后一条匹配的规则。
        for (rule in rules) { // 使用 for 循环遍历
            val osRule = rule.os // 获取规则中的操作系统约束
            val action = rule.action == "allow" // 判断规则是允许 (allow) 还是禁止 (disallow)

            // 检查操作系统名称是否匹配 (osRule.name 为 null 表示匹配所有系统)
            val nameMatches = osRule?.name == null || osRule.name == osName
            // 检查操作系统版本是否匹配 (osRule.version 为 null 表示匹配所有版本)
            // 版本检查使用正则表达式匹配
            val versionMatches = osRule?.version == null || System.getProperty("os.version", "").matches(Regex(osRule.version))
            // 检查操作系统架构是否匹配 (osRule.arch 为 null 表示匹配所有架构)
            val archMatches = osRule?.arch == null || osRule.arch == osArch

            // 如果所有条件都匹配，则将 `allowed` 设置为此规则的 action (允许或禁止)
            // 后续匹配的规则会覆盖前面的结果
            if (nameMatches && versionMatches && archMatches) {
                allowed = action
            }
        }
        return allowed // 返回最后一条匹配规则的决定
    }

    // --- 规则检查逻辑结束 ---

    // --- 参数处理辅助函数 (Argument Processing Helpers) ---

    // 用于解析 JSON 格式参数的 Json 解析器实例
    private val argumentJsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * @brief 用于从 JsonObject 解码包含规则的参数值的临时数据类。
     *        对应 version.json 中 arguments 字段下可能出现的复杂对象结构。
     * @property rules 应用这个参数值的规则列表。
     * @property rules 应用此参数值的规则列表。
     * @property value 参数值本身，可以是字符串或字符串列表 (用 JsonElement 表示以支持两种类型)。
     */
    @Serializable
    private data class RuleBasedArgumentValue(
        val rules: List<LibraryRule>? = null, // 规则列表，可能为空
        val value: JsonElement // 参数值 (字符串或数组)
    )

    /**
     * @brief 处理来自版本 JSON 的单个参数条目。
     *        参数条目可以是简单的 JsonPrimitive 字符串，也可以是包含规则和值的 JsonObject。
     *
     * @param argElement 要处理的参数的 JsonElement 表示。
     * @param placeholderValues 一个 Map，包含需要替换的占位符及其对应的值。
     * @return 处理并替换占位符后的参数字符串列表。如果参数根据规则被禁止，则返回 `null`。
     */
    private fun processArgument(argElement: JsonElement, placeholderValues: Map<String, String>): List<String>? {
        return when (argElement) {
            // 情况 1: 参数是简单的字符串
            is JsonPrimitive -> {
                // 确保确实是字符串类型
                if (!argElement.isString) return emptyList() // 忽略非字符串的基本类型
                // 替换占位符并包装在列表中返回
                listOf(replacePlaceholders(argElement.content, placeholderValues))
            }
            // 情况 2: 参数是包含规则和值的对象
            is JsonObject -> {
                try {
                    //尝试将 JsonObject 解码为 RuleBasedArgumentValue 数据类
                    val ruleArg = argumentJsonParser.decodeFromJsonElement<RuleBasedArgumentValue>(argElement)
                    // 检查此参数的规则是否允许在当前系统上使用
                    if (!checkRules(ruleArg.rules)) {
                        null // 规则禁止此参数，返回 null
                    } else {
                        // 规则允许，处理参数值 (value 可能是字符串或数组)
                        when (val valueElement = ruleArg.value) {
                            // 值是单个字符串
                            is JsonPrimitive -> {
                                if (!valueElement.isString) return emptyList() // 忽略非字符串
                                listOf(replacePlaceholders(valueElement.content, placeholderValues))
                            }
                            // 值是字符串数组
                            is JsonArray -> {
                                // 遍历数组，替换每个字符串元素的占位符
                                valueElement.mapNotNull { element ->
                                    if (element is JsonPrimitive && element.isString) {
                                        replacePlaceholders(element.content, placeholderValues)
                                    } else {
                                        null // 忽略数组中的非字符串元素
                                    }
                                }
                            }
                            else -> emptyList() // 值是预料之外的类型，返回空列表
                        }
                    }
                } catch (e: Exception) {
                     println("GameLauncher: 解析带规则的参数对象失败: $argElement. 错误: ${e.message}")
                     emptyList() // 解析失败，视为空参数
                }
            }
            // 情况 3: 参数是其他未知的 JSON 类型
            else -> {
                println("GameLauncher: 警告 - 跳过未知的参数 JSON 类型: ${argElement::class.simpleName}")
                emptyList() // 返回空列表
            }
        }
    }

    /**
     * @brief 将字符串中格式如 `${key}` 的占位符替换为 `values` 映射中提供的实际值。
     *
     * @param argument 包含潜在占位符的原始字符串参数。
     * @param values 一个 Map，其键是不带 `${}` 的占位符名称，值是替换后的字符串。
     * @return 占位符被替换后的最终字符串。
     */
    private fun replacePlaceholders(argument: String, values: Map<String, String>): String {
        var result = argument // 从原始参数开始
        values.forEach { (key, value) ->
            // 对每个占位符键值对，执行替换操作
            result = result.replace("\\$\\{$key\\}", value) // 使用 replace 进行替换
        }
        return result // 返回替换完成的字符串
    }

    // --- 参数处理辅助函数结束 ---

    // --- 本地库分类标识符辅助函数 ---

    /**
     * @brief 根据当前操作系统获取相应的本地库 (natives) 分类标识符字符串。
     *        例如，Windows 返回 "natives-windows"。
     *
     * @return 分类标识符字符串，如果操作系统无法识别，则返回 `null`。
     */
     private fun getNativeClassifier(): String? {
         return when (osName) { // osName 是预先计算好的
             "windows" -> "natives-windows"
             "linux" -> "natives-linux"
             "osx" -> "natives-osx"
             else -> null // 未知操作系统
         }
         // 注意：此实现目前未考虑 CPU 架构 (例如 natives-windows-x86)。
         // 现代 Minecraft 版本通常不再区分 x86/x64 的本地库，
         // 但如果需要支持非常旧的版本，可能需要在这里加入架构判断。
     }

     // --- 本地库分类标识符辅助函数结束 ---

} // object GameLauncher 结束
