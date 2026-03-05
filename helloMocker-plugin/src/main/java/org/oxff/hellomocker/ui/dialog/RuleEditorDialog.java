package org.oxff.hellomocker.ui.dialog;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MatchCondition;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.ui.component.MatchConditionPanel;
import org.oxff.hellomocker.ui.component.ResponseConfigPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 规则编辑器对话框
 * 用于创建和编辑Mock规则
 *
 * @author oxff
 * @version 1.0
 */
public class RuleEditorDialog extends JDialog {

    private final MontoyaApi api;
    private final MockRuleManager ruleManager;
    private final MockRule existingRule;

    private JTextField nameField;
    private JTextArea descriptionArea;
    private JSpinner prioritySpinner;
    private JCheckBox enabledCheckBox;
    private MatchConditionPanel matchConditionPanel;
    private ResponseConfigPanel responseConfigPanel;

    private boolean saved = false;
    private boolean isMaximized = false;
    private Rectangle normalBounds;
    private JButton maximizeButton;
    private Point dragStartPoint;
    private Point dialogStartLocation;

    public RuleEditorDialog(Window owner, MontoyaApi api, MockRuleManager ruleManager, MockRule existingRule) {
        super(owner, existingRule == null ? "Add Mock Rule" : "Edit Mock Rule", ModalityType.APPLICATION_MODAL);
        this.api = api;
        this.ruleManager = ruleManager;
        this.existingRule = existingRule;

        // 移除原生标题栏，使用自定义标题栏
        setUndecorated(true);

        initializeUI();
        if (existingRule != null) {
            loadRuleData(existingRule);
        }

        pack();
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));

        // 创建自定义标题栏（可双击最大化）
        JPanel titleBarPanel = createTitleBarPanel();
        add(titleBarPanel, BorderLayout.NORTH);

        // 内容面板
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        // 添加灰色外边框 + 内边距
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                new EmptyBorder(5, 5, 5, 5)));

        // 基本信息面板
        contentPanel.add(createBasicInfoPanel(), BorderLayout.NORTH);

        // Tab面板（匹配条件和响应配置）
        JTabbedPane tabbedPane = new JTabbedPane();

        // 匹配条件
        matchConditionPanel = new MatchConditionPanel();
        tabbedPane.addTab("Match Condition", matchConditionPanel);

        // 响应配置
        responseConfigPanel = new ResponseConfigPanel(api);
        tabbedPane.addTab("Response Config", responseConfigPanel);

        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        // 按钮面板
        contentPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTitleBarPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY));
        // 使用更深的灰色背景，与内容面板形成统一整体
        panel.setBackground(new Color(220, 220, 220));
        panel.setOpaque(true);

        // 标题标签
        JLabel titleLabel = new JLabel(getTitle(), SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setBorder(new EmptyBorder(5, 10, 5, 10));
        panel.add(titleLabel, BorderLayout.CENTER);

        // 添加鼠标拖动功能
        MouseAdapter dragAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!isMaximized) {
                    dragStartPoint = e.getPoint();
                    dialogStartLocation = getLocation();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!isMaximized && dragStartPoint != null) {
                    Point currentLocation = e.getLocationOnScreen();
                    int x = currentLocation.x - dragStartPoint.x;
                    int y = currentLocation.y - dragStartPoint.y;
                    setLocation(x, y);
                }
            }
        };
        panel.addMouseListener(dragAdapter);
        panel.addMouseMotionListener(dragAdapter);
        titleLabel.addMouseListener(dragAdapter);
        titleLabel.addMouseMotionListener(dragAdapter);

        // 添加双击监听器
        MouseAdapter doubleClickAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleMaximize();
                }
            }
        };
        panel.addMouseListener(doubleClickAdapter);
        titleLabel.addMouseListener(doubleClickAdapter);

        // 添加提示工具提示
        panel.setToolTipText("Double-click to maximize/restore, drag to move");
        titleLabel.setToolTipText("Double-click to maximize/restore, drag to move");

        // 窗口控制按钮面板（右侧）
        JPanel buttonPanel = createWindowControlButtons();
        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createWindowControlButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panel.setOpaque(false);

        // 最小化按钮
        JButton minimizeButton = createControlButton("−", "Minimize");
        minimizeButton.addActionListener(e -> minimizeWindow());
        panel.add(minimizeButton);

        // 最大化/恢复按钮
        maximizeButton = createControlButton("□", "Maximize");
        maximizeButton.addActionListener(e -> toggleMaximize());
        panel.add(maximizeButton);

        // 关闭按钮
        JButton closeButton = createControlButton("×", "Close");
        closeButton.addActionListener(e -> dispose());
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(new Color(232, 17, 35));
                closeButton.setForeground(Color.WHITE);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(UIManager.getColor("Panel.background"));
                closeButton.setForeground(UIManager.getColor("Label.foreground"));
            }
        });
        panel.add(closeButton);

        return panel;
    }

    private JButton createControlButton(String text, String tooltip) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(45, 30));
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setToolTipText(tooltip);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBackground(UIManager.getColor("Panel.background"));
        button.setForeground(UIManager.getColor("Label.foreground"));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.getBackground().equals(new Color(232, 17, 35))) {
                    return;
                }
                button.setBackground(new Color(200, 200, 200));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (button.getBackground().equals(new Color(232, 17, 35))) {
                    return;
                }
                button.setBackground(UIManager.getColor("Panel.background"));
            }
        });

        return button;
    }

    private void minimizeWindow() {
        // JDialog 没有 setExtendedState 方法，使用 setVisible(false) 模拟最小化
        setVisible(false);
    }

    private void toggleMaximize() {
        if (isMaximized) {
            // 恢复原始大小
            if (normalBounds != null) {
                setBounds(normalBounds);
            }
            isMaximized = false;
            // 更新按钮图标为最大化
            if (maximizeButton != null) {
                maximizeButton.setText("□");
                maximizeButton.setToolTipText("Maximize");
            }
        } else {
            // 保存当前状态
            normalBounds = getBounds();

            // 最大化到可视区域
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            Rectangle maxBounds = ge.getMaximumWindowBounds();
            setBounds(maxBounds);
            isMaximized = true;
            // 更新按钮图标为恢复
            if (maximizeButton != null) {
                maximizeButton.setText("❐");
                maximizeButton.setToolTipText("Restore");
            }
        }
    }

    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Basic Information"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 规则名称
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Name:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(30);
        panel.add(nameField, gbc);

        // 启用状态
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        enabledCheckBox = new JCheckBox("Enabled");
        enabledCheckBox.setSelected(true);
        panel.add(enabledCheckBox, gbc);

        // 优先级
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Priority:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        SpinnerNumberModel priorityModel = new SpinnerNumberModel(100, 1, 9999, 10);
        prioritySpinner = new JSpinner(priorityModel);
        panel.add(prioritySpinner, gbc);

        // 描述
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        descriptionArea = new JTextArea(3, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        panel.add(scrollPane, gbc);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));

        // 保存按钮
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this::saveRule);
        panel.add(saveButton);

        // 取消按钮
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        panel.add(cancelButton);

        // 设置默认按钮
        getRootPane().setDefaultButton(saveButton);

        return panel;
    }

    private void loadRuleData(MockRule rule) {
        nameField.setText(rule.getName());
        descriptionArea.setText(rule.getDescription());
        prioritySpinner.setValue(rule.getPriority());
        enabledCheckBox.setSelected(rule.isEnabled());

        if (rule.getMatchCondition() != null) {
            matchConditionPanel.setMatchCondition(rule.getMatchCondition());
        }

        if (rule.getResponseConfig() != null) {
            responseConfigPanel.setResponseConfig(rule.getResponseConfig());
        }
    }

    /**
     * 预填充规则数据（用于从右键菜单创建时）
     * 此方法在 existingRule 为 null 时调用，用于预填充表单数据
     * 注意：调用此方法后规则仍会被当作新规则处理（走 addRule 分支）
     */
    public void preFillRuleData(MockRule rule) {
        if (rule != null) {
            nameField.setText(rule.getName());
            descriptionArea.setText(rule.getDescription());
            prioritySpinner.setValue(rule.getPriority());
            enabledCheckBox.setSelected(rule.isEnabled());

            if (rule.getMatchCondition() != null) {
                matchConditionPanel.setMatchCondition(rule.getMatchCondition());
            }

            if (rule.getResponseConfig() != null) {
                responseConfigPanel.setResponseConfig(rule.getResponseConfig());
            }
        }
    }

    private void saveRule(ActionEvent e) {
        // 验证输入
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Rule name is required!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        // 获取匹配条件
        MatchCondition matchCondition = matchConditionPanel.getMatchCondition();
        if (matchCondition == null || !matchCondition.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid match condition! URL pattern is required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 获取响应配置
        ResponseConfig responseConfig = responseConfigPanel.getResponseConfig();
        if (responseConfig == null || !responseConfig.isValid()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid response configuration!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 创建或更新规则
        MockRule rule;
        if (existingRule != null) {
            rule = existingRule;
            rule.setName(name);
            rule.setDescription(descriptionArea.getText().trim());
            rule.setPriority((Integer) prioritySpinner.getValue());
            rule.setEnabled(enabledCheckBox.isSelected());
            rule.setMatchCondition(matchCondition);
            rule.setResponseConfig(responseConfig);
            rule.touch();

            if (ruleManager.updateRule(rule)) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to update rule!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            rule = MockRule.builder()
                    .name(name)
                    .description(descriptionArea.getText().trim())
                    .priority((Integer) prioritySpinner.getValue())
                    .enabled(enabledCheckBox.isSelected())
                    .matchCondition(matchCondition)
                    .responseConfig(responseConfig)
                    .build();

            if (ruleManager.addRule(rule)) {
                saved = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to add rule! Maximum number of rules may have been reached.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
