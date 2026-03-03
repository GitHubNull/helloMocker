package org.oxff.hellomocker.util;

import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * HTTP工具类
 * 提供HTTP相关的辅助方法
 *
 * @author oxff
 * @version 1.0
 */
public class HttpUtils {

    private HttpUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 从URL字符串中提取路径
     *
     * @param urlString URL字符串
     * @return 路径部分，解析失败返回空字符串
     */
    public static String extractPath(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return "";
        }
        try {
            URL url = new URL(urlString);
            String path = url.getPath();
            return path != null ? path : "";
        } catch (MalformedURLException e) {
            return "";
        }
    }

    /**
     * 从URL字符串中提取查询字符串
     *
     * @param urlString URL字符串
     * @return 查询字符串，解析失败返回null
     */
    public static String extractQuery(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            return url.getQuery();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 从URL字符串中提取主机名
     *
     * @param urlString URL字符串
     * @return 主机名，解析失败返回null
     */
    public static String extractHost(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            return url.getHost();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 从URL字符串中提取端口
     *
     * @param urlString URL字符串
     * @return 端口，解析失败返回-1
     */
    public static int extractPort(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return -1;
        }
        try {
            URL url = new URL(urlString);
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            return port;
        } catch (MalformedURLException e) {
            return -1;
        }
    }

    /**
     * 从URL字符串中提取协议
     *
     * @param urlString URL字符串
     * @return 协议（http/https），解析失败返回null
     */
    public static String extractProtocol(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return null;
        }
        try {
            URL url = new URL(urlString);
            return url.getProtocol();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     * 检查Content-Type是否为JSON
     *
     * @param contentType Content-Type头值
     * @return 是否为JSON
     */
    public static boolean isJsonContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase().contains("application/json");
    }

    /**
     * 检查Content-Type是否为HTML
     *
     * @param contentType Content-Type头值
     * @return 是否为HTML
     */
    public static boolean isHtmlContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase().contains("text/html");
    }

    /**
     * 检查Content-Type是否为XML
     *
     * @param contentType Content-Type头值
     * @return 是否为XML
     */
    public static boolean isXmlContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        String lower = contentType.toLowerCase();
        return lower.contains("application/xml") || lower.contains("text/xml");
    }

    /**
     * 获取默认Content-Type
     *
     * @param body 响应体
     * @return 默认Content-Type
     */
    public static String getDefaultContentType(String body) {
        if (body == null || body.trim().isEmpty()) {
            return "text/plain";
        }
        String trimmed = body.trim();
        if ((trimmed.startsWith("{") && trimmed.endsWith("}")) ||
            (trimmed.startsWith("[") && trimmed.endsWith("]"))) {
            return "application/json";
        }
        if (trimmed.startsWith("<?xml") || trimmed.startsWith("<")) {
            return "text/xml";
        }
        if (trimmed.toLowerCase().startsWith("<!doctype html") || 
            trimmed.toLowerCase().startsWith("<html")) {
            return "text/html";
        }
        return "text/plain";
    }

    /**
     * 构建完整的URL
     *
     * @param protocol 协议
     * @param host 主机
     * @param port 端口
     * @param path 路径
     * @param query 查询字符串
     * @return 完整URL
     */
    public static String buildUrl(String protocol, String host, int port, String path, String query) {
        StringBuilder sb = new StringBuilder();
        sb.append(protocol != null ? protocol : "http").append("://");
        sb.append(host != null ? host : "localhost");
        
        if (port > 0) {
            boolean isDefaultPort = ("http".equalsIgnoreCase(protocol) && port == 80) ||
                                    ("https".equalsIgnoreCase(protocol) && port == 443);
            if (!isDefaultPort) {
                sb.append(":").append(port);
            }
        }
        
        if (path != null && !path.isEmpty()) {
            if (!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
        }
        
        if (query != null && !query.isEmpty()) {
            sb.append("?").append(query);
        }
        
        return sb.toString();
    }

    /**
     * 从Burp请求中提取请求体字符串
     *
     * @param request Burp请求
     * @return 请求体字符串
     */
    public static String getRequestBodyAsString(HttpRequest request) {
        if (request == null || request.body() == null) {
            return "";
        }
        return request.body().toString();
    }

    /**
     * 从Burp响应中提取响应体字符串
     *
     * @param response Burp响应
     * @return 响应体字符串
     */
    public static String getResponseBodyAsString(HttpResponse response) {
        if (response == null || response.body() == null) {
            return "";
        }
        return response.body().toString();
    }
}
