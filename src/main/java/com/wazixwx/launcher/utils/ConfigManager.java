package com.wazixwx.launcher.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(ConfigManager.class);
    private static final String CONFIG_FILE = "config.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static JsonObject config;

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                String content = new String(Files.readAllBytes(configPath));
                config = gson.fromJson(content, JsonObject.class);
            } else {
                // 从资源文件加载默认配置
                try (InputStream is = ConfigManager.class.getResourceAsStream("/config.json")) {
                    if (is != null) {
                        String content = new String(is.readAllBytes());
                        config = gson.fromJson(content, JsonObject.class);
                        // 保存默认配置到文件
                        saveConfig();
                    } else {
                        logger.error("Default config file not found in resources");
                        config = new JsonObject();
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load config", e);
            config = new JsonObject();
        }
    }

    public static void saveConfig() {
        try {
            String json = gson.toJson(config);
            Files.write(Paths.get(CONFIG_FILE), json.getBytes());
        } catch (IOException e) {
            logger.error("Failed to save config", e);
        }
    }

    public static String getString(String key) {
        return getString(key, "");
    }

    public static String getString(String key, String defaultValue) {
        try {
            return config.get(key).getAsString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            return config.get(key).getAsInt();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            return config.get(key).getAsBoolean();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static void setString(String key, String value) {
        config.addProperty(key, value);
        saveConfig();
    }

    public static void setInt(String key, int value) {
        config.addProperty(key, value);
        saveConfig();
    }

    public static void setBoolean(String key, boolean value) {
        config.addProperty(key, value);
        saveConfig();
    }
} 