package org.oxff.hellomocker.ui.actions;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.ui.dialog.RuleEditorDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.List;

/**
 * 规则表格操作逻辑封装
 * 提供删除、编辑、移动、导入导出等操作的标准实现
 *
 * @author oxff
 * @version 1.0
 */
public class RuleTableActions {

    private final Component parentComponent;
    private final MontoyaApi api;
    private final MockRuleManager ruleManager;

    public RuleTableActions(Component parentComponent, MontoyaApi api, MockRuleManager ruleManager) {
        this.parentComponent = parentComponent;
        this.api = api;
        this.ruleManager = ruleManager;
    }

    // ==================== 基本CRUD操作 ====================

    /**
     * 添加新规则
     */
    public void addRule() {
        RuleEditorDialog dialog = new RuleEditorDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                api,
                ruleManager,
                null
        );
        dialog.setVisible(true);
    }

    /**
     * 编辑指定行的规则
     *
     * @param row 表格行索引
     */
    public void editRule(int row) {
        MockRule rule = getRuleAtRow(row);
        if (rule == null) {
            showError("No rule selected for editing.");
            return;
        }

        RuleEditorDialog dialog = new RuleEditorDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                api,
                ruleManager,
                rule
        );
        dialog.setVisible(true);
    }

    /**
     * 删除指定行的规则
     *
     * @param row 表格行索引
     */
    public void deleteRule(int row) {
        MockRule rule = getRuleAtRow(row);
        if (rule == null) {
            showError("No rule selected for deletion.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                "Are you sure you want to delete rule '" + rule.getName() + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            ruleManager.deleteRule(rule.getId());
        }
    }

    /**
     * 复制指定行的规则
     *
     * @param row 表格行索引
     */
    public void duplicateRule(int row) {
        MockRule rule = getRuleAtRow(row);
        if (rule == null) {
            showError("No rule selected for duplication.");
            return;
        }

        MockRule copy = MockRule.builder()
                .name(rule.getName() + " (Copy)")
                .description(rule.getDescription())
                .enabled(false)
                .priority(rule.getPriority())
                .matchCondition(rule.getMatchCondition())
                .responseConfig(rule.getResponseConfig())
                .build();

        ruleManager.addRule(copy);
    }

    // ==================== 移动操作 ====================

    /**
     * 上移规则
     *
     * @param row 当前行索引
     * @return 新的行索引（移动后的位置）
     */
    public int moveUp(int row) {
        if (row <= 0) {
            return row; // 已经在最上面，无法上移
        }

        return swapPriority(row, row - 1);
    }

    /**
     * 下移规则
     *
     * @param row 当前行索引
     * @return 新的行索引（移动后的位置）
     */
    public int moveDown(int row) {
        List<MockRule> rules = ruleManager.getAllRules();
        if (row >= rules.size() - 1) {
            return row; // 已经在最下面，无法下移
        }

        return swapPriority(row, row + 1);
    }

    /**
     * 交换两条规则的优先级
     *
     * @param row1 第一个规则的行索引
     * @param row2 第二个规则的行索引
     * @return row1 的新位置
     */
    private int swapPriority(int row1, int row2) {
        List<MockRule> rules = ruleManager.getAllRules();

        if (row1 < 0 || row1 >= rules.size() || row2 < 0 || row2 >= rules.size()) {
            return row1;
        }

        MockRule rule1 = rules.get(row1);
        MockRule rule2 = rules.get(row2);

        // 交换优先级
        int tempPriority = rule1.getPriority();
        rule1.setPriority(rule2.getPriority());
        rule2.setPriority(tempPriority);

        // 更新规则
        ruleManager.updateRule(rule1);
        ruleManager.updateRule(rule2);

        return row2;
    }

    // ==================== 导入导出操作 ====================

    /**
     * 导入规则
     */
    public void importRules() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Mock Rules");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON Files", "json"));

        int result = fileChooser.showOpenDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                Path path = fileChooser.getSelectedFile().toPath();
                int count = ruleManager.importRules(path);
                showInfo("Rules imported successfully!\n\nFile: " + path.toString() + "\nImported: " + count + " rules");
            } catch (Exception ex) {
                showError("Failed to import rules: " + ex.getMessage());
            }
        }
    }

    /**
     * 导出规则
     */
    public void exportRules() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Mock Rules");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON Files", "json"));

        int result = fileChooser.showSaveDialog(parentComponent);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    filePath += ".json";
                    selectedFile = new File(filePath);
                }

                Path path = selectedFile.toPath();
                ruleManager.exportRules(path);

                showInfo("Rules exported successfully!\n\nFile: " + selectedFile.getAbsolutePath() +
                        "\nRules: " + ruleManager.getAllRules().size());
            } catch (Exception ex) {
                showError("Failed to export rules: " + ex.getMessage());
            }
        }
    }

    // ==================== 搜索操作 ====================

    /**
     * 搜索规则
     *
     * @param searchText 搜索文本
     * @return 匹配的规则列表
     */
    public List<MockRule> searchRules(String searchText) {
        if (searchText == null || searchText.trim().isEmpty()) {
            return ruleManager.getAllRules();
        }

        String lowerSearchText = searchText.toLowerCase().trim();
        return ruleManager.getAllRules().stream()
                .filter(rule -> {
                    String name = rule.getName() != null ? rule.getName().toLowerCase() : "";
                    String pattern = "";
                    if (rule.getMatchCondition() != null && rule.getMatchCondition().getUrlPattern() != null) {
                        pattern = rule.getMatchCondition().getUrlPattern().toLowerCase();
                    }
                    return name.contains(lowerSearchText) || pattern.contains(lowerSearchText);
                })
                .toList();
    }

    // ==================== 工具方法 ====================

    /**
     * 获取指定行的规则
     *
     * @param row 表格行索引
     * @return MockRule 对象，如果无效则返回 null
     */
    public MockRule getRuleAtRow(int row) {
        if (row < 0) {
            return null;
        }

        List<MockRule> rules = ruleManager.getAllRules();
        if (row >= rules.size()) {
            return null;
        }

        return rules.get(row);
    }

    /**
     * 获取规则总数
     */
    public int getRuleCount() {
        return ruleManager.getAllRules().size();
    }

    /**
     * 切换规则的启用状态
     *
     * @param row 表格行索引
     */
    public void toggleRuleEnabled(int row) {
        MockRule rule = getRuleAtRow(row);
        if (rule != null) {
            ruleManager.toggleRuleEnabled(rule.getId());
        }
    }

    // ==================== 对话框工具 ====================

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                message,
                "Information",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                SwingUtilities.getWindowAncestor(parentComponent),
                message,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}