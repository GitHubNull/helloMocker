package org.oxff.hellomocker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mock规则实体类
 * 用于存储和管理HTTP Mock规则
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockRule implements Serializable, Comparable<MockRule> {

    private static final long serialVersionUID = 1L;

    /**
     * 规则唯一标识符
     */
    @Builder.Default
    private String id = UUID.randomUUID().toString();

    /**
     * 规则名称
     */
    private String name;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 是否启用
     */
    @Builder.Default
    private boolean enabled = true;

    /**
     * 优先级（数字越小优先级越高）
     */
    @Builder.Default
    private int priority = 100;

    /**
     * 匹配条件
     */
    private MatchCondition matchCondition;

    /**
     * 响应配置
     */
    private ResponseConfig responseConfig;

    /**
     * 创建时间
     */
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * 更新时间
     */
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    /**
     * 比较优先级，用于排序
     */
    @Override
    public int compareTo(MockRule other) {
        return Integer.compare(this.priority, other.priority);
    }

    /**
     * 更新更新时间戳
     */
    public void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查规则是否有效
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty()
                && matchCondition != null
                && matchCondition.isValid()
                && responseConfig != null
                && responseConfig.isValid();
    }
}
