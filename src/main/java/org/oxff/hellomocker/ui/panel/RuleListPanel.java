package org.oxff.hellomocker.ui.panel;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.MockRule;
import org.oxff.hellomocker.service.MockRuleManager;
import org.oxff.hellomocker.storage.ConfigStorage;
import org.oxff.hellomocker.ui.actions.RuleTableActions;
import org.oxff.hellomocker.ui.component.MockRuleTable;
import org.oxff.hellomocker.ui.component.RuleToolbar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
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
    @SuppressWarnings("unused")
    private final ConfigStorage configStorage;
    private final RuleTableActions tableActions;

    private RuleToolbar toolbar;
    private MockRuleTable ruleTable;
    private DefaultTableModel tableModel;

    public RuleListPanel(MontoyaApi api, MockRuleManager ruleManager, ConfigStorage configStorage) {
        this.api = api;
        this.ruleManager = ruleManager;
        this.configStorage = configStorage;
        this.tableActions = new RuleTableActions(this, api, ruleManager);

        ruleManager.addRuleChangeListener(this);
        initializeUI();
        refreshRules();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 工具栏
        toolbar = new RuleToolbar();
        add(toolbar, BorderLayout.NORTH);

        // 规则表格
        add(createRuleTablePanel(), BorderLayout.CENTER);

        // 绑定事件
        bindToolbarEvents();
        bindTableEvents();
    }

    private JScrollPane createRuleTablePanel() {
        String[] columnNames = {"#", "Name", "URL Pattern", "Type", "Priority", "Enabled"};

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // 只有Enabled列可编辑
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex == 5 ? Boolean.class : String.class;
            }
        };

        // 监听Enabled列变化
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 5) {
                int row = e.getFirstRow();
                if (row >= 0) {
                    tableActions.toggleRuleEnabled(row);
                }
            }
        });

        ruleTable = new MockRuleTable(tableModel, api, ruleManager);
        ruleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ruleTable.setRowHeight(25);

        setupColumnWidths();
        setupDoubleClickHandler();

        JScrollPane scrollPane = new JScrollPane(ruleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mock Rules"));

        return scrollPane;
    }

    private void setupColumnWidths() {
        ruleTable.getColumnModel().getColumn(0).setPreferredWidth(30);
        ruleTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        ruleTable.getColumnModel().getColumn(2).setPreferredWidth(250);
        ruleTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        ruleTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        ruleTable.getColumnModel().getColumn(5).setPreferredWidth(60);
    }

    private void setupDoubleClickHandler() {
        ruleTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = ruleTable.getSelectedRow();
                    if (row >= 0) {
                        tableActions.editRule(row);
                    }
                }
            }
        });
    }

    private void bindToolbarEvents() {
        // 主操作按钮
        toolbar.getAddButton().addActionListener(e -> tableActions.addRule());
        toolbar.getImportButton().addActionListener(e -> tableActions.importRules());
        toolbar.getExportButton().addActionListener(e -> tableActions.exportRules());

        // 规则操作按钮
        toolbar.getEditButton().addActionListener(e -> {
            int row = ruleTable.getSelectedRow();
            if (row >= 0) {
                tableActions.editRule(row);
            }
        });

        toolbar.getDeleteButton().addActionListener(e -> {
            int row = ruleTable.getSelectedRow();
            if (row >= 0) {
                tableActions.deleteRule(row);
            }
        });

        // 移动按钮
        toolbar.getMoveUpButton().addActionListener(e -> {
            int row = ruleTable.getSelectedRow();
            if (row >= 0) {
                int newRow = tableActions.moveUp(row);
                ruleTable.setRowSelectionInterval(newRow, newRow);
                updateToolbarButtonStates();
            }
        });

        toolbar.getMoveDownButton().addActionListener(e -> {
            int row = ruleTable.getSelectedRow();
            if (row >= 0) {
                int newRow = tableActions.moveDown(row);
                ruleTable.setRowSelectionInterval(newRow, newRow);
                updateToolbarButtonStates();
            }
        });

        // 搜索按钮
        toolbar.getSearchButton().addActionListener(e -> performSearch());
        toolbar.getSearchField().addActionListener(e -> performSearch());
    }

    private void bindTableEvents() {
        // 监听表格选择变化，更新按钮状态
        ruleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateToolbarButtonStates();
                }
            }
        });
    }

    private void updateToolbarButtonStates() {
        int selectedRow = ruleTable.getSelectedRow();
        int totalRows = tableModel.getRowCount();
        toolbar.updateButtonStates(selectedRow, totalRows);
    }

    private void performSearch() {
        String searchText = toolbar.getSearchField().getText();
        List<MockRule> filteredRules = tableActions.searchRules(searchText);
        updateTableData(filteredRules);
    }

    public void refreshRules() {
        List<MockRule> rules = ruleManager.getAllRules();
        updateTableData(rules);
    }

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

        // 刷新后更新按钮状态
        updateToolbarButtonStates();
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
        ruleManager.removeRuleChangeListener(this);
    }
}