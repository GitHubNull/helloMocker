package org.oxff.hellomocker.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock请求对象
 * 传递给扩展JAR包中的处理器
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockRequest {

    /**
     * 完整URL
     */
    private String url;

    /**
     * HTTP方法
     */
    private String method;

    /**
     * 请求头
     */
    @Builder.Default
    private Map<String, String> headers = new HashMap<>();

    /**
     * 请求体（字节数组）
     */
    private byte[] body;

    /**
     * URL路径部分
     */
    private String path;

    /**
     * URL查询字符串
     */
    private String query;

    /**
     * 获取请求头
     */
    public String getHeader(String name) {
        return headers != null ? headers.get(name) : null;
    }

    /**
     * 获取请求头（带默认值）
     */
    public String getHeader(String name, String defaultValue) {
        String value = getHeader(name);
        return value != null ? value : defaultValue;
    }

    /**
     * 获取请求体字符串（UTF-8编码）
     */
    public String getBodyAsString() {
        if (body == null) {
            return "";
        }
        return new String(body, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 从MockContext创建MockRequest
     */
    public static MockRequest fromContext(org.oxff.hellomocker.model.MockContext context) {
        if (context == null) {
            return null;
        }
        return MockRequest.builder()
                .url(context.getUrl())
                .method(context.getMethod())
                .headers(context.getHeaders() != null ? new HashMap<>(context.getHeaders()) : new HashMap<>())
                .body(context.getBody())
                .path(context.getPath())
                .query(context.getQuery())
                .build();
    }
}
