package org.oxff.hellomocker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock响应
 * 包含生成的HTTP响应数据
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockResponse implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 响应体（字节数组）
     */
    private byte[] body;

    /**
     * 延迟时间（毫秒，用于模拟网络延迟）
     */
    @Builder.Default
    private int delay = 0;

    /**
     * 是否处理成功
     */
    @Builder.Default
    private boolean success = true;

    /**
     * 错误信息（如果处理失败）
     */
    private String errorMessage;

    /**
     * 处理器类型（用于调试）
     */
    private String handlerType;

    /**
     * 创建成功响应
     */
    public static MockResponse success(int statusCode, byte[] body) {
        return MockResponse.builder()
                .statusCode(statusCode)
                .body(body)
                .success(true)
                .build();
    }

    /**
     * 创建成功响应（字符串body）
     */
    public static MockResponse success(int statusCode, String body) {
        return success(statusCode, body.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 创建错误响应
     */
    public static MockResponse error(String errorMessage) {
        return MockResponse.builder()
                .statusCode(500)
                .success(false)
                .errorMessage(errorMessage)
                .body(errorMessage.getBytes(java.nio.charset.StandardCharsets.UTF_8))
                .build();
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
     * 获取响应头
     */
    public String getHeader(String name) {
        return headers != null ? headers.get(name) : null;
    }

    /**
     * 获取响应体字符串（UTF-8编码）
     */
    public String getBodyAsString() {
        if (body == null) {
            return "";
        }
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 设置默认内容类型
     */
    public void setDefaultContentType(String contentType) {
        if (getHeader("Content-Type") == null) {
            addHeader("Content-Type", contentType);
        }
    }

    /**
     * 转换为BurpSuite HttpResponse
     */
    public burp.api.montoya.http.message.responses.HttpResponse toBurpResponse(
            burp.api.montoya.http.message.responses.HttpResponse originalResponse) {
        
        // 由于Montoya API的限制，这里返回一个新的响应
        // 实际使用时，可能需要在处理器中手动构建响应
        return originalResponse;
    }
}
