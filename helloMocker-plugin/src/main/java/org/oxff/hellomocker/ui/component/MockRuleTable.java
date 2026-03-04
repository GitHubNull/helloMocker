package org.oxff.hellomocker.ui.component;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.service.MockRuleManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Mock规则表格组件
 * 扩展JTable，添加右键菜单功能
 *
 * @author oxff
 * @version 1.0
 */
public class MockRuleTable extends JTable {

    @SuppressWarnings("unused")
    private final MontoyaApi api;
    private final MockRuleManager ruleManager;
    private JPopupMenu popupMenu;

    public MockRuleTable(DefaultTableModel model, MontoyaApi api, MockRuleManager ruleManager) {
        super(model);
        this.api = api;
        this.ruleManager = ruleManager;

        initializePopupMenu();
        setupTable();
    }

    private void setupTable() {
        // 启用行选择
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(false);

        // 设置表格外观
        setShowGrid(true);
        setGridColor(javax.swing.UIManager.getColor("Table.gridColor"));

        // 添加右键菜单
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopupMenu(e);
                }
            }
        });
    }

    private void initializePopupMenu() {
        popupMenu = new JPopupMenu();

        // 启用/禁用
        JMenuItem toggleItem = new JMenuItem("Enable/Disable");
        toggleItem.addActionListener(this::toggleSelectedRule);
        popupMenu.add(toggleItem);

        popupMenu.addSeparator();

        // 上移
        JMenuItem moveUpItem = new JMenuItem("Move Up");
        moveUpItem.addActionListener(e -> moveRule(-1));
        popupMenu.add(moveUpItem);

        // 下移
        JMenuItem moveDownItem = new JMenuItem("Move Down");
        moveDownItem.addActionListener(e -> moveRule(1));
        popupMenu.add(moveDownItem);

        popupMenu.addSeparator();

        // 删除
        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(this::deleteSelectedRule);
        popupMenu.add(deleteItem);

        popupMenu.addSeparator();

        // 复制
        JMenuItem duplicateItem = new JMenuItem("Duplicate");
        duplicateItem.addActionListener(this::duplicateSelectedRule);
        popupMenu.add(duplicateItem);
    }

    private void showPopupMenu(MouseEvent e) {
        int row = rowAtPoint(e.getPoint());
        if (row >= 0) {
            setRowSelectionInterval(row, row);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /**
     * 切换选中规则的启用状态
     */
    private void toggleSelectedRule(ActionEvent e) {
        int row = getSelectedRow();
        if (row >= 0) {
            var rules = ruleManager.getAllRules();
            if (row < rules.size()) {
                var rule = rules.get(row);
                ruleManager.toggleRuleEnabled(rule.getId());
            }
        }
    }

    /**
     * 删除选中规则
     */
    private void deleteSelectedRule(ActionEvent e) {
        int row = getSelectedRow();
        if (row >= 0) {
            var rules = ruleManager.getAllRules();
            if (row < rules.size()) {
                var rule = rules.get(row);

                int confirm = JOptionPane.showConfirmDialog(
                        SwingUtilities.getWindowAncestor(this),
                        "Are you sure you want to delete rule '\"" + rule.getName() + "\"?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    ruleManager.deleteRule(rule.getId());
                }
            }
        }
    }

    /**
     * 复制选中规则
     */
    private void duplicateSelectedRule(ActionEvent e) {
        int row = getSelectedRow();
        if (row >= 0) {
            var rules = ruleManager.getAllRules();
            if (row < rules.size()) {
                var rule = rules.get(row);

                // 创建副本
                var copy = org.oxff.hellomocker.model.MockRule.builder()
                        .name(rule.getName() + " (Copy)")
                        .description(rule.getDescription())
                        .enabled(false) // 默认禁用
                        .priority(rule.getPriority())
                        .matchCondition(rule.getMatchCondition())
                        .responseConfig(rule.getResponseConfig())
                        .build();

                ruleManager.addRule(copy);
            }
        }
    }

    /**
     * 移动规则（改变优先级）
     */
    private void moveRule(int direction) {
        int row = getSelectedRow();
        if (row < 0) {
            return;
        }

        var rules = ruleManager.getAllRules();
        if (row >= rules.size()) {
            return;
        }

        int newRow = row + direction;
        if (newRow < 0 || newRow >= rules.size()) {
            return;
        }

        var currentRule = rules.get(row);
        var targetRule = rules.get(newRow);

        // 交换优先级
        int tempPriority = currentRule.getPriority();
        currentRule.setPriority(targetRule.getPriority());
        targetRule.setPriority(tempPriority);

        // 更新规则
        ruleManager.updateRule(currentRule);
        ruleManager.updateRule(targetRule);

        // 更新选择
        setRowSelectionInterval(newRow, newRow);
    }
}
