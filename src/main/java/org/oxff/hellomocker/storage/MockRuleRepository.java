package org.oxff.hellomocker.storage;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Mock规则仓库
 * 负责规则的持久化存储和加载
 *
 * @author oxff
 * @version 1.0
 */
public class MockRuleRepository {

    private static final String RULES_DIR_NAME = "rules";
    private static final String RULE_FILE_EXTENSION = ".json";

    private final MontoyaApi api;
    private final Path rulesDir;

    public MockRuleRepository(MontoyaApi api, Path configDir) {
        this.api = api;
        this.rulesDir = configDir.resolve(RULES_DIR_NAME);
        ensureRulesDirectoryExists();
    }

    /**
     * 确保规则目录存在
     */
    private void ensureRulesDirectoryExists() {
        try {
            if (!Files.exists(rulesDir)) {
                Files.createDirectories(rulesDir);
                log("Created rules directory: " + rulesDir);
            }
        } catch (IOException e) {
            logError("Failed to create rules directory", e);
        }
    }

    /**
     * 保存规则
     */
    public void save(MockRule rule) {
        if (rule == null || !rule.isValid()) {
            logError("Cannot save invalid rule", null);
            return;
        }

        try {
            Path ruleFile = rulesDir.resolve(rule.getId() + RULE_FILE_EXTENSION);
            JsonUtils.writeToFile(rule, ruleFile);
            log("Saved rule: " + rule.getId() + " (" + rule.getName() + ")");
        } catch (IOException e) {
            logError("Failed to save rule: " + rule.getId(), e);
        }
    }

    /**
     * 加载规则
     */
    public MockRule load(String ruleId) {
        try {
            Path ruleFile = rulesDir.resolve(ruleId + RULE_FILE_EXTENSION);
            if (!Files.exists(ruleFile)) {
                return null;
            }
            return JsonUtils.fromFile(ruleFile, MockRule.class);
        } catch (IOException e) {
            logError("Failed to load rule: " + ruleId, e);
            return null;
        }
    }

    /**
     * 删除规则
     */
    public boolean delete(String ruleId) {
        try {
            Path ruleFile = rulesDir.resolve(ruleId + RULE_FILE_EXTENSION);
            if (Files.exists(ruleFile)) {
                Files.delete(ruleFile);
                log("Deleted rule: " + ruleId);
                return true;
            }
            return false;
        } catch (IOException e) {
            logError("Failed to delete rule: " + ruleId, e);
            return false;
        }
    }

    /**
     * 加载所有规则
     */
    public List<MockRule> loadAll() {
        List<MockRule> rules = new ArrayList<>();

        try (Stream<Path> paths = Files.list(rulesDir)) {
            List<Path> ruleFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(RULE_FILE_EXTENSION))
                    .collect(Collectors.toList());

            for (Path ruleFile : ruleFiles) {
                try {
                    MockRule rule = JsonUtils.fromFile(ruleFile, MockRule.class);
                    if (rule != null && rule.isValid()) {
                        rules.add(rule);
                    } else {
                        log("Skipping invalid rule file: " + ruleFile.getFileName());
                    }
                } catch (IOException e) {
                    logError("Failed to load rule from file: " + ruleFile.getFileName(), e);
                }
            }

            log("Loaded " + rules.size() + " rules");
        } catch (IOException e) {
            logError("Failed to list rules directory", e);
        }

        return rules;
    }

    /**
     * 导出所有规则到指定路径
     */
    public void exportAll(Path exportPath) throws IOException {
        List<MockRule> rules = loadAll();
        JsonUtils.writeToFile(rules, exportPath);
        log("Exported " + rules.size() + " rules to: " + exportPath);
    }

    /**
     * 从指定路径导入规则
     */
    @SuppressWarnings("unchecked")
    public int importFrom(Path importPath) throws IOException {
        List<MockRule> rules = JsonUtils.fromFile(importPath, List.class);
        int count = 0;
        for (MockRule rule : rules) {
            if (rule != null && rule.isValid()) {
                // 重新生成ID以避免冲突
                rule.setId(java.util.UUID.randomUUID().toString());
                save(rule);
                count++;
            }
        }
        log("Imported " + count + " rules from: " + importPath);
        return count;
    }

    /**
     * 获取规则存储目录
     */
    public Path getRulesDir() {
        return rulesDir;
    }

    /**
     * 日志记录
     */
    private void log(String message) {
        if (api != null) {
            api.logging().logToOutput("[MockRuleRepository] " + message);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, Throwable e) {
        if (api != null) {
            if (e != null) {
                api.logging().logToError("[MockRuleRepository] " + message, e);
            } else {
                api.logging().logToError("[MockRuleRepository] " + message);
            }
        }
    }
}
