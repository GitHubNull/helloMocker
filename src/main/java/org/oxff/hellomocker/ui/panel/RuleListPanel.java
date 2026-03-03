package org.oxff.hellomocker.ui.panel;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.ui.component.MockRuleTable;
import org.oxff.hellomocker.ui.dialog.RuleEditorDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 规则列表面板
 * 显示和管理所有Mock规则
 *
 * @author oxff
 * @version 1.0
 */
public class RuleListPanel extends JPanel implements MockRuleManager.RuleChangeListener {

    private final MontoyaApi api;
    private final MockRuleManager ruleManager;
    private final ConfigStorage configStorage;

    private MockRuleTable ruleTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public RuleListPanel(MontoyaApi api, MockRuleManager ruleManager, ConfigStorage configStorage) {
        this.api = api;
        this.ruleManager = ruleManager;
        this.configStorage = configStorage;

        // 注册监听器
        ruleManager.addRuleChangeListener(this);

        initializeUI();
        refreshRules();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        add(createToolbar(), BorderLayout.NORTH);

        // 规则表格
        add(createRuleTablePanel(), BorderLayout.CENTER);
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(5, 5));

        // 左侧按钮
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));

        JButton addButton = new JButton("Add Rule");
        addButton.setToolTipText("Add a new Mock rule");
        addButton.addActionListener(e -> showAddRuleDialog());
        leftPanel.add(addButton);

        JButton importButton = new JButton("Import");
        importButton.setToolTipText("Import rules from file");
        importButton.addActionListener(e -> importRules());
        leftPanel.add(importButton);

        JButton exportButton = new JButton("Export");
        exportButton.setToolTipText("Export rules to file");
        exportButton.addActionListener(e -> exportRules());
        leftPanel.add(exportButton);

        toolbar.add(leftPanel, BorderLayout.WEST);

        // 右侧搜索框
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        searchField = new JTextField(20);
        searchField.setToolTipText("Search rules by name or URL pattern");
        rightPanel.add(new JLabel("Search:"));
        rightPanel.add(searchField);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchRules());
        rightPanel.add(searchButton);

        toolbar.add(rightPanel, BorderLayout.EAST);

        return toolbar;
    }

    private JScrollPane createRuleTablePanel() {
        // 表格列名
        String[] columnNames = {"#", "Name", "URL Pattern", "Type", "Priority", "Enabled"};

        // 创建表格模型
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // 只有Enabled列(第5列)可以直接编辑(点击切换)
                return column == 5;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) {
                    return Boolean.class;
                }
                return String.class;
            }
        };

        // 添加表格模型监听器,监听Enabled列的变化
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                // 只处理更新事件且是Enabled列(第5列)
                if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) {
                    int row = e.getFirstRow();
                    if (row >= 0) {
                        toggleRuleEnabled(row);
                    }
                }
            }
        });

        // 创建表格
        ruleTable = new MockRuleTable(tableModel, api, ruleManager);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ruleTable.setRowHeight(25);

        // 设置列宽
        ruleTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        ruleTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ruleTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        ruleTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        ruleTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        ruleTable.getColumnModel().getColumn(5).setPreferredWidth(60);

        // 添加双击编辑功能
        ruleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ruleTable.getSelectedRow();
                    if (row >= 0) {
                        showEditRuleDialog(row);
                    }
                }
            }
        });

        // 创建滚动面板
        JScrollPane scrollPane = new JScrollPane(ruleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mock Rules"));

        return scrollPane;
    }

    /**
     * 刷新规则列表
     */
    public void refreshRules() {
        List<MockRule> rules = ruleManager.getAllRules();
        updateTableData(rules);
    }

    /**
     * 更新表格数据
     */
    private void updateTableData(List<MockRule> rules) {
        tableModel.setRowCount(0);

        int index = 1;
        for (MockRule rule : rules) {
            Object[] row = {
                index++,
                rule.getName(),
                rule.getMatchCondition() != null ? rule.getMatchCondition().getUrlPattern() : "",
                rule.getResponseConfig() != null ? rule.getResponseConfig().getTypeDescription() : "",
                rule.getPriority(),
                rule.isEnabled()
            };
            tableModel.addRow(row);
        }
    }

    /**
     * 切换指定行规则的启用状态
     *
     * @param row 表格行索引
     */
    private void toggleRuleEnabled(int row) {
        List<MockRule> rules = ruleManager.getAllRules();
        if (row >= 0 && row < rules.size()) {
            MockRule rule = rules.get(row);
            // 切换启用状态
            ruleManager.toggleRuleEnabled(rule.getId());
        }
    }

    /**
     * 显示添加规则对话框
     */
    private void showAddRuleDialog() {
        RuleEditorDialog dialog = new RuleEditorDialog(
                SwingUtilities.getWindowAncestor(this),
                api,
                ruleManager,
                null
        );
        dialog.setVisible(true);
    }

    /**
     * 显示编辑规则对话框
     */
    private void showEditRuleDialog(int row) {
        List<MockRule> rules = ruleManager.getAllRules();
        if (row >= 0 && row < rules.size()) {
            MockRule rule = rules.get(row);
            RuleEditorDialog dialog = new RuleEditorDialog(
                    SwingUtilities.getWindowAncestor(this),
                    api,
                    ruleManager,
                    rule
            );
            dialog.setVisible(true);
        }
    }

    /**
     * 搜索规则
     */
    private void searchRules() {
        String searchText = searchField.getText().trim().toLowerCase();
        if (searchText.isEmpty()) {
            refreshRules();
            return;
        }

        List<MockRule> allRules = ruleManager.getAllRules();
        List<MockRule> filteredRules = allRules.stream()
                .filter(rule -> {
                    String name = rule.getName() != null ? rule.getName().toLowerCase() : "";
                    String pattern = "";
                    if (rule.getMatchCondition() != null && rule.getMatchCondition().getUrlPattern() != null) {
                        pattern = rule.getMatchCondition().getUrlPattern().toLowerCase();
                    }
                    return name.contains(searchText) || pattern.contains(searchText);
                })
                .toList();

        updateTableData(filteredRules);
    }

    /**
     * 导入规则
     */
    private void importRules() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Mock Rules");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON Files", "json"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.nio.file.Path path = fileChooser.getSelectedFile().toPath();
                int count = ruleManager.importRules(path);
                JOptionPane.showMessageDialog(this,
                        "Rules imported successfully!\n\nFile: " + path.toString() + "\nImported: " + count + " rules",
                        "Import",
                        JOptionPane.INFORMATION_MESSAGE);
                refreshRules();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to import rules: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 导出规则
     */
    private void exportRules() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Mock Rules");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JSON Files", "json"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File selectedFile = fileChooser.getSelectedFile();
                // 确保文件扩展名为.json
                String filePath = selectedFile.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".json")) {
                    filePath += ".json";
                    selectedFile = new java.io.File(filePath);
                }
                
                java.nio.file.Path path = selectedFile.toPath();
                ruleManager.exportRules(path);
                
                JOptionPane.showMessageDialog(this,
                        "Rules exported successfully!\n\nFile: " + selectedFile.getAbsolutePath() + "\nRules: " + ruleManager.getAllRules().size(),
                        "Export",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export rules: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ==================== RuleChangeListener 实现 ====================

    @Override
    public void onRuleAdded(MockRule rule) {
        refreshRules();
    }

    @Override
    public void onRuleUpdated(MockRule rule) {
        refreshRules();
    }

    @Override
    public void onRuleDeleted(String ruleId) {
        refreshRules();
    }

    @Override
    public void onRulesChanged() {
        refreshRules();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        // 移除监听器避免内存泄漏
        ruleManager.removeRuleChangeListener(this);
    }
}
