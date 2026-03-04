package org.oxff.hellomocker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * URL匹配条件
 * 定义Mock规则的匹配规则
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MatchCondition implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 匹配类型
     */
    @Builder.Default
    private MatchType type = MatchType.CONTAINS;

    /**
     * URL匹配模式
     */
    private String urlPattern;

    /**
     * HTTP方法过滤（可选，null表示匹配所有方法）
     */
    private String method;

    /**
     * 匹配类型枚举
     */
    public enum MatchType {
        /**
         * URL完全相等
         */
        EQUALS,

        /**
         * URL包含模式
         */
        CONTAINS,

        /**
         * 正则表达式匹配
         */
        REGEX,

        /**
         * URL以模式开头
         */
        STARTS_WITH,

        /**
         * URL以模式结尾
         */
        ENDS_WITH
    }

    /**
     * 检查匹配条件是否有效
     */
    public boolean isValid() {
        return urlPattern != null && !urlPattern.trim().isEmpty()
                && type != null;
    }

    /**
     * 获取匹配类型描述
     */
    public String getTypeDescription() {
        return type != null ? type.name() : "UNKNOWN";
    }

    /**
     * 从字符串解析匹配类型
     */
    public static MatchType parseMatchType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return MatchType.CONTAINS;
        }
        try {
            return MatchType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return MatchType.CONTAINS;
        }
    }
}
