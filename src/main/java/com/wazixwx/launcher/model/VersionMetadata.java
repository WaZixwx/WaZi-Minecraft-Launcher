package com.wazixwx.launcher.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Minecraft版本元数据类
 * Minecraft Version Metadata Class
 * 
 * 该类表示Minecraft版本的元数据信息，包括版本ID、类型、发布时间等
 * This class represents the metadata information of a Minecraft version, including version ID, type, release time, etc.
 * 
 * @author WaZixwx
 * @version 1.0.0
 */
public class VersionMetadata {
    
    /**
     * 版本ID，如"1.19.2"
     * Version ID, e.g. "1.19.2"
     */
    private String id;
    
    /**
     * 版本类型，如"release"、"snapshot"、"old_beta"等
     * Version type, e.g. "release", "snapshot", "old_beta", etc.
     */
    private String type;
    
    /**
     * 版本URL，用于下载版本JSON文件
     * Version URL, used to download version JSON file
     */
    private String url;
    
    /**
     * 发布时间
     * Release time
     */
    private LocalDateTime releaseTime;
    
    /**
     * 版本描述
     * Version description
     */
    private String description;
    
    /**
     * 是否为推荐版本
     * Whether this version is recommended
     */
    private boolean recommended;
    
    /**
     * 是否为最新版本
     * Whether this version is the latest
     */
    private boolean latest;
    
    /**
     * 是否已安装
     * Whether this version is installed
     */
    private boolean installed;
    
    /**
     * 版本依赖项，如Java版本要求
     * Version dependencies, such as Java version requirements
     */
    private Map<String, String> dependencies;
    
    /**
     * 版本库列表
     * List of version libraries
     */
    private List<Library> libraries;
    
    /**
     * 主类
     * Main class
     */
    private String mainClass;
    
    /**
     * 游戏参数
     * Game arguments
     */
    private String arguments;
    
    /**
     * 最小内存要求（MB）
     * Minimum memory requirement (MB)
     */
    private int minMemory;
    
    /**
     * 推荐内存要求（MB）
     * Recommended memory requirement (MB)
     */
    private int recommendedMemory;
    
    /**
     * 版本库类
     * Version Library Class
     */
    public static class Library {
        
        /**
         * 库名称
         * Library name
         */
        private String name;
        
        /**
         * 库URL
         * Library URL
         */
        private String url;
        
        /**
         * 库路径
         * Library path
         */
        private String path;
        
        /**
         * 库SHA1值
         * Library SHA1 value
         */
        private String sha1;
        
        /**
         * 库规则
         * Library rules
         */
        private List<Rule> rules;
        
        /**
         * 库规则类
         * Library Rule Class
         */
        public static class Rule {
            
            /**
             * 规则动作，如"allow"或"disallow"
             * Rule action, e.g. "allow" or "disallow"
             */
            private String action;
            
            /**
             * 规则操作系统
             * Rule operating system
             */
            private String os;
            
            /**
             * 规则架构
             * Rule architecture
             */
            private String arch;
            
            // Getters and setters
            public String getAction() {
                return action;
            }
            
            public void setAction(String action) {
                this.action = action;
            }
            
            public String getOs() {
                return os;
            }
            
            public void setOs(String os) {
                this.os = os;
            }
            
            public String getArch() {
                return arch;
            }
            
            public void setArch(String arch) {
                this.arch = arch;
            }
        }
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public String getPath() {
            return path;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public String getSha1() {
            return sha1;
        }
        
        public void setSha1(String sha1) {
            this.sha1 = sha1;
        }
        
        public List<Rule> getRules() {
            return rules;
        }
        
        public void setRules(List<Rule> rules) {
            this.rules = rules;
        }
    }
    
    // Getters and setters
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
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public LocalDateTime getReleaseTime() {
        return releaseTime;
    }
    
    public void setReleaseTime(LocalDateTime releaseTime) {
        this.releaseTime = releaseTime;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public boolean isRecommended() {
        return recommended;
    }
    
    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }
    
    public boolean isLatest() {
        return latest;
    }
    
    public void setLatest(boolean latest) {
        this.latest = latest;
    }
    
    public boolean isInstalled() {
        return installed;
    }
    
    public void setInstalled(boolean installed) {
        this.installed = installed;
    }
    
    public Map<String, String> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(Map<String, String> dependencies) {
        this.dependencies = dependencies;
    }
    
    public List<Library> getLibraries() {
        return libraries;
    }
    
    public void setLibraries(List<Library> libraries) {
        this.libraries = libraries;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    
    public String getArguments() {
        return arguments;
    }
    
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }
    
    public int getMinMemory() {
        return minMemory;
    }
    
    public void setMinMemory(int minMemory) {
        this.minMemory = minMemory;
    }
    
    public int getRecommendedMemory() {
        return recommendedMemory;
    }
    
    public void setRecommendedMemory(int recommendedMemory) {
        this.recommendedMemory = recommendedMemory;
    }
} 