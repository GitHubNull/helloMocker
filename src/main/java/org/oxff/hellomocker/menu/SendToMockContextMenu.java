package org.oxff.hellomocker.menu;

import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.HttpRequestResponse;
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
import java.util.Collections;
import java.util.List;

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
                responseBody = requestResponse.response().bodyToString();
                
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
