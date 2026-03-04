package org.oxff.hellomocker.ui.component;

import org.oxff.hellomocker.model.MatchCondition;

import javax.swing.*;
import java.awt.*;

/**
 * 匹配条件配置面板
 * 用于配置URL匹配规则
 *
 * @author oxff
 * @version 1.0
 */
public class MatchConditionPanel extends JPanel {

    private JComboBox<String> matchTypeCombo;
    private JTextField urlPatternField;
    private JComboBox<String> methodCombo;

    public MatchConditionPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 匹配类型
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Match Type:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        String[] matchTypes = {"CONTAINS", "EQUALS", "REGEX", "STARTS_WITH", "ENDS_WITH"};
        matchTypeCombo = new JComboBox<>(matchTypes);
        matchTypeCombo.setSelectedItem("CONTAINS");
        add(matchTypeCombo, gbc);

        // URL模式
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("URL Pattern:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        urlPatternField = new JTextField(40);
        urlPatternField.setToolTipText("URL pattern to match (e.g., /api/admin or https://example.com/api)");
        add(urlPatternField, gbc);

        // HTTP方法
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        add(new JLabel("HTTP Method:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        String[] methods = {"(Any)", "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS"};
        methodCombo = new JComboBox<>(methods);
        methodCombo.setSelectedItem("(Any)");
        add(methodCombo, gbc);

        // 帮助文本
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea helpText = new JTextArea(
                "Match Type Help:\n" +
                "- CONTAINS: URL contains the pattern\n" +
                "- EQUALS: URL exactly equals the pattern\n" +
                "- REGEX: URL matches the regular expression\n" +
                "- STARTS_WITH: URL starts with the pattern\n" +
                "- ENDS_WITH: URL ends with the pattern\n\n" +
                "Examples:\n" +
                "- CONTAINS '/api/admin' matches any URL containing '/api/admin'\n" +
                "- REGEX '.*\\/user\\/\\d+$' matches /user/123, /user/456, etc."
        );
        helpText.setEditable(false);
        helpText.setBackground(getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        add(new JScrollPane(helpText), gbc);
    }

    /**
     * 获取匹配条件
     */
    public MatchCondition getMatchCondition() {
        String matchType = (String) matchTypeCombo.getSelectedItem();
        String urlPattern = urlPatternField.getText().trim();
        String method = (String) methodCombo.getSelectedItem();

        if (urlPattern.isEmpty()) {
            return null;
        }

        MatchCondition.MatchType type = MatchCondition.parseMatchType(matchType);

        return MatchCondition.builder()
                .type(type)
                .urlPattern(urlPattern)
                .method("(Any)".equals(method) ? null : method)
                .build();
    }

    /**
     * 设置匹配条件
     */
    public void setMatchCondition(MatchCondition condition) {
        if (condition == null) {
            return;
        }

        if (condition.getType() != null) {
            matchTypeCombo.setSelectedItem(condition.getType().name());
        }

        if (condition.getUrlPattern() != null) {
            urlPatternField.setText(condition.getUrlPattern());
        }

        if (condition.getMethod() != null) {
            methodCombo.setSelectedItem(condition.getMethod());
        } else {
            methodCombo.setSelectedItem("(Any)");
        }
    }
}
