package org.oxff.hellomocker.menu;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.InvocationType;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.model.MatchCondition;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.util.HttpUtils;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Send to Mock右键菜单提供器
 * 在BurpSuite Proxy History等位置提供右键菜单
 *
 * @author oxff
 * @version 1.0
 */
public class SendToMockContextMenu implements ContextMenuItemsProvider {

    private final MockRuleManager ruleManager;
    private final ConfigStorage configStorage;
    private final Logging logging;

    public SendToMockContextMenu(MockRuleManager ruleManager, ConfigStorage configStorage, Logging logging) {
        this.ruleManager = ruleManager;
        this.configStorage = configStorage;
        this.logging = logging;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        // 只在特定的调用类型显示菜单
        // 支持：Proxy History, Logger, 等HTTP日志界面
        InvocationType invocationType = event.invocationType();
        ToolType toolType = event.toolType();

        // 检查是否有选中的请求
        List<HttpRequestResponse> selectedRequestResponses = event.selectedRequestResponses();
        if (selectedRequestResponses == null || selectedRequestResponses.isEmpty()) {
            return Collections.emptyList();
        }

        // 创建菜单项
        JMenuItem sendToMockItem = new JMenuItem("Send to Mock");
        sendToMockItem.setToolTipText("Create a Mock rule from this request/response");
        
        // 添加图标（如果有的话）
        // sendToMockItem.setIcon(new ImageIcon(getClass().getResource("/icons/mock-icon.png")));

        // 添加点击事件
        sendToMockItem.addActionListener(e -> {
            handleSendToMock(selectedRequestResponses);
        });

        return List.of(sendToMockItem);
    }

    /**
     * 处理Send to Mock操作
     */
    private void handleSendToMock(List<HttpRequestResponse> requestResponses) {
        if (requestResponses.isEmpty()) {
            return;
        }

        // 目前只处理第一个选中的请求
        HttpRequestResponse requestResponse = requestResponses.get(0);

        try {
            // 提取请求信息
            String url = requestResponse.request().url();
            String method = requestResponse.request().method();
            String path = HttpUtils.extractPath(url);

            // 获取响应（如果有）
            String responseBody = "";
            int statusCode = 200;
            java.util.Map<String, String> responseHeaders = new java.util.HashMap<>();

            if (requestResponse.response() != null) {
                statusCode = requestResponse.response().statusCode();
                responseBody = getResponseBodyString(requestResponse.response());

                // 提取响应头
                requestResponse.response().headers().forEach(header -> {
                    responseHeaders.put(header.name(), header.value());
                });
            }

            // 创建规则名称
            String ruleName = generateRuleName(method, path);

            // 创建匹配条件
            MatchCondition matchCondition = MatchCondition.builder()
                    .type(MatchCondition.MatchType.CONTAINS)
                    .urlPattern(url)
                    .method(method)
                    .build();

            // 创建响应配置（静态响应）
            ResponseConfig responseConfig = ResponseConfig.builder()
                    .type(ResponseConfig.ResponseType.STATIC)
                    .statusCode(statusCode)
                    .headers(responseHeaders)
                    .body(responseBody)
                    .build();

            // 创建Mock规则
            MockRule rule = MockRule.builder()
                    .name(ruleName)
                    .description("Created from Proxy History: " + method + " " + path)
                    .enabled(true)
                    .priority(100)
                    .matchCondition(matchCondition)
                    .responseConfig(responseConfig)
                    .build();

            // 添加到规则管理器
            if (ruleManager.addRule(rule)) {
                log("Created Mock rule from Proxy History: " + ruleName);
                
                // 显示成功提示
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "Mock rule created successfully!\n\n" +
                            "Name: " + ruleName + "\n" +
                            "URL: " + url + "\n\n" +
                            "You can edit this rule in the HelloMocker tab.",
                            "Send to Mock",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                });
            } else {
                logError("Failed to create Mock rule: " + ruleName, null);
                showError("Failed to create Mock rule. Maximum number of rules may have been reached.");
            }

        } catch (Exception e) {
            logError("Error creating Mock rule from Proxy History", e);
            showError("Error creating Mock rule: " + e.getMessage());
        }
    }

    /**
     * 从响应头中提取字符编码
     *
     * @param response HTTP响应
     * @return 检测到的字符编码，如果没有指定则返回UTF-8
     */
    private Charset extractCharsetFromResponse(HttpResponse response) {
        if (response == null) {
            return StandardCharsets.UTF_8;
        }

        // 从Content-Type头中提取charset
        String contentType = null;
        for (var header : response.headers()) {
            if (header.name().equalsIgnoreCase("Content-Type")) {
                contentType = header.value();
                break;
            }
        }

        if (contentType != null) {
            // 尝试匹配 charset=xxx 或 charset="xxx"
            Pattern pattern = Pattern.compile("charset\\s*=\\s*['\"]?([^;'\"\\s]+)['\"]?", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(contentType);
            if (matcher.find()) {
                String charsetName = matcher.group(1).trim();
                try {
                    return Charset.forName(charsetName);
                } catch (Exception e) {
                    // 如果charset名称无效，使用UTF-8
                    log("Invalid charset in response: " + charsetName + ", using UTF-8");
                }
            }
        }

        // 默认使用UTF-8
        return StandardCharsets.UTF_8;
    }

    /**
     * 安全地获取响应体字符串，正确处理中文编码
     *
     * @param response HTTP响应
     * @return 正确编码的响应体字符串
     */
    private String getResponseBodyString(HttpResponse response) {
        if (response == null || response.body() == null) {
            return "";
        }

        try {
            // 检测字符编码
            Charset charset = extractCharsetFromResponse(response);

            // 获取原始字节并转换为字符串
            byte[] bodyBytes = response.body().getBytes();
            if (bodyBytes.length == 0) {
                return "";
            }

            String decoded = new String(bodyBytes, charset);
            log("Decoded response body with charset: " + charset.name() + ", length: " + decoded.length());
            return decoded;
        } catch (Exception e) {
            logError("Error decoding response body, falling back to bodyToString()", e);
            // 如果解码失败，回退到原来的方法
            return response.bodyToString();
        }
    }

    /**
     * 生成规则名称
     */
    private String generateRuleName(String method, String path) {
        // 简化路径
        String simplifiedPath = path;
        if (path.length() > 30) {
            simplifiedPath = path.substring(0, 27) + "...";
        }
        
        // 移除特殊字符
        simplifiedPath = simplifiedPath.replaceAll("[^a-zA-Z0-9/_-]", "_");
        
        return method + " " + simplifiedPath + " (Auto)";
    }

    /**
     * 显示错误对话框
     */
    private void showError(String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        });
    }

    /**
     * 日志记录
     */
    private void log(String message) {
        if (configStorage.isEnableLogging() && logging != null) {
            logging.logToOutput("[SendToMock] " + message);
        }
    }

    /**
     * 错误日志记录
     */
    private void logError(String message, Throwable e) {
        if (logging != null) {
            if (e != null) {
                logging.logToError("[SendToMock] " + message, e);
            } else {
                logging.logToError("[SendToMock] " + message);
            }
        }
    }
}
