package org.oxff.hellomocker.http;

import burp.api.montoya.core.Annotations;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import org.oxff.hellomocker.handler.ProxyForwardHandler;
import org.oxff.hellomocker.handler.PythonScriptHandler;
import org.oxff.hellomocker.model.MockContext;
import org.oxff.hellomocker.model.MockResponse;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.util.HttpUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP请求处理器
 * 拦截匹配的HTTP请求并返回Mock响应
 *
 * @author oxff
 * @version 1.0
 */
public class MockHttpHandler implements HttpHandler {

    private final MockRuleManager ruleManager;
    private final ConfigStorage configStorage;
    private final Logging logging;
    private final PythonScriptHandler pythonScriptHandler;
    private final ProxyForwardHandler proxyForwardHandler;

    // 用于存储当前正在处理的请求和规则映射
    // Key: messageId, Value: ruleId
    private final Map<Integer, String> activeMockRequests;

    public MockHttpHandler(MockRuleManager ruleManager, ConfigStorage configStorage, Logging logging) {
        this.ruleManager = ruleManager;
        this.configStorage = configStorage;
        this.logging = logging;
        this.pythonScriptHandler = new PythonScriptHandler(configStorage);
        this.proxyForwardHandler = new ProxyForwardHandler();
        this.activeMockRequests = new ConcurrentHashMap<>();
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent) {
        // 获取请求信息
        HttpRequest request = requestToBeSent;
        String url = request.url();
        String method = request.method();
        int messageId = requestToBeSent.messageId();

        // 查找匹配的规则
        MockRule matchedRule = findMatchingRule(url, method);

        if (matchedRule != null) {
            log("Intercepted request: " + method + " " + url + " -> Rule: " + matchedRule.getName());

            // 存储消息ID和规则ID的映射
            activeMockRequests.put(messageId, matchedRule.getId());

            // 在Annotations中添加注释
            Annotations annotations = requestToBeSent.annotations()
                    .withNotes("HelloMocker: " + matchedRule.getName());

            // 继续发送请求（我们会在响应阶段替换它）
            return RequestToBeSentAction.continueWith(requestToBeSent, annotations);
        }

        // 没有匹配的规则，继续原始请求
        return RequestToBeSentAction.continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived) {
        int messageId = responseReceived.messageId();
        
        // 检查此消息是否标记为Mock请求
        String ruleId = activeMockRequests.remove(messageId);

        if (ruleId != null) {
            MockRule rule = ruleManager.getRuleById(ruleId);

            if (rule != null) {
                try {
                    // 创建Mock上下文（从原始请求）
                    HttpRequest originalRequest = responseReceived.initiatingRequest();
                    MockContext context = MockContext.fromBurpRequest(originalRequest);

                    // 生成Mock响应
                    MockResponse mockResponse = generateMockResponse(context, rule);

                    if (mockResponse != null && mockResponse.isSuccess()) {
                        // 构建Burp响应
                        HttpResponse mockHttpResponse = buildBurpResponse(mockResponse);

                        log("Returning mock response for rule: " + rule.getName() + 
                            " (Status: " + mockResponse.getStatusCode() + ")");

                        // 返回Mock响应，保持原始注释
                        return ResponseReceivedAction.continueWith(mockHttpResponse, responseReceived.annotations());
                    } else {
                        logError("Failed to generate mock response for rule: " + rule.getName() + 
                                 " - " + (mockResponse != null ? mockResponse.getErrorMessage() : "Unknown error"), null);
                    }
                } catch (Exception e) {
                    logError("Error generating mock response for rule: " + rule.getName(), e);
                }
            }
        }

        // 返回原始响应
        return ResponseReceivedAction.continueWith(responseReceived);
    }

    /**
     * 查找匹配的规则
     */
    private MockRule findMatchingRule(String url, String method) {
        List<MockRule> enabledRules = ruleManager.getEnabledRules();
        
        for (MockRule rule : enabledRules) {
            if (matches(rule, url, method)) {
                return rule;
            }
        }
        
        return null;
    }

    /**
     * 检查规则是否匹配
     */
    private boolean matches(MockRule rule, String url, String method) {
        if (rule.getMatchCondition() == null) {
            return false;
        }

        // 检查HTTP方法
        String ruleMethod = rule.getMatchCondition().getMethod();
        if (ruleMethod != null && !ruleMethod.isEmpty()) {
            if (!ruleMethod.equalsIgnoreCase(method)) {
                return false;
            }
        }

        // 检查URL匹配
        String pattern = rule.getMatchCondition().getUrlPattern();
        if (pattern == null || pattern.isEmpty()) {
            return false;
        }

        try {
            return switch (rule.getMatchCondition().getType()) {
                case EQUALS -> url.equals(pattern);
                case CONTAINS -> url.contains(pattern);
                case STARTS_WITH -> url.startsWith(pattern);
                case ENDS_WITH -> url.endsWith(pattern);
                case REGEX -> url.matches(pattern);
                default -> false;
            };
        } catch (Exception e) {
            logError("Error matching rule: " + rule.getName(), e);
            return false;
        }
    }

    /**
     * 生成Mock响应
     */
    private MockResponse generateMockResponse(MockContext context, MockRule rule) {
        ResponseConfig config = rule.getResponseConfig();
        if (config == null) {
            return MockResponse.error("No response configuration");
        }

        return switch (config.getType()) {
            case STATIC -> generateStaticResponse(config);
            case PYTHON_SCRIPT -> generatePythonScriptResponse(context, config);
            case PROXY_FORWARD -> generateProxyForwardResponse(context, config);
            default -> MockResponse.error("Unknown response type");
        };
    }

    /**
     * 生成代理转发响应
     */
    private MockResponse generateProxyForwardResponse(MockContext context, ResponseConfig config) {
        try {
            return proxyForwardHandler.handle(context, config);
        } catch (Exception e) {
            logError("Error forwarding request to proxy", e);
            return MockResponse.error("Proxy forward failed: " + e.getMessage());
        }
    }

    /**
     * 生成静态响应
     */
    private MockResponse generateStaticResponse(ResponseConfig config) {
        MockResponse response = MockResponse.builder()
                .statusCode(config.getStatusCode())
                .headers(config.getHeaders() != null ? 
                        new HashMap<>(config.getHeaders()) : new HashMap<>())
                .body(config.getBody() != null ? 
                        config.getBody().getBytes(StandardCharsets.UTF_8) : new byte[0])
                .success(true)
                .handlerType("STATIC")
                .build();

        // 设置默认Content-Type
        if (response.getHeader("Content-Type") == null) {
            String body = config.getBody() != null ? config.getBody() : "";
            response.addHeader("Content-Type", HttpUtils.getDefaultContentType(body));
        }

        return response;
    }

    /**
     * 生成Python脚本响应
     */
    private MockResponse generatePythonScriptResponse(MockContext context, ResponseConfig config) {
        try {
            return pythonScriptHandler.handle(context, config);
        } catch (Exception e) {
            logError("Error executing Python script", e);
            return MockResponse.error("Python script execution failed: " + e.getMessage());
        }
    }

    /**
     * 构建BurpSuite HTTP响应
     */
    private HttpResponse buildBurpResponse(MockResponse mockResponse) {
        // 构建HTTP响应字符串
        StringBuilder responseBuilder = new StringBuilder();
        responseBuilder.append("HTTP/1.1 ").append(mockResponse.getStatusCode())
                       .append(" ").append(getStatusText(mockResponse.getStatusCode()))
                       .append("\r\n");
        
        // 添加headers
        if (mockResponse.getHeaders() != null) {
            mockResponse.getHeaders().forEach((name, value) -> {
                responseBuilder.append(name).append(": ").append(value).append("\r\n");
            });
        }
        
        // 添加Content-Length（如果还没有）
        if (mockResponse.getHeader("Content-Length") == null && mockResponse.getBody() != null) {
            responseBuilder.append("Content-Length: ").append(mockResponse.getBody().length).append("\r\n");
        }
        
        responseBuilder.append("\r\n");
        
        // 合并头部和body
        byte[] headerBytes = responseBuilder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] bodyBytes = mockResponse.getBody() != null ? mockResponse.getBody() : new byte[0];
        byte[] fullResponse = new byte[headerBytes.length + bodyBytes.length];
        
        System.arraycopy(headerBytes, 0, fullResponse, 0, headerBytes.length);
        System.arraycopy(bodyBytes, 0, fullResponse, headerBytes.length, bodyBytes.length);
        
        // 使用Burp API创建响应
        return HttpResponse.httpResponse(
            burp.api.montoya.core.ByteArray.byteArray(fullResponse)
        );
    }

    /**
     * 获取状态码文本
     */
    private String getStatusText(int statusCode) {
        return switch (statusCode) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 301 -> "Moved Permanently";
            case 302 -> "Found";
            case 304 -> "Not Modified";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            case 502 -> "Bad Gateway";
            case 503 -> "Service Unavailable";
            default -> "Unknown";
        };
    }

    /**
     * 日志记录
     */
    private void log(String message) {
        if (configStorage.isEnableLogging() && logging != null) {
            logging.logToOutput("[MockHttpHandler] " + message);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, Throwable e) {
        if (logging != null) {
            if (e != null) {
                logging.logToError("[MockHttpHandler] " + message, e);
            } else {
                logging.logToError("[MockHttpHandler] " + message);
            }
        }
    }
}
