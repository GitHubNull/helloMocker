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

    public RuleEditorDialog(Window owner, MontoyaApi api, MockRuleManager ruleManager, MockRule existingRule) {
        super(owner, existingRule == null ? "Add Mock Rule" : "Edit Mock Rule", ModalityType.APPLICATION_MODAL);
        this.api = api;
        this.ruleManager = ruleManager;
        this.existingRule = existingRule;

        initializeUI();
        if (existingRule != null) {
            loadRuleData(existingRule);
        }

        pack();
        setSize(800, 600);
        setLocationRelativeTo(owner);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));

        // 基本信息面板
        add(createBasicInfoPanel(), BorderLayout.NORTH);

        // Tab面板（匹配条件和响应配置）
        JTabbedPane tabbedPane = new JTabbedPane();

        // 匹配条件
        matchConditionPanel = new MatchConditionPanel();
        tabbedPane.addTab("Match Condition", matchConditionPanel);

        // 响应配置
        responseConfigPanel = new ResponseConfigPanel(api);
        tabbedPane.addTab("Response Config", responseConfigPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 按钮面板
        add(createButtonPanel(), BorderLayout.SOUTH);
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
