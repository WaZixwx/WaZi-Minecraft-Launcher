/**
 * @file VersionsViewModel.kt
 * @brief "游戏版本" 屏幕的 ViewModel (视图模型)，负责处理这个屏幕的数据获取、状态管理和用户交互逻辑。
 * @author WaZixwx
 * @date 2025-04-13
 * @version 1.0.0
 * @copyright Copyright (c) 2025 WaZixwx. 版权所有。
 *            根据 MIT 许可证授权。
 */
package com.wazixwx.mc.launcher.vm // 放在 ViewModel 包下

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.wazixwx.mc.launcher.core.MojangApiService
import com.wazixwx.mc.launcher.core.VersionScanner
import com.wazixwx.mc.launcher.model.MinecraftVersion
import com.wazixwx.mc.launcher.model.VersionInfo // 导入清单版本信息
import com.wazixwx.mc.launcher.model.VersionDetails // 导入版本详情数据类
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import com.wazixwx.mc.launcher.core.GameLauncher // <--- 添加 GameLauncher 导入
import com.wazixwx.mc.launcher.core.DownloadManager // <--- 添加 DownloadManager 导入

/**
 * @brief 代表 "游戏版本" 屏幕的用户界面 (UI) 状态。
 *        这是一个数据类，用来封装所有驱动 UI 显示所需的信息。
 *
 * @property isLoading 指示当前是不是正在加载版本列表 (true 表示正在加载)。
 * @property versions 要在列表里显示的版本信息。这是一个 `VersionInfoView` 对象的列表，
 *                   结合了来自 Mojang API 的信息和本地扫描结果。
 * @property error 如果加载版本列表或执行操作时发生错误，就存储错误消息字符串；否则为 `null`。
 * @property isDetailsLoading 指示当前是不是正在加载所选版本的详细信息 (true 表示正在加载)。
 * @property selectedVersionDetails 持有当前选中的版本的详细信息 (`VersionDetails` 对象)。
 *                               如果没选任何版本或加载失败，就为 `null`。
 * @property downloadingVersionId 记录当前正在下载的版本的 ID。如果没有版本在下载中，就为 `null`。
 *                               用于在 UI 上显示下载状态和禁用相关操作。
 * @property downloadProgress 整体下载进度，范围从 0.0 到 1.0。只有当有版本在下载时才有效，否则为 `null`。
 */
data class VersionsScreenState(
    val isLoading: Boolean = true, // 初始状态为加载中
    val versions: List<VersionInfoView> = emptyList(), // 初始版本列表为空
    val error: String? = null, // 初始无错误
    val isDetailsLoading: Boolean = false, // 初始未加载详情
    val selectedVersionDetails: VersionDetails? = null, // 初始未选择详情
    val downloadingVersionId: String? = null, // 初始无下载任务
    val downloadProgress: Float? = null // 初始无下载进度
)

/**
 * @brief 一个专门为 "游戏版本" 屏幕 UI 设计的数据类，用于在列表里显示单个版本条目。
 *        它整合了来自本地扫描 (`VersionScanner`) 和 Mojang 版本清单 (`VersionManifest`)
 *        的关键信息，方便 UI 直接用。
 *
 * @property id 版本的唯一标识符 (比如, "1.21.5", "fabric-loader-...")。
 * @property type 版本类型 (比如, "release", "snapshot")，可能为空。
 * @property releaseTime 版本的发布时间字符串 (ISO 8601 格式)，可能为空。
 * @property isInstalled 指示这个版本是不是已经在本地检测到了 (基于 `VersionScanner` 的结果)。
 * @property manifestUrl 指向这个版本详细信息 JSON 文件的 URL，用于后续获取详情或下载。
 */
data class VersionInfoView(
    val id: String,
    val type: String?, // 版本类型可能缺失
    val releaseTime: String?, // 发布时间可能缺失
    val isInstalled: Boolean, // 是否已安装
    val manifestUrl: String? // 详情 URL 可能缺失 (理论上不应发生)
)

/**
 * @brief "游戏版本" 屏幕的 ViewModel 实现。
 *        负责:
 *        1.  调用 `MojangApiService` 和 `VersionScanner` 获取数据。
 *        2.  合并、处理数据生成 `VersionsScreenState`。
 *        3.  管理 UI 状态 (`uiState`) 并把它暴露给 Composable 函数。
 *        4.  处理用户交互，比如加载版本、下载版本、启动游戏等，并更新 UI 状态。
 *        5.  管理协程作用域 (`viewModelScope`) 来执行异步操作。
 */
class VersionsViewModel {

    // 可变的 UI 状态，用 `mutableStateOf` 创建，Compose 会观察它的变化并触发 UI 重组。
    // `by` 委托使得可以直接读写 `uiState` 变量。
    var uiState by mutableStateOf(VersionsScreenState()) // 初始状态在 data class 里定义了
        private set // 把 setter 设为私有，强制状态更新只能通过 ViewModel 的方法进行，保证单向数据流。

    // 创建一个与这个 ViewModel 生命周期绑定的协程作用域。
    // 使用 Dispatchers.Main 意味着在这个作用域启动的协程默认在主线程执行，适合直接更新 UI 状态。
    // Job() 允许我们后面取消这个作用域里的所有协程。
    // 注意：在更复杂的应用中 (比如 Android)，应该用 Jetpack ViewModel 的 `viewModelScope` 来自动处理生命周期。
    private val viewModelJob = Job() // 把 Job 实例存起来，方便在 onCleared 里取消
    // 移除了 viewModelScope 的默认 Dispatchers.Main，因为 suspend 函数会在调用者的上下文中运行
    // private val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    // 或者保留它用于不挂起的后台任务
    private val viewModelScope = CoroutineScope(viewModelJob)

    init {
        // ViewModel 实例创建时，立马开始加载版本数据。
        loadVersions() // loadVersions 内部应该自己处理线程切换
    }

    /**
     * @brief 异步加载版本信息。
     *        这个函数会:
     *        1.  启动一个后台协程。
     *        2.  更新 UI 状态为加载中，并清空之前的错误和选中信息。
     *        3.  并行或顺序地调用 `VersionScanner.scanLocalVersions()` 获取本地版本信息
     *            和 `MojangApiService.getVersionManifest()` 获取远程版本清单。
     *        4.  合并本地和远程信息，生成 `List<VersionInfoView>`。
     *        5.  按发布时间降序排版本列表。
     *        6.  在主线程上更新 `uiState`，显示版本列表或错误信息。
     */
    fun loadVersions() {
        // 在 viewModelScope 中启动一个新的协程来执行网络和磁盘 IO 操作
        viewModelScope.launch { // 这个协程默认运行在 viewModelScope 的上下文中
            try {
                // 开始加载，更新 UI 状态：确保在主线程上进行
                withContext(Dispatchers.Main) {
                    println("ViewModel: Starting to load version info (Main)...")
                    uiState = uiState.copy(
                        isLoading = true,
                        error = null,
                        isDetailsLoading = false, 
                        selectedVersionDetails = null, 
                        downloadingVersionId = null, 
                        downloadProgress = null
                    )
                }

                // --- 获取数据 (切换到 IO 线程) --- 
                val localVersions = withContext(Dispatchers.IO) {
                     println("ViewModel: Scanning local versions (IO)...")
                     VersionScanner.scanLocalVersions()
                }
                val manifest = withContext(Dispatchers.IO) {
                     println("ViewModel: Fetching remote version manifest (IO)...")
                     MojangApiService.getVersionManifest()
                }

                // 检查版本清单是不是获取成功了
                if (manifest == null) {
                    throw Exception("Failed to load version manifest from Mojang.")
                }

                // --- 数据处理 (可以在 Default 线程) ---
                val combinedVersions = withContext(Dispatchers.Default) { 
                    println("ViewModel: Merging and sorting version info (Default)...")
                    val localVersionIds = localVersions.map { it.id }.toSet()
                    manifest.versions.map { versionInfo ->
                        VersionInfoView(
                            id = versionInfo.id,
                            type = versionInfo.type,
                            releaseTime = versionInfo.releaseTime,
                            isInstalled = localVersionIds.contains(versionInfo.id),
                            manifestUrl = versionInfo.url
                        )
                    }.sortedByDescending { it.releaseTime }
                }
                println("ViewModel: Version info processing complete.")

                // 数据处理完成，切换回主线程更新最终 UI 状态
                withContext(Dispatchers.Main) {
                    println("ViewModel: Updating UI to display version list (Main)...")
                    uiState = uiState.copy(
                        isLoading = false, // 加载完成
                        versions = combinedVersions, // 设置新的版本列表
                        error = null // 清除错误状态
                    )
                }

            } catch (e: Exception) {
                // 捕获加载过程中发生的任何异常
                println("ViewModel: Error loading versions: ${e.message}")
                e.printStackTrace()
                // 切换回主线程更新 UI 状态以显示错误信息
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false, // 加载结束 (即使是失败)
                        error = "Error loading versions: ${e.message}",
                        isDetailsLoading = false,
                        selectedVersionDetails = null,
                        downloadingVersionId = null,
                        downloadProgress = null
                    )
                }
            }
        }
    }

    /**
     * @brief (这个函数当前没在 UI 里用，留着以后可能的功能用)
     *        异步获取指定版本的详细信息。
     *        当用户选择某个版本时，可以调用这个函数来加载更详细的数据。
     *
     * @param version 用户在列表里选择的版本对应的 `VersionInfoView` 对象。
     */
    fun fetchVersionDetails(version: VersionInfoView) {
        val url = version.manifestUrl // 获取详情 JSON 的 URL
        // 检查 URL 是不是有效的
        if (url == null) {
            // 理论上不应该发生，但做好防御性编程
            uiState = uiState.copy(error = "Details URL missing for version ${version.id}.")
            return
        }

        // 更新 UI 状态：开始加载详情，清除旧详情和错误
        // 注意：不应该清除正在进行的下载状态 (downloadingVersionId, downloadProgress)
        uiState = uiState.copy(
            isDetailsLoading = true,
            selectedVersionDetails = null,
            error = null
        )

        viewModelScope.launch { // 启动协程执行网络请求
            try {
                // 在 IO 线程获取版本详情
                val details = withContext(Dispatchers.IO) {
                    MojangApiService.getVersionDetails(url)
                }

                // 切换回主线程更新 UI
                withContext(Dispatchers.Main) {
                    if (details != null) {
                        // 获取成功，更新状态
                        uiState = uiState.copy(
                            isDetailsLoading = false, // 加载完成
                            selectedVersionDetails = details, // 设置获取到的详情
                            error = null
                        )
                    } else {
                        // MojangApiService 内部处理了错误并返回 null
                        uiState = uiState.copy(
                            isDetailsLoading = false, // 加载结束
                            selectedVersionDetails = null,
                            error = "Failed to load details for version ${version.id}."
                        )
                    }
                }
            } catch (e: Exception) {
                // 捕获获取或更新过程中的意外异常
                 withContext(Dispatchers.Main) {
                     uiState = uiState.copy(
                         isDetailsLoading = false, // 加载结束
                         selectedVersionDetails = null,
                         error = "Error fetching details for version ${version.id}: ${e.message}"
                     )
                     e.printStackTrace() // 打印错误详情
                 }
            }
        }
    }

    /**
     * @brief 启动指定版本的下载过程。
     *        这个函数现在是 suspend 函数。
     *        这个函数会:
     *        1.  检查是不是已经有其他下载在进行中。
     *        2.  更新 UI 状态为开始下载，设置 `downloadingVersionId` 和初始进度。
     *        3.  直接在调用者的协程上下文中执行。
     *        4.  获取版本的详细信息 (`VersionDetails`)。
     *        5.  调用 `DownloadManager.parseDownloadTasks` 解析需要下载的文件列表。
     *        6.  调用 `DownloadManager.executeDownloadTasks` 执行下载，并传入进度回调来更新 `uiState`。
     *        7.  下载完成后，更新 UI 状态 (清除下载状态，标记版本为已安装)。
     *        8.  处理过程中可能发生的错误。
     *
     * @param version 用户选择要下载的版本对应的 `VersionInfoView` 对象。
     */
    suspend fun downloadVersion(version: VersionInfoView) { // <--- 改为 suspend fun
        // 防止同时启动多个下载任务
        if (uiState.downloadingVersionId != null) {
            println("ViewModel: Download task already in progress (Version: ${uiState.downloadingVersionId}), cannot start new download for ${version.id}.")
            // 在 Main 线程更新 UI
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(error = "Another download is already in progress")
            }
            return // 阻止启动新下载
        }

        // --- 开始下载准备阶段 ---
        println("ViewModel: Starting preparation to download version ${version.id}...")
        // 更新 UI 状态 (需要在 Main 线程)
        withContext(Dispatchers.Main) {
            uiState = uiState.copy(
                downloadingVersionId = version.id,
                error = null,
                isDetailsLoading = false, // 重置详情加载状态
                selectedVersionDetails = null, // 清除详情显示
                downloadProgress = 0f // 初始化进度为 0%
            )
        }

        // 不再需要内部启动协程，直接在当前协程上下文中执行
        // 使用 withContext 切换线程执行耗时操作
        try {
            // --- 步骤 1: 获取版本详细信息 (IO) ---
            val url = version.manifestUrl
            if (url == null) {
                throw Exception("Details URL missing for version ${version.id}.")
            }
            // 先获取 VersionDetails，并检查是否为空
            val fetchedDetails = withContext(Dispatchers.IO) {
                println("ViewModel: Fetching details for version ${version.id} (IO)...") // 修正日志信息
                MojangApiService.getVersionDetails(url) // <<< 修正：调用 getVersionDetails
            }
            if (fetchedDetails == null) { // <<< 修正：检查 fetchedDetails
                throw Exception("Failed to fetch details for version ${version.id}, cannot determine download content.")
            }
            // 到这里，fetchedDetails 肯定不为空

            // --- 步骤 2: 解析下载任务 (Default) ---
            println("ViewModel: Parsing download tasks for ${fetchedDetails.id} (Default)...") // <<< 修正：使用 fetchedDetails
            // 使用获取到的 fetchedDetails 来解析任务列表
            val initialDownloadTasks = withContext(Dispatchers.Default) {
                DownloadManager.parseDownloadTasks(fetchedDetails) // <<< 修正：传入 fetchedDetails
            }
            if (initialDownloadTasks.isEmpty()) {
                // 如果初始任务列表为空 (可能只发生在版本json有问题时)，也应该处理
                throw Exception("Parsed download task list is empty for version ${fetchedDetails.id}.")
            }

            // --- 步骤 3: 执行下载任务 (IO + Default for progress) ---
            val standardGameDir = File(System.getProperty("user.home"), ".wzs_minecraft_launcher/minecraft")
            // 执行下载，传入解析好的 initialDownloadTasks
            val downloadSuccess = DownloadManager.executeDownloadTasks( // <<< 修正：直接调用，结果赋给新变量
                tasks = initialDownloadTasks, // <<< 修正：传入初始任务列表
                gameDir = standardGameDir,
                client = MojangApiService.getClient(),
                progressCallback = { progress ->
                    // 进度回调内部切换到 Main 线程更新 UI
                    viewModelScope.launch(Dispatchers.Main.immediate) { // 使用 immediate 尝试立即执行
                        // 检查一下，确保只有当下载还在进行时才更新进度 (避免下载结束后意外更新)
                        if (uiState.downloadingVersionId == version.id) {
                           uiState = uiState.copy(downloadProgress = progress)
                        }
                    }
                }
            )


            // --- 步骤 4: 处理下载结果 (Main) ---
            withContext(Dispatchers.Main) {
                if (downloadSuccess) { // <<< 修正：检查下载执行结果
                    println("ViewModel: All files for version ${version.id} downloaded successfully.")
                    // 更新版本列表中的状态
                    val updatedVersions = uiState.versions.map {
                        if (it.id == version.id) it.copy(isInstalled = true) else it
                    }
                    uiState = uiState.copy(
                        downloadingVersionId = null,
                        downloadProgress = null,
                        versions = updatedVersions,
                        error = null // 清除可能存在的旧错误
                    )
                } else {
                    println("ViewModel: Download failed for version ${version.id}.")
                    uiState = uiState.copy(
                        downloadingVersionId = null,
                        downloadProgress = null,
                        error = "Download failed for ${version.id}. Please check logs."
                    )
                }
            }

        } catch (e: Exception) {
            // 捕获下载准备或执行过程中的任何异常
            println("ViewModel: Error during download process for version ${version.id}: ${e.message}")
            e.printStackTrace() // 打印错误详情
            // 在 Main 线程更新 UI 状态以显示错误
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    downloadingVersionId = null, // 清除下载状态
                    downloadProgress = null, // 清除进度
                    error = "Download failed: ${e.message}" // <<< 修正：提供更具体的错误信息
                )
            }
        }
    }

    /**
     * @brief 为指定的已安装版本启动游戏。
     *        这个函数现在是 suspend 函数。
     *        这个函数会:
     *        1.  执行基本检查 (是不是已安装，是不是正在下载等)。
     *        2.  更新 UI 状态 (比如，清除错误信息，以后可以加个"正在启动"状态)。
     *        3.  启动后台协程。
     *        4.  获取版本的详细信息 (`VersionDetails`)。
     *        5.  准备启动参数 (游戏目录、用户名、内存等)。
     *        6.  调用 `GameLauncher.launchGame` 尝试启动游戏。
     *        7.  处理启动结果 (目前只打印日志，以后可以更新 UI)。
     *        8.  处理过程中可能发生的错误。
     *
     * @param version 用户选择要启动的版本对应的 `VersionInfoView` 对象。
     */
    suspend fun launchVersion(version: VersionInfoView) { // <--- 改为 suspend fun
        println("ViewModel: Requesting launch for version ${version.id}")

        // --- 基本条件检查 --- 
        // 检查版本是不是已标记为安装
        if (!version.isInstalled) {
            println("ViewModel: Launch cancelled - Version ${version.id} is not marked as installed.")
            // 需要在 Main 线程更新 UI
            withContext(Dispatchers.Main) {
                 uiState = uiState.copy(error = "Launch failed: Version ${version.id} is not installed")
            }
            return
        }
        // 检查这个版本当前是不是正在下载
        if (uiState.downloadingVersionId == version.id) {
             println("ViewModel: Launch cancelled - Version ${version.id} is currently downloading.")
             withContext(Dispatchers.Main) {
                 uiState = uiState.copy(error = "Launch failed: Version ${version.id} is downloading")
             }
             return
        }
        // 检查版本详情 URL 是否存在 (虽然不太可能没有，但做个检查)
        val url = version.manifestUrl
        if (url == null) {
             println("ViewModel: Launch cancelled - Manifest URL missing for version ${version.id}.")
             withContext(Dispatchers.Main) {
                 uiState = uiState.copy(error = "Launch failed: Manifest URL missing for version ${version.id}")
             }
             return
        }

        // TODO: isLaunching 状态处理 (需要放到 Main 线程)
        println("ViewModel: Starting preparation to launch version ${version.id}...")
        // 清除错误信息 (需要在 Main 线程)
        withContext(Dispatchers.Main) {
             uiState = uiState.copy(error = null)
        }

        // 不再需要内部启动协程，直接在当前协程 (由 UI 处的 scope.launch 启动) 的上下文中执行
        // 使用 withContext(Dispatchers.IO) 来执行 IO 密集型操作
        try {
            val details: VersionDetails? = withContext(Dispatchers.IO) {
                println("ViewModel: Fetching details for version ${version.id} (IO)...")
                MojangApiService.getVersionDetails(url)
            }

            if (details == null) {
                println("ViewModel: Launch failed - Could not fetch details for version ${version.id}.")
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(error = "Launch failed: Could not fetch details for version ${version.id}")
                }
                return // 退出 suspend 函数
            }
            println("ViewModel: Successfully fetched details for version ${version.id}.")

            // --- 步骤 2: 准备启动所需的参数 --- (这部分是 CPU 密集型或内存操作，可以在 IO 或 Default)
            val gameDir: File
            val nativesDir: File
            val username: String
            val jvmArgs: List<String>
            val maxMemoryMb: Int
            withContext(Dispatchers.Default) { // 使用 Default dispatcher 做准备工作
                // TODO: 这个路径应该从设置里读取，或者用一个更标准的、跨平台的方式获取
                gameDir = File(System.getProperty("user.home"), ".wzs_minecraft_launcher/minecraft") 
                nativesDir = File(gameDir, "natives/${details.id}") // natives 目录最好按版本隔离
                username = "Player${(100..999).random()}" // TODO: 后面要接入账户系统
                jvmArgs = emptyList() // TODO: 从设置读取或提供默认值
                maxMemoryMb = 2048 // TODO: 从设置读取
            }
            
            // 确保目录存在 (文件 IO)
            try {
                withContext(Dispatchers.IO) {
                    gameDir.mkdirs()
                    nativesDir.mkdirs()
                }
            } catch (e: SecurityException) {
                 println("ViewModel: Security exception while creating directories: ${e.message}")
                 withContext(Dispatchers.Main) {
                     uiState = uiState.copy(error = "Launch failed: Permission denied when creating directories")
                 }
                 return
            }

            println("ViewModel: Preparing to call GameLauncher for version ${version.id} (IO)...")
            // --- 步骤 3: 调用 GameLauncher (suspend 函数，执行 IO/进程操作) ---
            val launchSuccess = withContext(Dispatchers.IO) {
                 GameLauncher.launchGame(
                     versionDetails = details,
                     gameDir = gameDir,
                     nativesDir = nativesDir,
                     username = username,
                     jvmArgs = jvmArgs,
                     maxMemoryMb = maxMemoryMb
                 )
            }

            // --- 步骤 4: 处理结果 (需要在 Main 线程更新 UI) ---
            withContext(Dispatchers.Main) {
                if (launchSuccess) {
                    println("ViewModel: GameLauncher successfully started the process for version ${version.id}.")
                    // uiState = uiState.copy(error = null) // 可选：清除错误
                } else {
                    println("ViewModel: GameLauncher failed to start the process for version ${version.id}.")
                    uiState = uiState.copy(error = "Launch failed: Could not start game process (check console logs for details)")
                }
            }

        } catch (e: Exception) {
            // 捕获整个过程中的任何意外错误
            println("ViewModel: Unexpected error during launch process for version ${version.id}: ${e.message}")
            e.printStackTrace() // 打印错误详情
            // 在 Main 线程更新 UI
            withContext(Dispatchers.Main) {
                uiState = uiState.copy(
                    error = "Launch preparation failed: ${e.message}"
                )
            }
        }
    }

    /**
     * @brief 当 ViewModel 不再需要时调用这个方法，用于清理资源。
     *        比如，取消所有正在运行的协程。
     */
    fun onCleared() {
        println("VersionsViewModel: Cleaning up resources, cancelling coroutines...")
        // 取消与这个 ViewModel 关联的 Job，这会取消 viewModelScope 中所有正在运行的协程
        viewModelJob.cancel()
        // 注意：在 Android ViewModel 中，这通常由框架自动处理。
    }

} // class VersionsViewModel 结束 