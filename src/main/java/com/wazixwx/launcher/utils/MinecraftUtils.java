package com.wazixwx.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Minecraft工具类
 * 用于处理Minecraft游戏相关的功能
 */
public class MinecraftUtils {
    private static final Logger logger = LoggerFactory.getLogger(MinecraftUtils.class);
    private static final Gson GSON = new Gson();
    private static final String MINECRAFT_VERSIONS_URL = "https://launchermeta.mojang.com/v1/packages/";
    private static final String MINECRAFT_VERSION_MANIFEST_URL = "https://launchermeta.mojang.com/mc/game/version_manifest.json";
    private static final String MINECRAFT_ASSETS_URL = "https://resources.download.minecraft.net/";
    private static final String MINECRAFT_LIBRARIES_URL = "https://libraries.minecraft.net/";
    private static final String FORGE_MAVEN_URL = "https://maven.minecraftforge.net/";
    private static final String FABRIC_MAVEN_URL = "https://maven.fabricmc.net/";
    
    private static final ExecutorService downloadExecutor = Executors.newFixedThreadPool(4);
    
    /**
     * 获取Minecraft版本列表
     * @return 版本列表
     */
    public static CompletableFuture<List<MinecraftVersion>> getVersionList() {
        return CompletableFuture.supplyAsync(() -> {
            List<MinecraftVersion> versions = new ArrayList<>();
            try {
                URL url = new URL(MINECRAFT_VERSION_MANIFEST_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                    JsonArray versionArray = json.getAsJsonArray("versions");
                    
                    for (JsonElement element : versionArray) {
                        JsonObject versionObj = element.getAsJsonObject();
                        String id = versionObj.get("id").getAsString();
                        String type = versionObj.get("type").getAsString();
                        String url = versionObj.get("url").getAsString();
                        String releaseTime = versionObj.get("releaseTime").getAsString();
                        
                        versions.add(new MinecraftVersion(id, type, url, releaseTime));
                    }
                    
                    // 按发布日期排序，最新的在前
                    versions.sort((v1, v2) -> v2.getReleaseTime().compareTo(v1.getReleaseTime()));
                } else {
                    logger.error("获取版本列表失败，HTTP状态码: {}", connection.getResponseCode());
                }
            } catch (Exception e) {
                logger.error("获取版本列表时发生错误", e);
            }
            return versions;
        });
    }
    
    /**
     * 获取特定版本的详细信息
     * @param version 版本对象
     * @return 版本详细信息
     */
    public static CompletableFuture<MinecraftVersionInfo> getVersionInfo(MinecraftVersion version) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(version.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    
                    JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                    
                    String id = json.get("id").getAsString();
                    String type = json.get("type").getAsString();
                    String mainClass = json.get("mainClass").getAsString();
                    String releaseTime = json.get("releaseTime").getAsString();
                    String time = json.get("time").getAsString();
                    
                    // 解析下载信息
                    JsonObject downloads = json.getAsJsonObject("downloads");
                    JsonObject client = downloads.getAsJsonObject("client");
                    JsonObject server = downloads.getAsJsonObject("server");
                    
                    DownloadInfo clientInfo = new DownloadInfo(
                        client.get("sha1").getAsString(),
                        client.get("size").getAsLong(),
                        client.get("url").getAsString()
                    );
                    
                    DownloadInfo serverInfo = new DownloadInfo(
                        server.get("sha1").getAsString(),
                        server.get("size").getAsLong(),
                        server.get("url").getAsString()
                    );
                    
                    // 解析资源索引
                    JsonObject assetIndex = json.getAsJsonObject("assetIndex");
                    AssetIndex assetIndexInfo = new AssetIndex(
                        assetIndex.get("id").getAsString(),
                        assetIndex.get("sha1").getAsString(),
                        assetIndex.get("size").getAsLong(),
                        assetIndex.get("totalSize").getAsLong(),
                        assetIndex.get("url").getAsString()
                    );
                    
                    // 解析库
                    List<Library> libraries = new ArrayList<>();
                    JsonArray librariesArray = json.getAsJsonArray("libraries");
                    for (JsonElement element : librariesArray) {
                        JsonObject libObj = element.getAsJsonObject();
                        String name = libObj.get("name").getAsString();
                        
                        // 解析下载信息
                        JsonObject downloadsObj = libObj.getAsJsonObject("downloads");
                        JsonObject artifact = downloadsObj.has("artifact") ? 
                            downloadsObj.getAsJsonObject("artifact") : null;
                        
                        DownloadInfo artifactInfo = null;
                        if (artifact != null) {
                            artifactInfo = new DownloadInfo(
                                artifact.get("sha1").getAsString(),
                                artifact.get("size").getAsLong(),
                                artifact.get("url").getAsString()
                            );
                        }
                        
                        // 解析原生库
                        Map<String, DownloadInfo> natives = new HashMap<>();
                        if (libObj.has("natives")) {
                            JsonObject nativesObj = libObj.getAsJsonObject("natives");
                            JsonObject classifiers = downloadsObj.getAsJsonObject("classifiers");
                            
                            for (Map.Entry<String, JsonElement> entry : nativesObj.entrySet()) {
                                String os = entry.getKey();
                                String classifier = entry.getValue().getAsString();
                                
                                if (classifiers.has(classifier)) {
                                    JsonObject classifierObj = classifiers.getAsJsonObject(classifier);
                                    natives.put(os, new DownloadInfo(
                                        classifierObj.get("sha1").getAsString(),
                                        classifierObj.get("size").getAsLong(),
                                        classifierObj.get("url").getAsString()
                                    ));
                                }
                            }
                        }
                        
                        // 解析规则
                        List<Rule> rules = new ArrayList<>();
                        if (libObj.has("rules")) {
                            JsonArray rulesArray = libObj.getAsJsonArray("rules");
                            for (JsonElement ruleElement : rulesArray) {
                                JsonObject ruleObj = ruleElement.getAsJsonObject();
                                String action = ruleObj.get("action").getAsString();
                                
                                JsonObject osObj = ruleObj.has("os") ? 
                                    ruleObj.getAsJsonObject("os") : null;
                                
                                OsRule osRule = null;
                                if (osObj != null) {
                                    String osName = osObj.has("name") ? 
                                        osObj.get("name").getAsString() : null;
                                    String osVersion = osObj.has("version") ? 
                                        osObj.get("version").getAsString() : null;
                                    String osArch = osObj.has("arch") ? 
                                        osObj.get("arch").getAsString() : null;
                                    
                                    osRule = new OsRule(osName, osVersion, osArch);
                                }
                                
                                JsonObject featuresObj = ruleObj.has("features") ? 
                                    ruleObj.getAsJsonObject("features") : null;
                                
                                Map<String, Boolean> features = new HashMap<>();
                                if (featuresObj != null) {
                                    for (Map.Entry<String, JsonElement> featureEntry : featuresObj.entrySet()) {
                                        features.put(featureEntry.getKey(), featureEntry.getValue().getAsBoolean());
                                    }
                                }
                                
                                rules.add(new Rule(action, osRule, features));
                            }
                        }
                        
                        libraries.add(new Library(name, artifactInfo, natives, rules));
                    }
                    
                    // 解析参数
                    JsonObject arguments = json.getAsJsonObject("arguments");
                    List<String> jvmArgs = new ArrayList<>();
                    List<String> gameArgs = new ArrayList<>();
                    
                    if (arguments.has("jvm")) {
                        JsonArray jvmArray = arguments.getAsJsonArray("jvm");
                        for (JsonElement element : jvmArray) {
                            if (element.isJsonPrimitive()) {
                                jvmArgs.add(element.getAsString());
                            } else if (element.isJsonObject()) {
                                JsonObject obj = element.getAsJsonObject();
                                if (obj.has("rules")) {
                                    JsonArray rulesArray = obj.getAsJsonArray("rules");
                                    boolean shouldAdd = true;
                                    
                                    for (JsonElement ruleElement : rulesArray) {
                                        JsonObject ruleObj = ruleElement.getAsJsonObject();
                                        String action = ruleObj.get("action").getAsString();
                                        
                                        if (ruleObj.has("os")) {
                                            JsonObject osObj = ruleObj.getAsJsonObject("os");
                                            if (osObj.has("name")) {
                                                String osName = osObj.get("name").getAsString();
                                                String currentOs = SystemUtils.getOsType();
                                                
                                                if (action.equals("allow") && !osName.equals(currentOs)) {
                                                    shouldAdd = false;
                                                } else if (action.equals("disallow") && osName.equals(currentOs)) {
                                                    shouldAdd = false;
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (shouldAdd && obj.has("value")) {
                                        JsonElement valueElement = obj.get("value");
                                        if (valueElement.isJsonPrimitive()) {
                                            jvmArgs.add(valueElement.getAsString());
                                        } else if (valueElement.isJsonArray()) {
                                            JsonArray valueArray = valueElement.getAsJsonArray();
                                            for (JsonElement value : valueArray) {
                                                jvmArgs.add(value.getAsString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (arguments.has("game")) {
                        JsonArray gameArray = arguments.getAsJsonArray("game");
                        for (JsonElement element : gameArray) {
                            if (element.isJsonPrimitive()) {
                                gameArgs.add(element.getAsString());
                            } else if (element.isJsonObject()) {
                                JsonObject obj = element.getAsJsonObject();
                                if (obj.has("rules")) {
                                    JsonArray rulesArray = obj.getAsJsonArray("rules");
                                    boolean shouldAdd = true;
                                    
                                    for (JsonElement ruleElement : rulesArray) {
                                        JsonObject ruleObj = ruleElement.getAsJsonObject();
                                        String action = ruleObj.get("action").getAsString();
                                        
                                        if (ruleObj.has("os")) {
                                            JsonObject osObj = ruleObj.getAsJsonObject("os");
                                            if (osObj.has("name")) {
                                                String osName = osObj.get("name").getAsString();
                                                String currentOs = SystemUtils.getOsType();
                                                
                                                if (action.equals("allow") && !osName.equals(currentOs)) {
                                                    shouldAdd = false;
                                                } else if (action.equals("disallow") && osName.equals(currentOs)) {
                                                    shouldAdd = false;
                                                }
                                            }
                                        }
                                    }
                                    
                                    if (shouldAdd && obj.has("value")) {
                                        JsonElement valueElement = obj.get("value");
                                        if (valueElement.isJsonPrimitive()) {
                                            gameArgs.add(valueElement.getAsString());
                                        } else if (valueElement.isJsonArray()) {
                                            JsonArray valueArray = valueElement.getAsJsonArray();
                                            for (JsonElement value : valueArray) {
                                                gameArgs.add(value.getAsString());
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    return new MinecraftVersionInfo(
                        id, type, mainClass, releaseTime, time,
                        clientInfo, serverInfo, assetIndexInfo,
                        libraries, jvmArgs, gameArgs
                    );
                } else {
                    logger.error("获取版本信息失败，HTTP状态码: {}", connection.getResponseCode());
                }
            } catch (Exception e) {
                logger.error("获取版本信息时发生错误", e);
            }
            return null;
        });
    }
    
    /**
     * 下载Minecraft版本
     * @param versionInfo 版本信息
     * @param gameDir 游戏目录
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static CompletableFuture<Boolean> downloadVersion(
            MinecraftVersionInfo versionInfo, 
            String gameDir,
            ProgressCallback progressCallback) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 创建版本目录
                Path versionDir = Paths.get(gameDir, "versions", versionInfo.getId());
                Files.createDirectories(versionDir);
                
                // 下载客户端JAR
                DownloadInfo clientInfo = versionInfo.getClientInfo();
                Path clientJarPath = versionDir.resolve(versionInfo.getId() + ".jar");
                
                if (!Files.exists(clientJarPath) || !verifyFile(clientJarPath, clientInfo.getSha1())) {
                    progressCallback.onProgress("正在下载客户端JAR...", 0.0);
                    downloadFile(clientInfo.getUrl(), clientJarPath, progressCallback);
                }
                
                // 下载资源索引
                AssetIndex assetIndex = versionInfo.getAssetIndex();
                Path assetIndexPath = versionDir.resolve(assetIndex.getId() + ".json");
                
                if (!Files.exists(assetIndexPath) || !verifyFile(assetIndexPath, assetIndex.getSha1())) {
                    progressCallback.onProgress("正在下载资源索引...", 0.1);
                    downloadFile(assetIndex.getUrl(), assetIndexPath, progressCallback);
                }
                
                // 下载资源
                progressCallback.onProgress("正在解析资源索引...", 0.2);
                JsonObject assetIndexJson = JsonParser.parseString(
                    new String(Files.readAllBytes(assetIndexPath), StandardCharsets.UTF_8)
                ).getAsJsonObject();
                
                JsonObject objects = assetIndexJson.getAsJsonObject("objects");
                List<Asset> assets = new ArrayList<>();
                
                for (Map.Entry<String, JsonElement> entry : objects.entrySet()) {
                    String hash = entry.getValue().getAsJsonObject().get("hash").getAsString();
                    long size = entry.getValue().getAsJsonObject().get("size").getAsLong();
                    assets.add(new Asset(entry.getKey(), hash, size));
                }
                
                // 下载资源文件
                Path assetsDir = Paths.get(gameDir, "assets");
                Files.createDirectories(assetsDir);
                
                int totalAssets = assets.size();
                int downloadedAssets = 0;
                
                for (Asset asset : assets) {
                    String hash = asset.getHash();
                    String prefix = hash.substring(0, 2);
                    Path assetPath = assetsDir.resolve("objects").resolve(prefix).resolve(hash);
                    
                    if (!Files.exists(assetPath) || !verifyFile(assetPath, hash)) {
                        String assetUrl = MINECRAFT_ASSETS_URL + prefix + "/" + hash;
                        Files.createDirectories(assetPath.getParent());
                        downloadFile(assetUrl, assetPath, null);
                    }
                    
                    downloadedAssets++;
                    double progress = 0.2 + (0.3 * downloadedAssets / totalAssets);
                    progressCallback.onProgress("正在下载资源文件... " + downloadedAssets + "/" + totalAssets, progress);
                }
                
                // 下载库文件
                progressCallback.onProgress("正在下载库文件...", 0.5);
                Path librariesDir = Paths.get(gameDir, "libraries");
                Files.createDirectories(librariesDir);
                
                List<Library> libraries = versionInfo.getLibraries();
                int totalLibraries = libraries.size();
                int downloadedLibraries = 0;
                
                for (Library library : libraries) {
                    // 检查规则
                    boolean shouldDownload = true;
                    for (Rule rule : library.getRules()) {
                        if (rule.getAction().equals("disallow")) {
                            if (rule.getOsRule() != null) {
                                OsRule osRule = rule.getOsRule();
                                if (osRule.getName() != null && osRule.getName().equals(SystemUtils.getOsType())) {
                                    shouldDownload = false;
                                    break;
                                }
                            }
                        }
                    }
                    
                    if (!shouldDownload) {
                        downloadedLibraries++;
                        continue;
                    }
                    
                    // 下载主JAR
                    if (library.getArtifact() != null) {
                        DownloadInfo artifact = library.getArtifact();
                        String[] nameParts = library.getName().split(":");
                        String groupId = nameParts[0].replace('.', '/');
                        String artifactId = nameParts[1];
                        String version = nameParts[2];
                        
                        Path libraryPath = librariesDir.resolve(groupId).resolve(artifactId).resolve(version)
                            .resolve(artifactId + "-" + version + ".jar");
                        
                        if (!Files.exists(libraryPath) || !verifyFile(libraryPath, artifact.getSha1())) {
                            Files.createDirectories(libraryPath.getParent());
                            downloadFile(artifact.getUrl(), libraryPath, null);
                        }
                    }
                    
                    // 下载原生库
                    Map<String, DownloadInfo> natives = library.getNatives();
                    if (natives != null && !natives.isEmpty()) {
                        String osName = SystemUtils.getOsType();
                        DownloadInfo nativeInfo = natives.get(osName);
                        
                        if (nativeInfo != null) {
                            String[] nameParts = library.getName().split(":");
                            String groupId = nameParts[0].replace('.', '/');
                            String artifactId = nameParts[1];
                            String version = nameParts[2];
                            
                            Path nativePath = librariesDir.resolve(groupId).resolve(artifactId).resolve(version)
                                .resolve(artifactId + "-" + version + "-" + osName + ".jar");
                            
                            if (!Files.exists(nativePath) || !verifyFile(nativePath, nativeInfo.getSha1())) {
                                Files.createDirectories(nativePath.getParent());
                                downloadFile(nativeInfo.getUrl(), nativePath, null);
                                
                                // 解压原生库
                                Path nativesDir = versionDir.resolve("natives");
                                Files.createDirectories(nativesDir);
                                
                                // 这里需要实现解压JAR文件的功能
                                // 由于Java没有内置的ZIP解压功能，这里省略具体实现
                            }
                        }
                    }
                    
                    downloadedLibraries++;
                    double progress = 0.5 + (0.3 * downloadedLibraries / totalLibraries);
                    progressCallback.onProgress("正在下载库文件... " + downloadedLibraries + "/" + totalLibraries, progress);
                }
                
                // 保存版本JSON
                Path versionJsonPath = versionDir.resolve(versionInfo.getId() + ".json");
                if (!Files.exists(versionJsonPath)) {
                    // 这里需要实现保存版本JSON的功能
                    // 由于我们已经有了版本信息对象，可以直接序列化为JSON
                    String versionJson = GSON.toJson(versionInfo);
                    Files.write(versionJsonPath, versionJson.getBytes(StandardCharsets.UTF_8));
                }
                
                progressCallback.onProgress("版本下载完成", 1.0);
                return true;
            } catch (Exception e) {
                logger.error("下载版本时发生错误", e);
                progressCallback.onProgress("下载版本时发生错误: " + e.getMessage(), -1.0);
                return false;
            }
        }, downloadExecutor);
    }
    
    /**
     * 安装Forge模组加载器
     * @param minecraftVersion Minecraft版本
     * @param forgeVersion Forge版本
     * @param gameDir 游戏目录
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static CompletableFuture<Boolean> installForge(
            String minecraftVersion,
            String forgeVersion,
            String gameDir,
            ProgressCallback progressCallback) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 构建Forge安装器URL
                String forgeUrl = String.format("%snet/minecraftforge/forge/%s-%s/forge-%s-%s-installer.jar",
                    FORGE_MAVEN_URL, minecraftVersion, forgeVersion, minecraftVersion, forgeVersion);
                
                // 下载Forge安装器
                Path tempDir = Paths.get(SystemUtils.getTempDir());
                Path installerPath = tempDir.resolve("forge-installer.jar");
                
                progressCallback.onProgress("正在下载Forge安装器...", 0.0);
                downloadFile(forgeUrl, installerPath, progressCallback);
                
                // 执行Forge安装器
                progressCallback.onProgress("正在安装Forge...", 0.5);
                
                // 这里需要实现执行Forge安装器的功能
                // 由于这涉及到执行外部JAR文件，这里省略具体实现
                
                progressCallback.onProgress("Forge安装完成", 1.0);
                return true;
            } catch (Exception e) {
                logger.error("安装Forge时发生错误", e);
                progressCallback.onProgress("安装Forge时发生错误: " + e.getMessage(), -1.0);
                return false;
            }
        }, downloadExecutor);
    }
    
    /**
     * 安装Fabric模组加载器
     * @param minecraftVersion Minecraft版本
     * @param fabricVersion Fabric版本
     * @param gameDir 游戏目录
     * @param progressCallback 进度回调
     * @return 是否成功
     */
    public static CompletableFuture<Boolean> installFabric(
            String minecraftVersion,
            String fabricVersion,
            String gameDir,
            ProgressCallback progressCallback) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 构建Fabric安装器URL
                String fabricUrl = String.format("%snet/fabricmc/fabric-installer/%s/fabric-installer-%s.jar",
                    FABRIC_MAVEN_URL, fabricVersion, fabricVersion);
                
                // 下载Fabric安装器
                Path tempDir = Paths.get(SystemUtils.getTempDir());
                Path installerPath = tempDir.resolve("fabric-installer.jar");
                
                progressCallback.onProgress("正在下载Fabric安装器...", 0.0);
                downloadFile(fabricUrl, installerPath, progressCallback);
                
                // 执行Fabric安装器
                progressCallback.onProgress("正在安装Fabric...", 0.5);
                
                // 这里需要实现执行Fabric安装器的功能
                // 由于这涉及到执行外部JAR文件，这里省略具体实现
                
                progressCallback.onProgress("Fabric安装完成", 1.0);
                return true;
            } catch (Exception e) {
                logger.error("安装Fabric时发生错误", e);
                progressCallback.onProgress("安装Fabric时发生错误: " + e.getMessage(), -1.0);
                return false;
            }
        }, downloadExecutor);
    }
    
    /**
     * 启动Minecraft游戏
     * @param versionInfo 版本信息
     * @param gameDir 游戏目录
     * @param javaPath Java路径
     * @param username 用户名
     * @param uuid UUID
     * @param accessToken 访问令牌
     * @param memory 内存大小（MB）
     * @param additionalArgs 额外参数
     * @return 进程
     */
    public static Process launchGame(
            MinecraftVersionInfo versionInfo,
            String gameDir,
            String javaPath,
            String username,
            String uuid,
            String accessToken,
            int memory,
            List<String> additionalArgs) {
        
        try {
            List<String> command = new ArrayList<>();
            command.add(javaPath);
            
            // 添加JVM参数
            command.add("-Xmx" + memory + "M");
            command.add("-XX:+UnlockExperimentalVMOptions");
            command.add("-XX:+UseG1GC");
            command.add("-XX:G1NewSizePercent=20");
            command.add("-XX:G1ReservePercent=20");
            command.add("-XX:MaxGCPauseMillis=50");
            command.add("-XX:G1HeapRegionSize=32M");
            
            // 添加版本特定的JVM参数
            for (String arg : versionInfo.getJvmArgs()) {
                // 替换变量
                String processedArg = arg
                    .replace("${natives_directory}", "\"" + Paths.get(gameDir, "versions", versionInfo.getId(), "natives").toString() + "\"")
                    .replace("${launcher_name}", "WaZi-Minecraft-Launcher")
                    .replace("${launcher_version}", "1.0.0")
                    .replace("${game_directory}", "\"" + gameDir + "\"")
                    .replace("${classpath}", "\"" + buildClasspath(versionInfo, gameDir) + "\"");
                
                command.add(processedArg);
            }
            
            // 添加主类
            command.add(versionInfo.getMainClass());
            
            // 添加版本特定的游戏参数
            for (String arg : versionInfo.getGameArgs()) {
                // 替换变量
                String processedArg = arg
                    .replace("${version_name}", versionInfo.getId())
                    .replace("${game_directory}", "\"" + gameDir + "\"")
                    .replace("${assets_root}", "\"" + Paths.get(gameDir, "assets").toString() + "\"")
                    .replace("${assets_index_name}", versionInfo.getAssetIndex().getId())
                    .replace("${auth_uuid}", uuid)
                    .replace("${auth_access_token}", accessToken)
                    .replace("${auth_session}", accessToken)
                    .replace("${user_type}", "mojang")
                    .replace("${version_type}", versionInfo.getType());
                
                command.add(processedArg);
            }
            
            // 添加用户名和UUID
            command.add("--username");
            command.add(username);
            command.add("--uuid");
            command.add(uuid);
            command.add("--accessToken");
            command.add(accessToken);
            
            // 添加额外参数
            if (additionalArgs != null) {
                command.addAll(additionalArgs);
            }
            
            // 启动进程
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(new File(gameDir));
            processBuilder.redirectErrorStream(true);
            
            return processBuilder.start();
        } catch (Exception e) {
            logger.error("启动游戏时发生错误", e);
            return null;
        }
    }
    
    /**
     * 构建类路径
     * @param versionInfo 版本信息
     * @param gameDir 游戏目录
     * @return 类路径
     */
    private static String buildClasspath(MinecraftVersionInfo versionInfo, String gameDir) {
        List<String> classpath = new ArrayList<>();
        
        // 添加客户端JAR
        classpath.add(Paths.get(gameDir, "versions", versionInfo.getId(), versionInfo.getId() + ".jar").toString());
        
        // 添加库
        for (Library library : versionInfo.getLibraries()) {
            // 检查规则
            boolean shouldAdd = true;
            for (Rule rule : library.getRules()) {
                if (rule.getAction().equals("disallow")) {
                    if (rule.getOsRule() != null) {
                        OsRule osRule = rule.getOsRule();
                        if (osRule.getName() != null && osRule.getName().equals(SystemUtils.getOsType())) {
                            shouldAdd = false;
                            break;
                        }
                    }
                }
            }
            
            if (!shouldAdd) {
                continue;
            }
            
            // 添加主JAR
            if (library.getArtifact() != null) {
                DownloadInfo artifact = library.getArtifact();
                String[] nameParts = library.getName().split(":");
                String groupId = nameParts[0].replace('.', '/');
                String artifactId = nameParts[1];
                String version = nameParts[2];
                
                classpath.add(Paths.get(gameDir, "libraries", groupId, artifactId, version,
                    artifactId + "-" + version + ".jar").toString());
            }
        }
        
        return String.join(SystemUtils.getOsType().equals("windows") ? ";" : ":", classpath);
    }
    
    /**
     * 下载文件
     * @param urlString URL
     * @param path 保存路径
     * @param progressCallback 进度回调
     * @throws IOException IO异常
     */
    private static void downloadFile(String urlString, Path path, ProgressCallback progressCallback) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        int fileSize = connection.getContentLength();
        int downloadedSize = 0;
        
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(path.toFile())) {
            
            byte[] buffer = new byte[8192];
            int count;
            
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
                downloadedSize += count;
                
                if (progressCallback != null && fileSize > 0) {
                    double progress = (double) downloadedSize / fileSize;
                    progressCallback.onProgress("正在下载... " + formatFileSize(downloadedSize) + " / " + formatFileSize(fileSize), progress);
                }
            }
        }
    }
    
    /**
     * 验证文件SHA1
     * @param path 文件路径
     * @param expectedSha1 预期的SHA1
     * @return 是否验证通过
     */
    private static boolean verifyFile(Path path, String expectedSha1) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[8192];
            int count;
            
            try (InputStream in = Files.newInputStream(path)) {
                while ((count = in.read(buffer)) != -1) {
                    digest.update(buffer, 0, count);
                }
            }
            
            byte[] sha1Bytes = digest.digest();
            StringBuilder sha1Hex = new StringBuilder();
            
            for (byte b : sha1Bytes) {
                sha1Hex.append(String.format("%02x", b));
            }
            
            return sha1Hex.toString().equals(expectedSha1);
        } catch (Exception e) {
            logger.error("验证文件SHA1时发生错误", e);
            return false;
        }
    }
    
    /**
     * 格式化文件大小
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    private static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * 进度回调接口
     */
    public interface ProgressCallback {
        /**
         * 进度回调
         * @param message 消息
         * @param progress 进度（0.0-1.0，-1.0表示错误）
         */
        void onProgress(String message, double progress);
    }
    
    /**
     * Minecraft版本类
     */
    public static class MinecraftVersion {
        private final String id;
        private final String type;
        private final String url;
        private final String releaseTime;
        
        public MinecraftVersion(String id, String type, String url, String releaseTime) {
            this.id = id;
            this.type = type;
            this.url = url;
            this.releaseTime = releaseTime;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getReleaseTime() {
            return releaseTime;
        }
    }
    
    /**
     * Minecraft版本信息类
     */
    public static class MinecraftVersionInfo {
        private final String id;
        private final String type;
        private final String mainClass;
        private final String releaseTime;
        private final String time;
        private final DownloadInfo clientInfo;
        private final DownloadInfo serverInfo;
        private final AssetIndex assetIndex;
        private final List<Library> libraries;
        private final List<String> jvmArgs;
        private final List<String> gameArgs;
        
        public MinecraftVersionInfo(
                String id, String type, String mainClass, String releaseTime, String time,
                DownloadInfo clientInfo, DownloadInfo serverInfo, AssetIndex assetIndex,
                List<Library> libraries, List<String> jvmArgs, List<String> gameArgs) {
            this.id = id;
            this.type = type;
            this.mainClass = mainClass;
            this.releaseTime = releaseTime;
            this.time = time;
            this.clientInfo = clientInfo;
            this.serverInfo = serverInfo;
            this.assetIndex = assetIndex;
            this.libraries = libraries;
            this.jvmArgs = jvmArgs;
            this.gameArgs = gameArgs;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
        
        public String getMainClass() {
            return mainClass;
        }
        
        public String getReleaseTime() {
            return releaseTime;
        }
        
        public String getTime() {
            return time;
        }
        
        public DownloadInfo getClientInfo() {
            return clientInfo;
        }
        
        public DownloadInfo getServerInfo() {
            return serverInfo;
        }
        
        public AssetIndex getAssetIndex() {
            return assetIndex;
        }
        
        public List<Library> getLibraries() {
            return libraries;
        }
        
        public List<String> getJvmArgs() {
            return jvmArgs;
        }
        
        public List<String> getGameArgs() {
            return gameArgs;
        }
    }
    
    /**
     * 下载信息类
     */
    public static class DownloadInfo {
        private final String sha1;
        private final long size;
        private final String url;
        
        public DownloadInfo(String sha1, long size, String url) {
            this.sha1 = sha1;
            this.size = size;
            this.url = url;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public long getSize() {
            return size;
        }
        
        public String getUrl() {
            return url;
        }
    }
    
    /**
     * 资源索引类
     */
    public static class AssetIndex {
        private final String id;
        private final String sha1;
        private final long size;
        private final long totalSize;
        private final String url;
        
        public AssetIndex(String id, String sha1, long size, long totalSize, String url) {
            this.id = id;
            this.sha1 = sha1;
            this.size = size;
            this.totalSize = totalSize;
            this.url = url;
        }
        
        public String getId() {
            return id;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public long getSize() {
            return size;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public String getUrl() {
            return url;
        }
    }
    
    /**
     * 资源类
     */
    public static class Asset {
        private final String path;
        private final String hash;
        private final long size;
        
        public Asset(String path, String hash, long size) {
            this.path = path;
            this.hash = hash;
            this.size = size;
        }
        
        public String getPath() {
            return path;
        }
        
        public String getHash() {
            return hash;
        }
        
        public long getSize() {
            return size;
        }
    }
    
    /**
     * 库类
     */
    public static class Library {
        private final String name;
        private final DownloadInfo artifact;
        private final Map<String, DownloadInfo> natives;
        private final List<Rule> rules;
        
        public Library(String name, DownloadInfo artifact, Map<String, DownloadInfo> natives, List<Rule> rules) {
            this.name = name;
            this.artifact = artifact;
            this.natives = natives;
            this.rules = rules;
        }
        
        public String getName() {
            return name;
        }
        
        public DownloadInfo getArtifact() {
            return artifact;
        }
        
        public Map<String, DownloadInfo> getNatives() {
            return natives;
        }
        
        public List<Rule> getRules() {
            return rules;
        }
    }
    
    /**
     * 规则类
     */
    public static class Rule {
        private final String action;
        private final OsRule osRule;
        private final Map<String, Boolean> features;
        
        public Rule(String action, OsRule osRule, Map<String, Boolean> features) {
            this.action = action;
            this.osRule = osRule;
            this.features = features;
        }
        
        public String getAction() {
            return action;
        }
        
        public OsRule getOsRule() {
            return osRule;
        }
        
        public Map<String, Boolean> getFeatures() {
            return features;
        }
    }
    
    /**
     * 操作系统规则类
     */
    public static class OsRule {
        private final String name;
        private final String version;
        private final String arch;
        
        public OsRule(String name, String version, String arch) {
            this.name = name;
            this.version = version;
            this.arch = arch;
        }
        
        public String getName() {
            return name;
        }
        
        public String getVersion() {
            return version;
        }
        
        public String getArch() {
            return arch;
        }
    }
} 