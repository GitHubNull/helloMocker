package org.oxff.hellomocker.ui.component;

import javax.swing.*;
import java.awt.*;

/**
 * 规则管理工具栏组件
 * 包含所有规则操作按钮：Add、Import、Export、Delete、Edit、Move Up、Move Down
 *
 * @author oxff
 * @version 1.0
 */
public class RuleToolbar extends JPanel {

    private JButton addButton;
    private JButton importButton;
    private JButton exportButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton moveUpButton;
    private JButton moveDownButton;
    private JTextField searchField;
    private JButton searchButton;

    public RuleToolbar() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));

        // 左侧操作按钮面板
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(createMainActionsPanel());
        leftPanel.add(createRuleActionsPanel());
        leftPanel.add(createMoveActionsPanel());

        add(leftPanel, BorderLayout.WEST);

        // 右侧搜索面板
        add(createSearchPanel(), BorderLayout.EAST);
    }

    /**
     * 创建主操作按钮面板（Add/Import/Export）
     */
    private JPanel createMainActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        addButton = new JButton("Add");
        addButton.setToolTipText("Add a new Mock rule");
        panel.add(addButton);

        importButton = new JButton("Import");
        importButton.setToolTipText("Import rules from file");
        panel.add(importButton);

        exportButton = new JButton("Export");
        exportButton.setToolTipText("Export rules to file");
        panel.add(exportButton);

        return panel;
    }

    /**
     * 创建规则操作按钮面板（Edit/Delete）
     */
    private JPanel createRuleActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        editButton = new JButton("Edit");
        editButton.setToolTipText("Edit selected rule");
        editButton.setEnabled(false);
        panel.add(editButton);

        deleteButton = new JButton("Delete");
        deleteButton.setToolTipText("Delete selected rule");
        deleteButton.setEnabled(false);
        panel.add(deleteButton);

        return panel;
    }

    /**
     * 创建移动操作按钮面板（Move Up/Move Down）
     */
    private JPanel createMoveActionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        moveUpButton = new JButton("Up");
        moveUpButton.setToolTipText("Move selected rule up");
        moveUpButton.setEnabled(false);
        panel.add(moveUpButton);

        moveDownButton = new JButton("Down");
        moveDownButton.setToolTipText("Move selected rule down");
        moveDownButton.setEnabled(false);
        panel.add(moveDownButton);

        return panel;
    }

    /**
     * 创建搜索面板
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        searchField = new JTextField(20);
        searchField.setToolTipText("Search rules by name or URL pattern");
        panel.add(new JLabel("Search:"));
        panel.add(searchField);

        searchButton = new JButton("Search");
        panel.add(searchButton);

        return panel;
    }

    // ==================== Getters ====================

    public JButton getAddButton() {
        return addButton;
    }

    public JButton getImportButton() {
        return importButton;
    }

    public JButton getExportButton() {
        return exportButton;
    }

    public JButton getEditButton() {
        return editButton;
    }

    public JButton getDeleteButton() {
        return deleteButton;
    }

    public JButton getMoveUpButton() {
        return moveUpButton;
    }

    public JButton getMoveDownButton() {
        return moveDownButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    // ==================== Button State Management ====================

    /**
     * 设置规则操作按钮的启用状态
     *
     * @param hasSelection 是否有选中项
     */
    public void setRuleActionButtonsEnabled(boolean hasSelection) {
        editButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    /**
     * 设置移动按钮的启用状态
     *
     * @param canMoveUp   是否可以上移
     * @param canMoveDown 是否可以下移
     */
    public void setMoveButtonsEnabled(boolean canMoveUp, boolean canMoveDown) {
        moveUpButton.setEnabled(canMoveUp);
        moveDownButton.setEnabled(canMoveDown);
    }

    /**
     * 根据选中行更新所有按钮状态
     *
     * @param selectedRow  当前选中的行索引（-1表示未选中）
     * @param totalRows    总行数
     */
    public void updateButtonStates(int selectedRow, int totalRows) {
        boolean hasSelection = selectedRow >= 0 && totalRows > 0;

        // 设置Edit和Delete按钮
        setRuleActionButtonsEnabled(hasSelection);

        // 设置Move按钮
        if (hasSelection) {
            setMoveButtonsEnabled(selectedRow > 0, selectedRow < totalRows - 1);
        } else {
            setMoveButtonsEnabled(false, false);
        }
    }
}