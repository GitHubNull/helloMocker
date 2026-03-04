package org.oxff.hellomocker.service;

import org.oxff.hellomocker.model.MatchCondition;
import org.oxff.hellomocker.model.MockRule;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 规则匹配器
 * 提供高效的URL匹配功能
 *
 * @author oxff
 * @version 1.0
 */
public class RuleMatcher {

    // 缓存编译后的正则表达式
    private final ConcurrentHashMap<String, Pattern> patternCache;

    public RuleMatcher() {
        this.patternCache = new ConcurrentHashMap<>();
    }

    /**
     * 检查规则是否匹配URL
     *
     * @param rule   Mock规则
     * @param url    请求URL
     * @param method HTTP方法
     * @return 是否匹配
     */
    public boolean matches(MockRule rule, String url, String method) {
        if (rule == null || !rule.isEnabled()) {
            return false;
        }

        MatchCondition condition = rule.getMatchCondition();
        if (condition == null) {
            return false;
        }

        // 检查HTTP方法
        if (!matchesMethod(condition, method)) {
            return false;
        }

        // 检查URL匹配
        return matchesUrl(condition, url);
    }

    /**
     * 检查HTTP方法是否匹配
     */
    private boolean matchesMethod(MatchCondition condition, String method) {
        String ruleMethod = condition.getMethod();
        
        // 如果未指定方法，匹配所有
        if (ruleMethod == null || ruleMethod.trim().isEmpty()) {
            return true;
        }

        // 不区分大小写比较
        return ruleMethod.equalsIgnoreCase(method);
    }

    /**
     * 检查URL是否匹配
     */
    private boolean matchesUrl(MatchCondition condition, String url) {
        String pattern = condition.getUrlPattern();
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        if (url == null) {
            return false;
        }

        try {
            return switch (condition.getType()) {
                case EQUALS -> url.equals(pattern);
                case CONTAINS -> url.contains(pattern);
                case STARTS_WITH -> url.startsWith(pattern);
                case ENDS_WITH -> url.endsWith(pattern);
                case REGEX -> matchesRegex(pattern, url);
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 正则表达式匹配
     */
    private boolean matchesRegex(String pattern, String url) {
        try {
            // 从缓存获取编译后的Pattern
            Pattern compiledPattern = patternCache.computeIfAbsent(pattern, p -> {
                try {
                    return Pattern.compile(p);
                } catch (PatternSyntaxException e) {
                    return null;
                }
            });

            if (compiledPattern == null) {
                return false;
            }

            return compiledPattern.matcher(url).matches();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清除正则表达式缓存
     */
    public void clearCache() {
        patternCache.clear();
    }

    /**
     * 获取缓存大小
     */
    public int getCacheSize() {
        return patternCache.size();
    }

    /**
     * 验证URL模式是否有效
     */
    public static boolean isValidPattern(String pattern, MatchCondition.MatchType type) {
        if (pattern == null || pattern.trim().isEmpty()) {
            return false;
        }

        if (type == MatchCondition.MatchType.REGEX) {
            try {
                Pattern.compile(pattern);
                return true;
            } catch (PatternSyntaxException e) {
                return false;
            }
        }

        return true;
    }
}
