package org.oxff.hellomocker.menu;

import burp.api.montoya.MontoyaApi;
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
import org.oxff.hellomocker.ui.dialog.RuleEditorDialog;
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

    private final MontoyaApi api;
    private final MockRuleManager ruleManager;
    private final ConfigStorage configStorage;
    private final Logging logging;

    public SendToMockContextMenu(MontoyaApi api, MockRuleManager ruleManager, ConfigStorage configStorage, Logging logging) {
        this.api = api;
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
     * 根据配置决定是否显示编辑弹窗
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

            // 根据配置决定是否显示编辑弹窗
            if (configStorage.isShowSendToMockDialog()) {
                // 显示编辑弹窗
                showEditDialog(rule, url);
            } else {
                // 直接创建规则（原有逻辑）
                createRuleDirectly(rule, url);
            }

        } catch (Exception e) {
            logError("Error creating Mock rule from Proxy History", e);
            showError("Error creating Mock rule: " + e.getMessage());
        }
    }

    /**
     * 显示规则编辑弹窗
     */
    private void showEditDialog(MockRule rule, String url) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 传 null 作为 existingRule，让对话框走 addRule 分支
                RuleEditorDialog dialog = new RuleEditorDialog(
                        null,
                        api,
                        ruleManager,
                        null
                );
                // 预填充从 Proxy History 提取的数据
                dialog.preFillRuleData(rule);
                dialog.setVisible(true);

                if (dialog.isSaved()) {
                    log("Created Mock rule from dialog");
                    JOptionPane.showMessageDialog(
                            null,
                            "Mock rule created successfully!\n\n" +
                            "URL: " + url,
                            "Send to Mock",
                            JOptionPane.INFORMATION_MESSAGE
                    );
                }
            } catch (Exception e) {
                logError("Error showing edit dialog", e);
                showError("Failed to open edit dialog: " + e.getMessage());
            }
        });
    }

    /**
     * 直接创建规则（不显示弹窗）
     */
    private void createRuleDirectly(MockRule rule, String url) {
        // 添加到规则管理器
        if (ruleManager.addRule(rule)) {
            log("Created Mock rule from Proxy History: " + rule.getName());
            
            // 显示成功提示
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(
                        null,
                        "Mock rule created successfully!\n\n" +
                        "Name: " + rule.getName() + "\n" +
                        "URL: " + url + "\n\n" +
                        "You can edit this rule in the HelloMocker tab.",
                        "Send to Mock",
                        JOptionPane.INFORMATION_MESSAGE
                );
            });
        } else {
            logError("Failed to create Mock rule: " + rule.getName(), null);
            showError("Failed to create Mock rule. Maximum number of rules may have been reached.");
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
     * 优化：移除查询参数，如果路径过长保留最后部分
     */
    private String generateRuleName(String method, String path) {
        // 移除查询参数
        String cleanPath = path.split("\\?")[0];
        
        // 如果路径过长，保留最后30个字符（通常更有标识性）
        if (cleanPath.length() > 30) {
            int startIndex = Math.max(0, cleanPath.length() - 27);
            cleanPath = "..." + cleanPath.substring(startIndex);
        }
        
        // 移除特殊字符
        cleanPath = cleanPath.replaceAll("[^a-zA-Z0-9/_-]", "_");
        
        return method + " " + cleanPath + " (Auto)";
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
