/**
 * @file DownloadManager.kt
 * @brief 管理 Minecraft 版本文件的下载和验证。
 *        包含解析下载任务、执行下载、SHA1 校验和进度报告等功能。
 * @author WaZixwx
 * @date 2025-04-14
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.core

import com.wazixwx.mc.launcher.model.DownloadItem
import com.wazixwx.mc.launcher.model.LibraryInfo
import com.wazixwx.mc.launcher.model.VersionDetails
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.math.roundToInt
import java.util.concurrent.atomic.AtomicLong
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withPermit

/**
 * @brief 代表下载和验证单个文件所需的信息。
 *
 * @property url 从哪里下载文件的 URL。
 * @property destinationPath 文件应该保存到的相对路径 (相对于游戏根目录)。
 * @property sha1 用于验证文件完整性的预期 SHA1 哈希值。
 * @property size 文件的预期大小 (单位：字节)。
 * @property type 下载内容的类型标识 (比如, "client", "library", "asset_index", "asset_object")。
 */
data class DownloadTaskInfo(
    val url: String,
    val destinationPath: String,
    val sha1: String,
    val size: Long,
    val type: String // 例子: "client", "library", "asset_index", "asset_object"
)

/**
 * @brief 管理解析版本信息、生成下载任务、执行下载和验证文件的过程。
 */
object DownloadManager {

    // Ktor HttpClient 实例。最好由外部注入或通过依赖管理框架提供，
    // 以便更好地控制它的生命周期和配置。这里为了简单起见，
    // 假设调用者会传入一个配置好的实例 (比如来自 MojangApiService)。

    private const val BUFFER_SIZE = 8 * 1024 // 文件下载时用的缓冲区大小 (8 KB)

    /**
     * @brief 解析版本详情对象 (`VersionDetails`)，生成初始的下载任务列表。
     *        这个列表通常包含客户端核心 JAR、资源索引文件以及所需的库文件。
     *        **注意：** 这个函数不处理资源对象 (asset objects) 的下载任务，
     *        资源对象的任务需要在下载并解析资源索引文件后另行生成。
     *
     * @param details 从 Mojang API 获取的 Minecraft 版本详细信息。
     * @return 一个包含初始下载任务 (`DownloadTaskInfo`) 的列表。
     */
    fun parseDownloadTasks(details: VersionDetails): List<DownloadTaskInfo> {
        val tasks = mutableListOf<DownloadTaskInfo>() // 初始化任务列表

        // 步骤 1: 添加客户端核心 JAR 文件的下载任务
        details.downloads.client?.let { clientDownloadInfo -> // 确保客户端下载信息存在
            val path = "versions/${details.id}/${details.id}.jar" // 构造客户端 JAR 的标准存放路径
            tasks.add(
                DownloadTaskInfo(
                    url = clientDownloadInfo.url,
                    destinationPath = path,
                    sha1 = clientDownloadInfo.sha1,
                    size = clientDownloadInfo.size,
                    type = "client" // 标记类型为客户端
                )
            )
        }

        // 步骤 2: 添加资源索引文件的下载任务
        val assetIndexPath = "assets/indexes/${details.assets}.json" // 构造资源索引的标准存放路径
        tasks.add(
            DownloadTaskInfo(
                url = details.assetIndexInfo.url,
                destinationPath = assetIndexPath,
                sha1 = details.assetIndexInfo.sha1,
                size = details.assetIndexInfo.size,
                type = "asset_index" // 标记类型为资源索引
            )
        )

        // 步骤 3: 添加库文件 (包括普通库和本地库) 的下载任务，并应用规则
        details.libraries.forEach { library -> // 遍历所有库
            if (checkRules(library)) { // 检查规则是否允许包含这个库
                // 添加主要构件 (通常是普通的 JAR 文件)
                library.downloads?.artifact?.let { artifactInfo ->
                    val path = getLibraryPath(library.name) // 获取库的标准 Maven 路径
                    if (path != null) { // 确保路径有效
                        tasks.add(
                            DownloadTaskInfo(
                                url = artifactInfo.url,
                                destinationPath = "libraries/$path", // 添加 "libraries/" 前缀
                                sha1 = artifactInfo.sha1,
                                size = artifactInfo.size,
                                type = "library" // 标记类型为库
                            )
                        )
                    }
                }
                // 添加适用于当前操作系统的本地库 (Native) JAR 文件
                getNativeClassifier()?.let { classifier -> // 获取当前系统分类标识符，比如 "natives-windows"
                    // 从库的 natives 映射中找到对应的名称，比如 "natives-windows" (它可能与 classifier 不同)
                    library.natives?.get(classifier)?.let { nativeName ->
                        // 使用 nativeName 从 classifiers 映射中查找具体的下载信息
                        library.downloads?.classifiers?.get(nativeName)?.let { nativeArtifactInfo ->
                            // 获取本地库 JAR 的标准 Maven 路径 (包含分类器)
                            val path = getLibraryPath(library.name, nativeName)
                            if (path != null) { // 确保路径有效
                                tasks.add(
                                    DownloadTaskInfo(
                                        url = nativeArtifactInfo.url,
                                        destinationPath = "libraries/$path", // 添加 "libraries/" 前缀
                                        sha1 = nativeArtifactInfo.sha1,
                                        size = nativeArtifactInfo.size,
                                        type = "native" // 标记类型为本地库
                                    )
                                )
                                // TODO: 本地库的提取规则 (library.extractRules) 需要在 GameLauncher 里处理，
                                //       下载时只负责下载它的 JAR 文件。
                            }
                        }
                    }
                }
            } // if checkRules 结束
        } // forEach library 结束

        // 步骤 4: 添加资源对象的任务 (需要后续步骤完成)
        // 这部分逻辑将在 executeDownloadTasks 中，下载并解析资源索引文件后进行。

        println("DownloadManager: Initial task parsing complete, generated ${tasks.size} tasks (Version: ${details.id}).")
        return tasks
    }

    /**
     * @brief 检查与库关联的规则 (rules)，判断这个库是否应该在当前操作系统和架构上被包含。
     *        当前实现主要基于操作系统名称进行匹配。
     *        规则逻辑: 默认不允许，除非有明确的 `allow` 规则匹配当前系统；
     *        如果有多条规则匹配，以最后一条匹配的规则为准。
     *
     * @param library 包含规则信息的库对象 (`LibraryInfo`)。
     * @return 如果规则允许包含该库，返回 `true`；否则返回 `false`。
     */
    private fun checkRules(library: LibraryInfo): Boolean {
        val rules = library.rules // 获取规则列表
        if (rules == null || rules.isEmpty()) {
            return true // 没有规则 = 默认允许
        }

        var allowed = false // 规则存在时的默认状态：不允许
        rules.forEach { rule -> // 遍历所有规则
            val osRule = rule.os // 获取规则中的操作系统约束部分
            val action = rule.action == "allow" // 判断规则是允许还是禁止

            // 只有当规则包含操作系统约束时才进行匹配
            if (osRule != null) {
                val osName = System.getProperty("os.name", "").lowercase() // 获取当前系统名称
                val requiredOs = osRule.name // 规则要求的系统名称 (比如 "windows", "osx", "linux")

                // 进行系统名称匹配
                val osMatch = when (requiredOs) {
                    "windows" -> osName.contains("win")
                    "osx" -> osName.contains("mac") || osName.contains("darwin")
                    "linux" -> osName.contains("nix") || osName.contains("nux") || osName.contains("aix")
                    null -> true // 如果规则没指定 os.name，就视为匹配所有系统
                    else -> false // 未知的操作系统名称要求，视为不匹配
                }

                // TODO: 如果需要，可以在这里加对 osRule.version 和 osRule.arch 的检查

                // 如果操作系统匹配，就把 allowed 状态更新为此规则的 action
                // 注意：后匹配的规则会覆盖前面的结果
                if (osMatch) {
                    allowed = action
                }
            } else {
                // 如果规则没有 os 约束 (可能是特性规则？目前忽略特性)
                // 根据 Mojang 启动器的行为，没有 os 约束的规则似乎会无条件应用它的 action
                // 这可能会覆盖之前基于操作系统的判断结果
                 allowed = action
            }
        }
        return allowed // 返回最后一条匹配规则的结果
    }

    /**
     * @brief 根据当前运行的操作系统，获取对应的本地库 (natives) 分类标识符字符串。
     *        比如，在 Windows 上返回 "natives-windows"。
     *
     * @return 分类标识符字符串，如果无法识别当前操作系统，就返回 `null`。
     */
    private fun getNativeClassifier(): String? {
        val osName = System.getProperty("os.name", "").lowercase() // 获取系统名称
        // TODO: 将来如果需要支持不同架构的本地库 (比如 natives-windows-x86/x64),
        //       需要在这里结合 System.getProperty("os.arch") 进行判断。
        //       目前的实现假定同一操作系统的 natives 不区分架构。
        return when {
            osName.contains("win") -> "natives-windows"
            osName.contains("mac") || osName.contains("darwin") -> "natives-osx"
            osName.contains("nix") || osName.contains("nux") || osName.contains("aix") -> "natives-linux"
            else -> null // 未知操作系统
        }
    }

    /**
     * @brief 根据库的 Maven 坐标字符串 (比如 "group:artifact:version") 构造其标准存放路径。
     *        例如: "com.google.code.gson:gson:2.11.0" => "com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
     *        对于本地库，会包含分类器，例如:
     *        "org.lwjgl:lwjgl:3.3.3", "natives-windows" => "org/lwjgl/lwjgl/3.3.3/lwjgl-3.3.3-natives-windows.jar"
     *
     * @param name Maven 坐标字符串 (格式: "group:artifact:version")。
     * @param classifier 可选的分类器字符串 (比如, "natives-windows")。
     * @param extension 文件扩展名 (默认为 ".jar")。
     * @return 构造好的相对路径字符串，如果 `name` 格式无效就返回 `null`。
     */
    fun getLibraryPath(name: String, classifier: String? = null, extension: String = ".jar"): String? {
        val parts = name.split(":") // 按冒号分割坐标
        if (parts.size != 3) return null // 必须是三部分: group, artifact, version

        val group = parts[0].replace(".", "/") // 把 group 中的点替换为斜杠
        val artifact = parts[1]
        val version = parts[2]

        // 构造基本文件名: artifact-version
        val baseFileName = "$artifact-$version"
        // 如果有分类器，添加到文件名中: -classifier
        val classifierSuffix = if (classifier != null) "-$classifier" else ""
        // 最终文件名: artifact-version[-classifier].extension
        val fileName = "$baseFileName$classifierSuffix$extension"

        // 完整相对路径: group/path/artifact/version/filename
        return "$group/$artifact/$version/$fileName"
    }

    /**
     * @brief 执行下载任务列表，包括下载、验证文件并报告整体进度。
     *        这个函数会先尝试下载并解析资源索引文件，然后将解析出的资源对象任务
     *        与初始任务合并，最后并发执行所有下载。
     *
     * @param tasks 由 `parseDownloadTasks` 生成的初始下载任务列表。
     * @param gameDir 游戏文件的根目录。
     * @param client 用于执行网络请求的 Ktor HttpClient 实例。
     * @param progressCallback 一个回调函数，用于接收整体下载进度 (范围 0.0 到 1.0)。
     *                         这个回调会在主线程或后台线程被调用，UI 更新需注意切换线程。
     * @return 布尔值，指示是否所有必需的文件都已成功下载并通过验证。
     */
    suspend fun executeDownloadTasks(
        tasks: List<DownloadTaskInfo>,
        gameDir: File,
        client: HttpClient,
        progressCallback: (Float) -> Unit
    ): Boolean {
        println("DownloadManager: Starting download execution...")
        var allSuccessful = true // 标记整体是否成功
        // --- 并发控制 --- 
        // 创建一个信号量，限制同时进行的网络下载数量，避免过多连接拖慢速度或被服务器拒绝
        // 这个值可以根据网络情况调整，8 是一个相对适中的起点
        // 去TMD几千个文件并发下载，谁顶得住啊，加个锁控制下。降低并发数到 8。
        val downloadSemaphore = Semaphore(8)
        // 使用 AtomicLong 来线程安全地累加已下载的总字节数
        val totalDownloaded = AtomicLong(0)
        var totalSize: Long // 所有需要下载的文件的总大小
        // 创建一个可变列表，用于存储所有最终需要下载的任务 (初始任务 + 资源对象任务)
        val allTasksToDownload = tasks.toMutableList()

        // --- 阶段 1: 预下载并解析资源索引文件 --- 
        // 从初始任务列表中查找资源索引文件任务
        val assetIndexTask = allTasksToDownload.find { it.type == "asset_index" }
        var assetTasks: List<DownloadTaskInfo>? = null // 用于存储解析出的资源对象任务
        var downloadedAssetIndexSize = 0L // 记录成功下载的资源索引文件大小

        if (assetIndexTask != null) {
            // 从待下载列表中移除资源索引任务，因为它需要优先单独处理
            allTasksToDownload.remove(assetIndexTask)
            println("DownloadManager: Found asset index task, starting pre-download: ${assetIndexTask.destinationPath}")
            // 调用 downloadFile 下载资源索引，预下载阶段不关心字节级进度
            val indexDownloaded = downloadFile(
                task = assetIndexTask,
                gameDir = gameDir,
                client = client,
                onBytesDownloaded = { /* 空回调 */ }
            )

            if (indexDownloaded) {
                val assetIndexFile = File(gameDir, assetIndexTask.destinationPath)
                // 确保下载后的文件确实存在且是一个文件
                if (assetIndexFile.isFile) {
                    println("DownloadManager: Asset index pre-download successful, starting parsing...")
                    // 调用 parseAssetIndex 解析资源索引文件
                    assetTasks = parseAssetIndex(assetIndexFile)
                    if (assetTasks != null) {
                        println("DownloadManager: Successfully parsed ${assetTasks.size} asset object tasks.")
                        // 记录索引文件大小，用于后续进度计算
                        downloadedAssetIndexSize = assetIndexTask.size
                        // 将解析出的资源对象任务添加到总任务列表
                        allTasksToDownload.addAll(assetTasks)
                    } else {
                        println("DownloadManager: Failed to parse asset index file: ${assetIndexFile.path}. Skipping asset object downloads.")
                        // 这里可以根据需要决定是否将整体标记为失败 (allSuccessful = false)
                    }
                } else {
                     println("DownloadManager: Asset index file not found after download: ${assetIndexFile.path}. Skipping asset object downloads.")
                    // allSuccessful = false
                }
            } else {
                 println("DownloadManager: Failed to pre-download asset index file: ${assetIndexTask.destinationPath}. Skipping asset object downloads.")
                 // allSuccessful = false
            }
        } else {
             println("DownloadManager: Asset index task not found in initial tasks list.")
        }

        // --- 阶段 2: 计算总大小并并发执行所有下载任务 --- 
        // 计算所有剩余任务 (初始任务 - 索引 + 资源对象) 的总大小
        totalSize = allTasksToDownload.sumOf { it.size }
        // 如果资源索引被成功预下载，将其大小预先加到已下载总量中
        if (downloadedAssetIndexSize > 0) {
             totalDownloaded.addAndGet(downloadedAssetIndexSize)
             // 立即报告一次初始进度，反映资源索引已完成
             val initialProgress = if (totalSize > 0) downloadedAssetIndexSize.toFloat() / totalSize else 0f
             progressCallback(initialProgress.coerceIn(0f, 1f)) // 确保进度在 0.0 到 1.0 之间
        }
        // 打印将要下载的总量和文件数 (不包括已预下载的索引文件的大小)
        val remainingSize = totalSize - downloadedAssetIndexSize
        println("DownloadManager: Calculated total download size (excluding pre-downloaded index): ${remainingSize / 1024} KB, ${allTasksToDownload.size} files.")

        // 使用 coroutineScope 创建一个作用域来管理并发的下载任务
        // coroutineScope 会等待其内部启动的所有协程执行完毕
        coroutineScope { 
            // 使用 map 将每个下载任务映射为一个 async 任务 (Deferred)
            val downloadJobs = allTasksToDownload.map { task -> 
                async(Dispatchers.IO) { // 在 IO 线程池上异步执行每个下载
                    // 在执行实际下载前，尝试获取信号量的一个许可
                    // 如果信号量已满 (达到并发上限)，withPermit 会挂起当前协程，直到有许可可用
                    downloadSemaphore.withPermit {
                        println("DownloadManager: Starting download for ${task.type}: ${task.destinationPath}...")
                        // 调用 downloadFile 执行单个文件的下载和验证
                        // 传入一个 lambda 作为字节进度回调
                        val success = downloadFile(task, gameDir, client) { bytesDownloaded ->
                            // 累加刚下载的字节数到总下载量
                            val currentTotal = totalDownloaded.addAndGet(bytesDownloaded)
                            // 计算当前整体进度
                            val progress = if (totalSize > 0) currentTotal.toFloat() / totalSize else 0f
                            // 调用外部传入的进度回调函数，更新 UI (注意线程安全)
                            progressCallback(progress.coerceIn(0f, 1f))
                        }
                        // 如果单个文件下载或验证失败
                        if (!success) {
                            println("DownloadManager: Download or verification failed: ${task.destinationPath}")
                            allSuccessful = false // 将整体成功标记置为 false
                        }
                        success // 返回当前任务的成功状态
                    } // withPermit 结束，自动释放信号量许可
                } // async 结束
            } // map 结束
            // 等待所有通过 map 创建的 async 任务执行完成
            downloadJobs.awaitAll() 
            // allSuccessful 标志已在每个任务内部更新，无需再根据 results 判断
        } // coroutineScope 结束

        println("DownloadManager: Download execution finished. Overall success state: $allSuccessful")
        return allSuccessful
    }

    /**
     * @brief 下载、验证并保存单个文件。
     *        如果文件已存在且 SHA1 校验通过，就跳过下载。
     *
     * @param task 包含文件 URL、目标路径、SHA1 和大小的下载任务信息。
     * @param gameDir 游戏根目录。
     * @param client Ktor HttpClient 实例。
     * @param onBytesDownloaded 一个回调函数，在每次写入数据块后调用，报告写入的字节数。
     * @return 布尔值，指示文件是否成功下载或已存在并通过验证。
     */
    private suspend fun downloadFile(
        task: DownloadTaskInfo,
        gameDir: File,
        client: HttpClient,
        onBytesDownloaded: (Long) -> Unit
    ): Boolean {
        val destinationFile = File(gameDir, task.destinationPath)

        // 检查文件是不是已存在且有效
        if (destinationFile.exists() && destinationFile.length() == task.size) {
            val existingSha1 = calculateSha1(destinationFile)
            if (existingSha1 == task.sha1) {
                println("DownloadManager: File exists and SHA1 matches, skipping: ${task.destinationPath}")
                // 文件有效，报告它的大小作为已下载字节数，确保进度条能反映跳过的文件
                onBytesDownloaded(task.size)
                return true // 跳过下载
            }
             else {
                 println("DownloadManager: File exists but SHA1 mismatch (Expected: ${task.sha1}, Got: $existingSha1). Redownloading: ${task.destinationPath}")
                 destinationFile.delete() // 删除损坏的文件
             }
        } else if (destinationFile.exists()) {
             println("DownloadManager: File exists but size mismatch (Expected: ${task.size}, Got: ${destinationFile.length()}). Redownloading: ${task.destinationPath}")
             destinationFile.delete() // 删除大小错误的文件
        }

        // 确保目标文件的父目录存在
        destinationFile.parentFile?.mkdirs()

        // 执行下载
        return try {
            // 使用 Ktor 发起 GET 请求
            client.prepareGet(task.url).execute { response ->
                if (!response.status.isSuccess()) {
                    println("DownloadManager: Download failed for ${task.destinationPath}: HTTP status ${response.status}")
                    return@execute false // HTTP 请求失败
                }

                val channel: ByteReadChannel = response.body()
                // 使用 FileOutputStream 把响应体写入文件
                withContext(Dispatchers.IO) { // 确保文件写入在 IO 线程执行
                    FileOutputStream(destinationFile).use { outputStream ->
                        val buffer = ByteArray(BUFFER_SIZE)
                        var bytesCopied: Long = 0
                        while (true) {
                            val read = channel.readAvailable(buffer)
                            if (read <= 0) break // 读取结束
                            outputStream.write(buffer, 0, read)
                            bytesCopied += read
                            onBytesDownloaded(read.toLong()) // 报告刚写入的字节数
                        }
                        println("DownloadManager: File write complete: ${task.destinationPath} ($bytesCopied bytes)")
                    }
                }

                // 下载完成后再次验证 SHA1
                val downloadedSha1 = calculateSha1(destinationFile)
                if (downloadedSha1 == task.sha1) {
                    println("DownloadManager: Download complete and SHA1 verified (SHA1: $downloadedSha1): ${task.destinationPath}")
                    true // 验证成功
                } else {
                    println("DownloadManager: Download complete but SHA1 mismatch (Expected: ${task.sha1}, Got: $downloadedSha1). File might be corrupted: ${task.destinationPath}")
                    destinationFile.delete() // 删除校验失败的文件
                    false // 验证失败
                }
            }
        } catch (e: Exception) {
            println("DownloadManager: Exception during download for ${task.destinationPath}: ${e.message}")
            // e.printStackTrace() // 可以取消注释以获取详细堆栈跟踪
            // 尝试删除可能不完整的文件
            try {
                 if (destinationFile.exists()) {
                     destinationFile.delete()
                 }
             } catch (deleteException: SecurityException) {
                 println("DownloadManager: Security exception while trying to delete failed download file ${destinationFile.path}: ${deleteException.message}")
             }
            false // 下载过程中发生异常
        }
    }

    /**
     * @brief 计算给定文件的 SHA-1 哈希值。
     *
     * @param file 要计算哈希值的文件。
     * @return 表示 SHA-1 哈希值的十六进制字符串，如果文件不存在或读取出错就返回 `null`。
     */
    private fun calculateSha1(file: File): String? {
        return try {
            if (!file.isFile) return null // 确保是文件且存在

            val digest = MessageDigest.getInstance("SHA-1") // 获取 SHA-1 摘要实例
            FileInputStream(file).use { fis -> // 使用 try-with-resources 确保流关闭
                val byteArray = ByteArray(1024) // 读取缓冲区
                var bytesCount: Int
                while (fis.read(byteArray).also { bytesCount = it } != -1) { // 循环读取文件内容
                    digest.update(byteArray, 0, bytesCount) // 更新摘要
                }
            }
            // 把计算出的摘要字节数组转换为十六进制字符串
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: IOException) {
            println("DownloadManager: IOException while calculating SHA1 for file ${file.name}: ${e.message}")
            null // IO 错误
        } catch (e: NoSuchAlgorithmException) {
             println("DownloadManager: SHA-1 algorithm not found. This should not happen.")
             null // 基本不可能发生
        } catch (e: SecurityException) {
             println("DownloadManager: Security exception while calculating SHA1 for file ${file.name} (Permission denied?): ${e.message}")
             null // 权限问题
        }
    }

    // --- 资源索引解析相关 --- 

    /**
     * @brief 用于解析资源索引 JSON 文件中 `objects` 字段的临时数据类。
     *        Map 的键是文件名 (比如 "minecraft/lang/en_us.json")，
     *        值是包含哈希和大小的 AssetObjectInfo。
     */
    @Serializable
    private data class AssetIndex(
        val objects: Map<String, AssetObjectInfo>
    )

    /**
     * @brief 代表资源索引中单个资源对象的信息。
     * @property hash 对象的 SHA-1 哈希值。
     * @property size 对象的大小 (字节)。
     */
    @Serializable
    private data class AssetObjectInfo(
        val hash: String,
        val size: Long
    )

    /**
     * @brief 读取并解析已下载的资源索引 JSON 文件，生成资源对象的下载任务列表。
     *
     * @param assetIndexFile 指向本地资源索引 JSON 文件的 `File` 对象。
     * @return 包含所有资源对象下载任务 (`DownloadTaskInfo`) 的列表，如果解析失败就返回 `null`。
     */
    private fun parseAssetIndex(assetIndexFile: File): List<DownloadTaskInfo>? {
        return try {
            val jsonContent = assetIndexFile.readText() // 读取文件内容
            val json = Json { ignoreUnknownKeys = true } // 创建 Json 解析器实例
            val indexData = json.decodeFromString<AssetIndex>(jsonContent) // 解析 JSON

            // 把解析出的 objects 映射转换为 DownloadTaskInfo 列表
            indexData.objects.map { (_, objectInfo) ->
                // 构造资源对象的 URL (基于哈希值)
                // URL 格式: https://resources.download.minecraft.net/xx/xxxxxxxx...
                val hashPrefix = objectInfo.hash.substring(0, 2) // 取哈希值的前两个字符
                val url = "https://resources.download.minecraft.net/$hashPrefix/${objectInfo.hash}"
                // 构造资源对象的本地保存路径
                val destinationPath = "assets/objects/$hashPrefix/${objectInfo.hash}"

                DownloadTaskInfo(
                    url = url,
                    destinationPath = destinationPath,
                    sha1 = objectInfo.hash,
                    size = objectInfo.size,
                    type = "asset_object" // 标记类型为资源对象
                )
            }
        } catch (e: Exception) {
            println("DownloadManager: Error parsing asset index file ${assetIndexFile.name}: ${e.message}")
            e.printStackTrace() // 打印详细错误
            null // 解析失败
        }
    }

} // object DownloadManager 结束 