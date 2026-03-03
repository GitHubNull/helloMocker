package org.oxff.hellomocker.ui;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.ui.panel.ConfigPanel;
import org.oxff.hellomocker.ui.panel.RuleListPanel;
import org.oxff.hellomocker.ui.util.FlatLafPanel;

import javax.swing.*;
import java.awt.*;

/**
 * 主Tab面板
 * 作为BurpSuite中插件的主界面容器
 *
 * @author oxff
 * @version 1.0
 */
public class MainTabPanel extends FlatLafPanel {

    private final MontoyaApi api;
    private final MockRuleManager ruleManager;
    private final ConfigStorage configStorage;

    private JTabbedPane tabbedPane;
    private RuleListPanel ruleListPanel;
    private ConfigPanel configPanel;

    public MainTabPanel(MontoyaApi api, MockRuleManager ruleManager, ConfigStorage configStorage) {
        this.api = api;
        this.ruleManager = ruleManager;
        this.configStorage = configStorage;

        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 创建Tab面板
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        // 规则列表面板
        ruleListPanel = new RuleListPanel(api, ruleManager, configStorage);
        tabbedPane.addTab("Mock Rules", ruleListPanel);
        tabbedPane.setToolTipTextAt(0, "Manage Mock Rules");

        // 配置面板
        configPanel = new ConfigPanel(api, configStorage);
        tabbedPane.addTab("Settings", configPanel);
        tabbedPane.setToolTipTextAt(1, "Plugin Settings");

        add(tabbedPane, BorderLayout.CENTER);

        // 添加状态栏
        add(createStatusBar(), BorderLayout.SOUTH);
    }

    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        JLabel statusLabel = new JLabel("HelloMocker Extension Loaded");
        statusBar.add(statusLabel, BorderLayout.WEST);

        JLabel versionLabel = new JLabel("v1.0");
        versionLabel.setForeground(Color.GRAY);
        statusBar.add(versionLabel, BorderLayout.EAST);

        return statusBar;
    }

    /**
     * 切换到规则列表面板
     */
    public void showRuleListPanel() {
        tabbedPane.setSelectedIndex(0);
    }

    /**
     * 切换到配置面板
     */
    public void showConfigPanel() {
        tabbedPane.setSelectedIndex(1);
    }

    /**
     * 刷新规则列表
     */
    public void refreshRuleList() {
        if (ruleListPanel != null) {
            ruleListPanel.refreshRules();
        }
    }
}
