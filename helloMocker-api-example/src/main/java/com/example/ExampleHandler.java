package com.example;

import burp.api.montoya.core.ByteArray;
import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HelloMocker 完整的JAR扩展示例处理器
 * 
 * 此示例展示了如何实现 IMockHandler 接口来创建功能完整的Mock处理器。
 * 
 * 功能特性：
 * - 多路由支持（根据URL路径返回不同响应）
 * - 请求信息解析（URL、Method、Headers、Body）
 * - 查询参数解析
 * - JSON/XML/PlainText多种响应格式
 * - 请求计数统计
 * - 延迟响应模拟
 * 
 * @author Example Developer
 * @version 1.0.0
 */
public class ExampleHandler implements IMockHandler {

    // 请求计数器（线程安全）
    private final Map<String, Integer> requestCounter = new ConcurrentHashMap<>();
    
    // 处理器启动时间
    private LocalDateTime startTime;

    /**
     * 处理 HTTP 请求的主方法
     * 
     * 这是必须实现的核心方法，每个匹配的请求都会调用此方法。
     * 
     * @param request Burp HTTP 请求对象，包含完整的请求信息
     * @return Burp HTTP 响应对象
     */
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        try {
            // ========== 1. 解析请求信息 ==========
            
            // 获取基础信息
            String url = request.url();
            String method = request.method();
            String path = extractPath(url);
            
            // 增加请求计数
            requestCounter.merge(path, 1, Integer::sum);
            int count = requestCounter.getOrDefault(path, 0);
            
            // 打印请求日志（可以在BurpSuite的Output窗口查看）
            System.out.println("[" + getName() + "] Request #" + count);
            System.out.println("  Time: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
            System.out.println("  Method: " + method);
            System.out.println("  URL: " + url);
            System.out.println("  Path: " + path);
            
            // ========== 2. 路由分发 ==========
            
            // 根据不同的URL路径返回不同的响应
            if ("/api/hello".equals(path)) {
                return handleHelloRequest(request, count);
                
            } else if ("/api/users".equals(path)) {
                return handleUsersRequest(request, count);
                
            } else if (path.startsWith("/api/users/")) {
                return handleUserDetailRequest(request, path, count);
                
            } else if ("/api/echo".equals(path)) {
                return handleEchoRequest(request, count);
                
            } else if ("/api/delay".equals(path)) {
                return handleDelayRequest(request, count);
                
            } else if ("/api/error".equals(path)) {
                return handleErrorRequest(request, count);
                
            } else if ("/api/stats".equals(path)) {
                return handleStatsRequest(count);
                
            } else {
                // 默认：404 Not Found
                return buildJsonResponse(404, "{\"error\": \"Not Found\", \"path\": \"" + path + "\"}");
            }
            
        } catch (Exception e) {
            // 异常处理：返回500错误
            System.err.println("[" + getName() + "] Error handling request: " + e.getMessage());
            e.printStackTrace();
            return buildJsonResponse(500, 
                "{\"error\": \"Internal Server Error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
    
    /**
     * 处理 /api/hello - 简单的问候接口
     */
    private HttpResponse handleHelloRequest(HttpRequest request, int count) {
        String body = String.format(
            "{" +
            "\"message\": \"Hello from HelloMocker JAR Extension!\"," +
            "\"handler\": \"%s\"," +
            "\"requestCount\": %d," +
            "\"timestamp\": \"%s\"" +
            "}",
            getName(),
            count,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        
        return buildJsonResponse(200, body);
    }
    
    /**
     * 处理 /api/users - 用户列表接口
     */
    private HttpResponse handleUsersRequest(HttpRequest request, int count) {
        // 获取查询参数
        String query = extractQuery(request.url());
        int page = 1;
        int limit = 10;
        
        if (query != null && !query.isEmpty()) {
            Map<String, String> params = parseQueryString(query);
            page = Integer.parseInt(params.getOrDefault("page", "1"));
            limit = Integer.parseInt(params.getOrDefault("limit", "10"));
        }
        
        // 构建用户列表JSON
        StringBuilder users = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            int userId = (page - 1) * limit + i + 1;
            if (i > 0) users.append(",");
            users.append(String.format(
                "{\"id\": %d, \"name\": \"User %d\", \"email\": \"user%d@example.com\"}",
                userId, userId, userId
            ));
        }
        
        String body = String.format(
            "{" +
            "\"users\": [%s]," +
            "\"page\": %d," +
            "\"limit\": %d," +
            "\"total\": 100," +
            "\"requestCount\": %d" +
            "}",
            users.toString(), page, limit, count
        );
        
        return buildJsonResponse(200, body);
    }
    
    /**
     * 处理 /api/users/{id} - 用户详情接口
     */
    private HttpResponse handleUserDetailRequest(HttpRequest request, String path, int count) {
        // 从路径中提取用户ID
        String userId = path.substring("/api/users/".length());
        
        // 模拟用户不存在的情况
        if ("999".equals(userId)) {
            return buildJsonResponse(404, 
                "{\"error\": \"User not found\", \"userId\": \"" + userId + "\"}");
        }
        
        String body = String.format(
            "{" +
            "\"id\": \"%s\"," +
            "\"name\": \"User %s\"," +
            "\"email\": \"user%s@example.com\"," +
            "\"role\": \"member\"," +
            "\"createdAt\": \"2024-01-01T00:00:00\"," +
            "\"requestCount\": %d" +
            "}",
            userId, userId, userId, count
        );
        
        return buildJsonResponse(200, body);
    }
    
    /**
     * 处理 /api/echo - 回显请求信息
     */
    private HttpResponse handleEchoRequest(HttpRequest request, int count) {
        // 获取请求体
        String requestBody = "";
        ByteArray body = request.body();
        if (body != null && body.length() > 0) {
            requestBody = new String(body.getBytes(), StandardCharsets.UTF_8);
            // 限制长度，防止响应过大
            if (requestBody.length() > 1000) {
                requestBody = requestBody.substring(0, 1000) + "... (truncated)";
            }
        }
        
        // 构建headers列表
        StringBuilder headers = new StringBuilder();
        List<HttpHeader> headerList = request.headers();
        for (int i = 0; i < headerList.size() && i < 10; i++) {
            HttpHeader h = headerList.get(i);
            if (i > 0) headers.append(",");
            headers.append(String.format("\"%s\": \"%s\"", h.name(), h.value()));
        }
        
        String responseBody = String.format(
            "{" +
            "\"echo\": true," +
            "\"method\": \"%s\"," +
            "\"url\": \"%s\"," +
            "\"headers\": {%s}," +
            "\"body\": \"%s\"," +
            "\"requestCount\": %d" +
            "}",
            request.method(),
            request.url(),
            headers.toString(),
            requestBody.replace("\"", "\\\"").replace("\n", "\\n"),
            count
        );
        
        return buildJsonResponse(200, responseBody);
    }
    
    /**
     * 处理 /api/delay - 延迟响应（模拟慢接口）
     */
    private HttpResponse handleDelayRequest(HttpRequest request, int count) {
        try {
            // 模拟2秒延迟
            System.out.println("[" + getName() + "] Simulating delay...");
            Thread.sleep(2000);
            
            return buildJsonResponse(200, String.format(
                "{\"message\": \"Delayed response after 2 seconds\", \"requestCount\": %d}",
                count
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return buildJsonResponse(500, "{\"error\": \"Delay interrupted\"}");
        }
    }
    
    /**
     * 处理 /api/error - 模拟错误
     */
    private HttpResponse handleErrorRequest(HttpRequest request, int count) {
        return buildJsonResponse(500, String.format(
            "{" +
            "\"error\": \"Internal Server Error\"," +
            "\"message\": \"This is a simulated error\"," +
            "\"code\": 500," +
            "\"requestCount\": %d" +
            "}",
            count
        ));
    }
    
    /**
     * 处理 /api/stats - 统计信息
     */
    private HttpResponse handleStatsRequest(int count) {
        StringBuilder stats = new StringBuilder();
        stats.append("\"endpoints\": {");
        
        int i = 0;
        for (Map.Entry<String, Integer> entry : requestCounter.entrySet()) {
            if (i > 0) stats.append(",");
            stats.append(String.format("\"%s\": %d", entry.getKey(), entry.getValue()));
            i++;
        }
        stats.append("}");
        
        String body = String.format(
            "{" +
            "\"handler\": \"%s\"," +
            "\"totalRequests\": %d," +
            "\"uniqueEndpoints\": %d," +
            "\"startTime\": \"%s\"," +
            "%s" +
            "}",
            getName(),
            requestCounter.values().stream().mapToInt(Integer::intValue).sum(),
            requestCounter.size(),
            startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            stats.toString()
        );
        
        return buildJsonResponse(200, body);
    }
    
    // ========== 工具方法 ==========
    
    /**
     * 从URL中提取路径部分
     */
    private String extractPath(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getPath();
        } catch (Exception e) {
            return url;
        }
    }
    
    /**
     * 从URL中提取查询字符串
     */
    private String extractQuery(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getQuery();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 解析查询字符串为Map
     */
    private Map<String, String> parseQueryString(String query) {
        Map<String, String> result = new ConcurrentHashMap<>();
        if (query == null || query.isEmpty()) {
            return result;
        }
        
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                result.put(kv[0], kv[1]);
            }
        }
        return result;
    }
    
    /**
     * 构建JSON响应
     */
    private HttpResponse buildJsonResponse(int statusCode, String jsonBody) {
        String httpResponse = String.format(
            "HTTP/1.1 %d %s\r\n" +
            "Content-Type: application/json; charset=UTF-8\r\n" +
            "Content-Length: %d\r\n" +
            "X-Handler: %s\r\n" +
            "\r\n" +
            "%s",
            statusCode,
            getStatusText(statusCode),
            jsonBody.getBytes(StandardCharsets.UTF_8).length,
            getName(),
            jsonBody
        );
        
        return HttpResponse.httpResponse(httpResponse);
    }
    
    /**
     * 获取状态码文本
     */
    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 400 -> "Bad Request";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
    
    // ========== IMockHandler 接口实现 ==========
    
    /**
     * 获取处理器名称
     * 显示在HelloMocker插件界面的状态栏中
     */
    @Override
    public String getName() {
        return "ExampleHandler-v1.0";
    }
    
    /**
     * 获取处理器描述
     */
    @Override
    public String getDescription() {
        return "A complete example demonstrating all features of IMockHandler interface, " +
               "including routing, request parsing, and various response types.";
    }
    
    /**
     * 初始化处理器
     * 在JAR被加载到插件后调用，可以进行初始化操作
     */
    @Override
    public void init() {
        startTime = LocalDateTime.now();
        System.out.println("========================================");
        System.out.println("[" + getName() + "] Initialized successfully!");
        System.out.println("  Start Time: " + startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        System.out.println("  Description: " + getDescription());
        System.out.println("  Supported endpoints:");
        System.out.println("    GET  /api/hello       - Hello message");
        System.out.println("    GET  /api/users       - User list (supports ?page=&limit=)");
        System.out.println("    GET  /api/users/{id}  - User detail");
        System.out.println("    POST /api/echo        - Echo request info");
        System.out.println("    GET  /api/delay       - Delayed response (2s)");
        System.out.println("    GET  /api/error       - Simulated error (500)");
        System.out.println("    GET  /api/stats       - Request statistics");
        System.out.println("========================================");
    }
    
    /**
     * 销毁处理器
     * 在JAR被卸载前调用，可以进行清理操作
     */
    @Override
    public void destroy() {
        System.out.println("[" + getName() + "] Destroyed.");
        System.out.println("  Total requests by endpoint:");
        for (Map.Entry<String, Integer> entry : requestCounter.entrySet()) {
            System.out.println("    " + entry.getKey() + ": " + entry.getValue());
        }
        requestCounter.clear();
    }
}