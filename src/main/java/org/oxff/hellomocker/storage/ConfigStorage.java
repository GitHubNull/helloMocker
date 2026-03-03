package org.oxff.hellomocker.storage;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.persistence.Persistence;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置存储
 * 管理插件的全局配置
 *
 * @author oxff
 * @version 1.0
 */
@Getter
@Setter
public class ConfigStorage {

    private static final String CONFIG_PREFIX = "hellomocker.config.";
    private static final String PYTHON_PATH_KEY = CONFIG_PREFIX + "pythonPath";
    private static final String DEFAULT_TIMEOUT_KEY = CONFIG_PREFIX + "defaultTimeout";
    private static final String MAX_RULES_KEY = CONFIG_PREFIX + "maxRules";
    private static final String ENABLE_LOGGING_KEY = CONFIG_PREFIX + "enableLogging";

    private final MontoyaApi api;
    private final Persistence persistence;

    // 配置项
    private String pythonPath;
    private int defaultTimeout;
    private int maxRules;
    private boolean enableLogging;

    public ConfigStorage(MontoyaApi api) {
        this.api = api;
        this.persistence = api.persistence();
        
        // 设置默认值
        this.pythonPath = "python3";
        this.defaultTimeout = 30000; // 30秒
        this.maxRules = 1000;
        this.enableLogging = true;
    }

    /**
     * 加载配置
     */
    public void loadConfig() {
        try {
            String savedPythonPath = persistence.extensionData().getString(PYTHON_PATH_KEY);
            if (savedPythonPath != null && !savedPythonPath.isEmpty()) {
                this.pythonPath = savedPythonPath;
            }

            String savedTimeout = persistence.extensionData().getString(DEFAULT_TIMEOUT_KEY);
            if (savedTimeout != null && !savedTimeout.isEmpty()) {
                try {
                    this.defaultTimeout = Integer.parseInt(savedTimeout);
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }

            String savedMaxRules = persistence.extensionData().getString(MAX_RULES_KEY);
            if (savedMaxRules != null && !savedMaxRules.isEmpty()) {
                try {
                    this.maxRules = Integer.parseInt(savedMaxRules);
                } catch (NumberFormatException e) {
                    // 使用默认值
                }
            }

            String savedLogging = persistence.extensionData().getString(ENABLE_LOGGING_KEY);
            if (savedLogging != null && !savedLogging.isEmpty()) {
                this.enableLogging = Boolean.parseBoolean(savedLogging);
            }

            log("Configuration loaded successfully");
        } catch (Exception e) {
            logError("Failed to load configuration", e);
        }
    }

    /**
     * 保存配置
     */
    public void saveConfig() {
        try {
            persistence.extensionData().setString(PYTHON_PATH_KEY, pythonPath);
            persistence.extensionData().setString(DEFAULT_TIMEOUT_KEY, String.valueOf(defaultTimeout));
            persistence.extensionData().setString(MAX_RULES_KEY, String.valueOf(maxRules));
            persistence.extensionData().setString(ENABLE_LOGGING_KEY, String.valueOf(enableLogging));

            log("Configuration saved successfully");
        } catch (Exception e) {
            logError("Failed to save configuration", e);
        }
    }

    /**
     * 获取Python路径（如果未设置，尝试自动检测）
     */
    public String getPythonPath() {
        if (pythonPath == null || pythonPath.trim().isEmpty()) {
            // 尝试自动检测
            return detectPythonPath();
        }
        return pythonPath;
    }

    /**
     * 检测Python路径
     */
    private String detectPythonPath() {
        String[] candidates = {"python3", "python", "py"};
        
        for (String candidate : candidates) {
            try {
                Process process = new ProcessBuilder(candidate, "--version")
                        .redirectErrorStream(true)
                        .start();
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    return candidate;
                }
            } catch (Exception e) {
                // 尝试下一个
            }
        }
        
        return "python3"; // 默认返回值
    }

    /**
     * 设置Python路径
     */
    public void setPythonPath(String pythonPath) {
        this.pythonPath = pythonPath;
        saveConfig();
    }

    /**
     * 验证Python路径是否有效
     */
    public boolean isPythonPathValid() {
        String path = getPythonPath();
        if (path == null || path.trim().isEmpty()) {
            return false;
        }

        try {
            Process process = new ProcessBuilder(path, "--version")
                    .redirectErrorStream(true)
                    .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 日志记录
     */
    private void log(String message) {
        if (enableLogging && api != null) {
            api.logging().logToOutput("[ConfigStorage] " + message);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, Throwable e) {
        if (api != null) {
            api.logging().logToError("[ConfigStorage] " + message, e);
        }
    }
}
