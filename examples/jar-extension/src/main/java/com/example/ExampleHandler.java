package com.example;

import burp.api.montoya.http.message.HttpHeader;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import org.oxff.hellomocker.api.IMockHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * HelloMocker 示例处理器
 * 
 * 这是一个完整的示例，展示了如何实现 IMockHandler 接口来处理 HTTP 请求。
 * 
 * 功能：
 * - 记录请求信息到控制台
 * - 根据 URL 路径返回不同的响应
 * - 展示如何访问请求头和请求体
 * - 展示如何构建响应
 *
 * @author Example Developer
 * @version 1.0.0
 */
public class ExampleHandler implements IMockHandler {

    /**
     * 处理 HTTP 请求的主方法
     * 
     * @param request Burp HTTP 请求对象
     * @return Burp HTTP 响应对象
     */
    @Override
    public HttpResponse handleRequest(HttpRequest request) {
        // 1. 获取请求基本信息
        String url = request.url();
        String method = request.method();
        String path = extractPath(url);
        
        // 2. 记录请求信息（可以在 BurpSuite 的 Output 窗口查看）
        System.out.println("[ExampleHandler] Received request:");
        System.out.println("  Method: " + method);
        System.out.println("  URL: " + url);
        System.out.println("  Path: " + path);
        
        // 3. 获取并记录请求头
        List<HttpHeader> headers = request.headers();
        System.out.println("  Headers:");
        for (HttpHeader header : headers) {
            System.out.println("    " + header.name() + ": " + header.value());
        }
        
        // 4. 根据路径返回不同的响应
        String responseBody;
        int statusCode;
        String contentType;
        
        if ("/api/hello".equals(path)) {
            // 示例 1：简单的问候接口
            responseBody = buildHelloResponse(method, url);
            statusCode = 200;
            contentType = "application/json";
            
        } else if (path.startsWith("/api/users/")) {
            // 示例 2：RESTful API，从路径提取参数
            String userId = path.substring("/api/users/".length());
            responseBody = buildUserResponse(userId);
            statusCode = 200;
            contentType = "application/json";
            
        } else if ("/api/error".equals(path)) {
            // 示例 3：模拟错误响应
            responseBody = "{\"error\": \"Internal Server Error\", \"code\": 500}";
            statusCode = 500;
            contentType = "application/json";
            
        } else {
            // 默认响应：404 Not Found
            responseBody = "{\"error\": \"Not Found\", \"path\": \"" + path + "\"}";
            statusCode = 404;
            contentType = "application/json";
        }
        
        // 5. 构建 HTTP 响应字符串
        String httpResponse = buildHttpResponse(statusCode, contentType, responseBody);
        
        // 6. 返回 Burp HttpResponse 对象
        return HttpResponse.httpResponse(httpResponse);
    }
    
    /**
     * 从 URL 中提取路径部分
     */
    private String extractPath(String url) {
        try {
            java.net.URL urlObj = new java.net.URL(url);
            return urlObj.getPath();
        } catch (Exception e) {
            return url;
        }
    }
    
    /**
     * 构建问候响应
     */
    private String buildHelloResponse(String method, String url) {
        return String.format(
            "{" +
            "\"message\": \"Hello from HelloMocker JAR Extension!\"," +
            "\"method\": \"%s\"," +
            "\"url\": \"%s\"," +
            "\"timestamp\": %d" +
            "}",
            method,
            url,
            System.currentTimeMillis()
        );
    }
    
    /**
     * 构建用户信息响应
     */
    private String buildUserResponse(String userId) {
        return String.format(
            "{" +
            "\"user\": {" +
            "\"id\": \"%s\"," +
            "\"name\": \"User %s\"," +
            "\"email\": \"user%s@example.com\"," +
            "\"role\": \"member\"" +
            "}," +
            "\"status\": \"success\"" +
            "}",
            userId, userId, userId
        );
    }
    
    /**
     * 构建 HTTP 响应字符串
     */
    private String buildHttpResponse(int statusCode, String contentType, String body) {
        StringBuilder sb = new StringBuilder();
        
        // 状态行
        sb.append("HTTP/1.1 ").append(statusCode).append(" ").append(getStatusText(statusCode)).append("\r\n");
        
        // 响应头
        sb.append("Content-Type: ").append(contentType).append("\r\n");
        sb.append("Content-Length: ").append(body.getBytes().length).append("\r\n");
        sb.append("X-Handler: HelloMocker-ExampleHandler\r\n");
        sb.append("\r\n");
        
        // 响应体
        sb.append(body);
        
        return sb.toString();
    }
    
    /**
     * 获取状态码文本
     */
    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Unknown";
        };
    }
    
    /**
     * 获取处理器名称（显示在插件界面中）
     */
    @Override
    public String getName() {
        return "Example Handler";
    }
    
    /**
     * 获取处理器描述
     */
    @Override
    public String getDescription() {
        return "A simple example handler demonstrating IMockHandler implementation";
    }
    
    /**
     * 初始化回调
     * 在 JAR 被加载后调用，可以在这里进行初始化操作
     */
    @Override
    public void init() {
        System.out.println("[ExampleHandler] Initialized successfully!");
        // 可以在这里加载配置、建立连接等
    }
    
    /**
     * 销毁回调
     * 在 JAR 被卸载前调用，可以在这里进行清理操作
     */
    @Override
    public void destroy() {
        System.out.println("[ExampleHandler] Destroyed.");
        // 可以在这里关闭连接、释放资源等
    }
}