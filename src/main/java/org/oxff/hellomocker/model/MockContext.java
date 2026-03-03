package org.oxff.hellomocker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock上下文
 * 包含HTTP请求的完整信息，传递给响应处理器
 *
 * @author oxff
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MockContext implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 主机名
     */
    private String host;

    /**
     * 端口
     */
    private int port;

    /**
     * 协议（http/https）
     */
    private String protocol;

    /**
     * 额外数据（用于扩展）
     */
    @Builder.Default
    private Map<String, Object> extra = new HashMap<>();

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
     * 添加请求头
     */
    public void addHeader(String name, String value) {
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put(name, value);
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
     * 获取额外数据
     */
    @SuppressWarnings("unchecked")
    public <T> T getExtra(String key) {
        return extra != null ? (T) extra.get(key) : null;
    }

    /**
     * 设置额外数据
     */
    public void setExtra(String key, Object value) {
        if (extra == null) {
            extra = new HashMap<>();
        }
        extra.put(key, value);
    }

    /**
     * 从BurpSuite HttpRequest创建MockContext
     */
    public static MockContext fromBurpRequest(burp.api.montoya.http.message.requests.HttpRequest request) {
        java.util.Map<String, String> headersMap = new java.util.HashMap<>();
        request.headers().forEach(header -> 
            headersMap.put(header.name(), header.value())
        );

        MockContextBuilder builder = MockContext.builder()
                .url(request.url())
                .method(request.method())
                .body(request.body().getBytes())
                .headers(headersMap);

        // 解析URL组件
        try {
            java.net.URL url = new java.net.URL(request.url());
            builder.path(url.getPath())
                   .query(url.getQuery())
                   .host(url.getHost())
                   .port(url.getPort() == -1 ? url.getDefaultPort() : url.getPort())
                   .protocol(url.getProtocol());
        } catch (java.net.MalformedURLException e) {
            // URL解析失败，使用原始值
        }

        return builder.build();
    }
}
