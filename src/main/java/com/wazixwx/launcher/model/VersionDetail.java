package com.wazixwx.launcher.model;

import java.util.List;
import java.util.Map;

/**
 * Minecraft版本详细信息类
 * Minecraft Version Detail Class
 * 
 * 该类存储完整的Minecraft版本详细信息，包括参数、依赖库等
 * This class stores the complete details of a Minecraft version, including arguments, libraries, etc.
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class VersionDetail {
    
    /**
     * 版本ID
     * Version ID
     */
    private String id;
    
    /**
     * 版本类型
     * Version type
     */
    private String type;
    
    /**
     * 主类
     * Main class
     */
    private String mainClass;
    
    /**
     * 资源索引
     * Asset index
     */
    private AssetIndex assetIndex;
    
    /**
     * 资源版本
     * Asset version
     */
    private String assets;
    
    /**
     * 符合级别
     * Compliance level
     */
    private int complianceLevel;
    
    /**
     * 下载信息
     * Download information
     */
    private Map<String, DownloadInfo> downloads;
    
    /**
     * Java版本要求
     * Java version requirement
     */
    private JavaVersion javaVersion;
    
    /**
     * 依赖库列表
     * Library list
     */
    private List<Library> libraries;
    
    /**
     * 游戏参数
     * Game arguments
     */
    private Arguments arguments;
    
    /**
     * 资源索引类
     * Asset Index Class
     */
    public static class AssetIndex {
        private String id;
        private String sha1;
        private long size;
        private long totalSize;
        private String url;
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public long getTotalSize() {
            return totalSize;
        }
        
        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    /**
     * 下载信息类
     * Download Information Class
     */
    public static class DownloadInfo {
        private String sha1;
        private long size;
        private String url;
        
        public String getSha1() {
            return sha1;
        }
        
        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
        
        public long getSize() {
            return size;
        }
        
        public void setSize(long size) {
            this.size = size;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    /**
     * Java版本要求类
     * Java Version Requirement Class
     */
    public static class JavaVersion {
        private String component;
        private int majorVersion;
        
        public String getComponent() {
            return component;
        }
        
        public void setComponent(String component) {
            this.component = component;
        }
        
        public int getMajorVersion() {
            return majorVersion;
        }
        
        public void setMajorVersion(int majorVersion) {
            this.majorVersion = majorVersion;
        }
    }
    
    /**
     * 依赖库类
     * Library Class
     */
    public static class Library {
        private String name;
        private Map<String, DownloadInfo> downloads;
        private List<Rule> rules;
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public Map<String, DownloadInfo> getDownloads() {
            return downloads;
        }
        
        public void setDownloads(Map<String, DownloadInfo> downloads) {
            this.downloads = downloads;
        }
        
        public List<Rule> getRules() {
            return rules;
        }
        
        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }
    
    /**
     * 规则类
     * Rule Class
     */
    public static class Rule {
        private String action;
        private Map<String, String> os;
        private Map<String, Boolean> features;
        
        public String getAction() {
            return action;
        }
        
        public void setAction(String action) {
            this.action = action;
        }
        
        public Map<String, String> getOs() {
            return os;
        }
        
        public void setOs(Map<String, String> os) {
            this.os = os;
        }
        
        public Map<String, Boolean> getFeatures() {
            return features;
        }
        
        public void setFeatures(Map<String, Boolean> features) {
            this.features = features;
        }
    }
    
    /**
     * 参数类
     * Arguments Class
     */
    public static class Arguments {
        private List<Object> game;
        private List<Object> jvm;
        
        public List<Object> getGame() {
            return game;
        }
        
        public void setGame(List<Object> game) {
            this.game = game;
        }
        
        public List<Object> getJvm() {
            return jvm;
        }
        
        public void setJvm(List<Object> jvm) {
            this.jvm = jvm;
        }
    }
    
    // Getters and setters for the main class
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    
    public AssetIndex getAssetIndex() {
        return assetIndex;
    }
    
    public void setAssetIndex(AssetIndex assetIndex) {
        this.assetIndex = assetIndex;
    }
    
    public String getAssets() {
        return assets;
    }
    
    public void setAssets(String assets) {
        this.assets = assets;
    }
    
    public int getComplianceLevel() {
        return complianceLevel;
    }
    
    public void setComplianceLevel(int complianceLevel) {
        this.complianceLevel = complianceLevel;
    }
    
    public Map<String, DownloadInfo> getDownloads() {
        return downloads;
    }
    
    public void setDownloads(Map<String, DownloadInfo> downloads) {
        this.downloads = downloads;
    }
    
    public JavaVersion getJavaVersion() {
        return javaVersion;
    }
    
    public void setJavaVersion(JavaVersion javaVersion) {
        this.javaVersion = javaVersion;
    }
    
    public List<Library> getLibraries() {
        return libraries;
    }
    
    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }
    
    public Arguments getArguments() {
        return arguments;
    }
    
    public void setArguments(Arguments arguments) {
        this.arguments = arguments;
    }
} 