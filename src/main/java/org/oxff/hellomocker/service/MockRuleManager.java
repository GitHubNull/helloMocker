package org.oxff.hellomocker.service;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.storage.MockRuleRepository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Mock规则管理器
 * 负责规则的管理、匹配和CRUD操作
 *
 * @author oxff
 * @version 1.0
 */
public class MockRuleManager {

    private final MontoyaApi api;
    private final ConfigStorage configStorage;
    private final MockRuleRepository repository;
    private final List<MockRule> rules;
    private final Map<String, MockRule> ruleCache;
    private final List<RuleChangeListener> listeners;

    public MockRuleManager(ConfigStorage configStorage) {
        this.api = configStorage.getApi();
        this.configStorage = configStorage;
        
        // 获取配置目录
        Path configDir = Paths.get(System.getProperty("user.home"), ".hellomocker");
        this.repository = new MockRuleRepository(api, configDir);
        
        this.rules = new CopyOnWriteArrayList<>();
        this.ruleCache = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * 加载所有规则
     */
    public void loadRules() {
        rules.clear();
        ruleCache.clear();
        
        List<MockRule> loadedRules = repository.loadAll();
        rules.addAll(loadedRules);
        
        // 按优先级排序
        Collections.sort(rules);
        
        // 更新缓存
        for (MockRule rule : rules) {
            ruleCache.put(rule.getId(), rule);
        }
        
        notifyRulesChanged();
        log("Loaded " + rules.size() + " rules");
    }

    /**
     * 添加规则
     */
    public boolean addRule(MockRule rule) {
        if (rule == null || !rule.isValid()) {
            logError("Cannot add invalid rule", null);
            return false;
        }

        // 检查是否超过最大规则数
        if (rules.size() >= configStorage.getMaxRules()) {
            logError("Maximum number of rules reached: " + configStorage.getMaxRules(), null);
            return false;
        }

        // 生成ID
        if (rule.getId() == null || rule.getId().trim().isEmpty()) {
            rule.setId(UUID.randomUUID().toString());
        }

        // 更新时间戳
        rule.touch();

        // 保存到存储
        repository.save(rule);

        // 添加到内存
        rules.add(rule);
        ruleCache.put(rule.getId(), rule);

        // 重新排序
        Collections.sort(rules);

        notifyRuleAdded(rule);
        log("Added rule: " + rule.getName());
        return true;
    }

    /**
     * 更新规则
     */
    public boolean updateRule(MockRule rule) {
        if (rule == null || !rule.isValid()) {
            logError("Cannot update invalid rule", null);
            return false;
        }

        // 检查规则是否存在
        if (!ruleCache.containsKey(rule.getId())) {
            logError("Rule not found: " + rule.getId(), null);
            return false;
        }

        // 更新时间戳
        rule.touch();

        // 保存到存储
        repository.save(rule);

        // 更新内存
        removeRuleFromList(rule.getId());
        rules.add(rule);
        ruleCache.put(rule.getId(), rule);

        // 重新排序
        Collections.sort(rules);

        notifyRuleUpdated(rule);
        log("Updated rule: " + rule.getName());
        return true;
    }

    /**
     * 删除规则
     */
    public boolean deleteRule(String ruleId) {
        if (ruleId == null || ruleId.trim().isEmpty()) {
            return false;
        }

        MockRule rule = ruleCache.get(ruleId);
        if (rule == null) {
            logError("Rule not found: " + ruleId, null);
            return false;
        }

        // 从存储删除
        repository.delete(ruleId);

        // 从内存删除
        removeRuleFromList(ruleId);
        ruleCache.remove(ruleId);

        notifyRuleDeleted(ruleId);
        log("Deleted rule: " + rule.getName());
        return true;
    }

    /**
     * 从列表中移除规则
     */
    private void removeRuleFromList(String ruleId) {
        rules.removeIf(r -> r.getId().equals(ruleId));
    }

    /**
     * 获取所有规则
     */
    public List<MockRule> getAllRules() {
        return new ArrayList<>(rules);
    }

    /**
     * 获取启用的规则
     */
    public List<MockRule> getEnabledRules() {
        return rules.stream()
                .filter(MockRule::isEnabled)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID获取规则
     */
    public MockRule getRuleById(String ruleId) {
        return ruleCache.get(ruleId);
    }

    /**
     * 启用/禁用规则
     */
    public boolean toggleRuleEnabled(String ruleId) {
        MockRule rule = ruleCache.get(ruleId);
        if (rule == null) {
            return false;
        }

        rule.setEnabled(!rule.isEnabled());
        rule.touch();
        repository.save(rule);

        notifyRuleUpdated(rule);
        log("Toggled rule enabled state: " + rule.getName() + " = " + rule.isEnabled());
        return true;
    }

    /**
     * 匹配规则
     */
    public MockRule matchRule(String url, String method) {
        for (MockRule rule : rules) {
            if (rule.isEnabled() && matches(rule, url, method)) {
                return rule;
            }
        }
        return null;
    }

    /**
     * 检查规则是否匹配
     */
    private boolean matches(MockRule rule, String url, String method) {
        if (rule.getMatchCondition() == null) {
            return false;
        }

        // 检查HTTP方法
        String ruleMethod = rule.getMatchCondition().getMethod();
        if (ruleMethod != null && !ruleMethod.isEmpty()) {
            if (!ruleMethod.equalsIgnoreCase(method)) {
                return false;
            }
        }

        // 检查URL匹配
        String pattern = rule.getMatchCondition().getUrlPattern();
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        return switch (rule.getMatchCondition().getType()) {
            case EQUALS -> url.equals(pattern);
            case CONTAINS -> url.contains(pattern);
            case STARTS_WITH -> url.startsWith(pattern);
            case ENDS_WITH -> url.endsWith(pattern);
            case REGEX -> url.matches(pattern);
            default -> false;
        };
    }

    /**
     * 添加规则变更监听器
     */
    public void addRuleChangeListener(RuleChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * 移除规则变更监听器
     */
    public void removeRuleChangeListener(RuleChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * 通知规则已添加
     */
    private void notifyRuleAdded(MockRule rule) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleAdded(rule);
            } catch (Exception e) {
                logError("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知规则已更新
     */
    private void notifyRuleUpdated(MockRule rule) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleUpdated(rule);
            } catch (Exception e) {
                logError("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知规则已删除
     */
    private void notifyRuleDeleted(String ruleId) {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRuleDeleted(ruleId);
            } catch (Exception e) {
                logError("Error notifying listener", e);
            }
        }
    }

    /**
     * 通知规则列表变更
     */
    private void notifyRulesChanged() {
        for (RuleChangeListener listener : listeners) {
            try {
                listener.onRulesChanged();
            } catch (Exception e) {
                logError("Error notifying listener", e);
            }
        }
    }

    /**
     * 规则变更监听器接口
     */
    public interface RuleChangeListener {
        void onRuleAdded(MockRule rule);
        void onRuleUpdated(MockRule rule);
        void onRuleDeleted(String ruleId);
        void onRulesChanged();
    }

    /**
     * 日志记录
     */
    private void log(String message) {
        if (configStorage.isEnableLogging() && api != null) {
            api.logging().logToOutput("[MockRuleManager] " + message);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, Throwable e) {
        if (api != null) {
            if (e != null) {
                api.logging().logToError("[MockRuleManager] " + message, e);
            } else {
                api.logging().logToError("[MockRuleManager] " + message);
            }
        }
    }
}
