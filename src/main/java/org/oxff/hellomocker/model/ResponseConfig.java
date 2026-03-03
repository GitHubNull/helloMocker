package org.oxff.hellomocker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应配置
 * 定义Mock响应的各种模式
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应类型
     */
    @Builder.Default
    private ResponseType type = ResponseType.STATIC;

    // ============ Python脚本模式配置 ============

    /**
     * Python脚本代码（在线编写）
     */
    private String pythonScript;

    /**
     * Python脚本文件路径（导入的文件，可选）
     */
    private String pythonFilePath;

    // ============ 静态响应模式配置 ============

    /**
     * HTTP状态码
     */
    @Builder.Default
    private int statusCode = 200;

    /**
     * 响应头
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * 响应体
     */
    private String body;

    // ============ 代理转发模式配置 ============

    /**
     * 目标主机
     */
    private String targetHost;

    /**
     * 目标端口
     */
    @Builder.Default
    private int targetPort = 80;

    /**
     * 是否使用SSL
     */
    @Builder.Default
    private boolean useSsl = false;

    /**
     * 响应类型枚举
     */
    public enum ResponseType {
        /**
         * Python脚本动态生成响应
         */
        PYTHON_SCRIPT,

        /**
         * 静态响应（从配置中直接返回）
         */
        STATIC,

        /**
         * 转发到上游服务器
         */
        PROXY_FORWARD
    }

    /**
     * 检查响应配置是否有效
     */
    public boolean isValid() {
        if (type == null) {
            return false;
        }

        return switch (type) {
            case PYTHON_SCRIPT -> isPythonScriptValid();
            case STATIC -> isStaticResponseValid();
            case PROXY_FORWARD -> isProxyForwardValid();
            default -> false;
        };
    }

    /**
     * 检查Python脚本配置是否有效
     */
    private boolean isPythonScriptValid() {
        return (pythonScript != null && !pythonScript.trim().isEmpty())
                || (pythonFilePath != null && !pythonFilePath.trim().isEmpty());
    }

    /**
     * 检查静态响应配置是否有效
     */
    private boolean isStaticResponseValid() {
        // 静态响应至少要有状态码，body可以为空
        return statusCode >= 100 && statusCode < 600;
    }

    /**
     * 检查代理转发配置是否有效
     */
    private boolean isProxyForwardValid() {
        return targetHost != null && !targetHost.trim().isEmpty()
                && targetPort > 0 && targetPort <= 65535;
    }

    /**
     * 获取响应类型描述
     */
    public String getTypeDescription() {
        return type != null ? type.name() : "UNKNOWN";
    }

    /**
     * 从字符串解析响应类型
     */
    public static ResponseType parseResponseType(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return ResponseType.STATIC;
        }
        try {
            return ResponseType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseType.STATIC;
        }
    }

    /**
     * 添加响应头
     */
    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
    }

    /**
     * 获取响应头（如果不存在返回默认值）
     */
    public String getHeader(String name, String defaultValue) {
        if (headers == null) {
            return defaultValue;
        }
        return headers.getOrDefault(name, defaultValue);
    }
}
