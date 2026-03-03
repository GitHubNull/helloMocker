package org.oxff.hellomocker.ui.panel;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.storage.ConfigStorage;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * 配置面板
 * 管理插件的全局配置
 *
 * @author oxff
 * @version 1.0
 */
public class ConfigPanel extends JPanel {

    @SuppressWarnings("unused")
    private final MontoyaApi api;
    private final ConfigStorage configStorage;

    private JTextField pythonPathField;
    private JSpinner timeoutSpinner;
    private JSpinner maxRulesSpinner;
    private JCheckBox enableLoggingCheckBox;
    private JLabel pythonStatusLabel;

    public ConfigPanel(MontoyaApi api, ConfigStorage configStorage) {
        this.api = api;
        this.configStorage = configStorage;

        initializeUI();
        loadConfig();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // 配置表单
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Python路径
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Python Path:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        pythonPathField = new JTextField(30);
        formPanel.add(pythonPathField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browsePythonPath());
        formPanel.add(browseButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        pythonStatusLabel = new JLabel(" ");
        pythonStatusLabel.setForeground(Color.GRAY);
        formPanel.add(pythonStatusLabel, gbc);

        // 测试Python按钮
        gbc.gridx = 2;
        gbc.gridy = 0;
        JButton testButton = new JButton("Test");
        testButton.addActionListener(e -> testPythonPath());
        formPanel.add(testButton, gbc);

        // 超时设置
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        formPanel.add(new JLabel("Script Timeout (ms):"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel timeoutModel = new SpinnerNumberModel(30000, 1000, 300000, 1000);
        timeoutSpinner = new JSpinner(timeoutModel);
        formPanel.add(timeoutSpinner, gbc);

        // 最大规则数
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Max Rules:"), gbc);

        gbc.gridx = 1;
        SpinnerNumberModel maxRulesModel = new SpinnerNumberModel(1000, 10, 10000, 100);
        maxRulesSpinner = new JSpinner(maxRulesModel);
        formPanel.add(maxRulesSpinner, gbc);

        // 启用日志
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Enable Logging:"), gbc);

        gbc.gridx = 1;
        enableLoggingCheckBox = new JCheckBox();
        formPanel.add(enableLoggingCheckBox, gbc);

        // 填充空白
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        formPanel.add(Box.createVerticalGlue(), gbc);

        add(formPanel, BorderLayout.CENTER);

        // 保存按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save Settings");
        saveButton.addActionListener(e -> saveConfig());
        buttonPanel.add(saveButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> loadConfig());
        buttonPanel.add(resetButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // 添加说明文本
        JTextArea infoArea = new JTextArea(
                "Configuration Help:\n" +
                "- Python Path: Path to the Python executable (e.g., /usr/bin/python3 or python.exe)\n" +
                "- Script Timeout: Maximum execution time for Python scripts (in milliseconds)\n" +
                "- Max Rules: Maximum number of Mock rules allowed\n" +
                "- Enable Logging: Enable detailed logging for debugging"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        add(infoArea, BorderLayout.NORTH);
    }

    /**
     * 加载配置
     */
    private void loadConfig() {
        pythonPathField.setText(configStorage.getPythonPath());
        timeoutSpinner.setValue(configStorage.getDefaultTimeout());
        maxRulesSpinner.setValue(configStorage.getMaxRules());
        enableLoggingCheckBox.setSelected(configStorage.isEnableLogging());

        // 更新Python状态
        updatePythonStatus();
    }

    /**
     * 保存配置
     */
    private void saveConfig() {
        String pythonPath = pythonPathField.getText().trim();
        int timeout = (Integer) timeoutSpinner.getValue();
        int maxRules = (Integer) maxRulesSpinner.getValue();
        boolean enableLogging = enableLoggingCheckBox.isSelected();

        // 验证
        if (pythonPath.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Python path cannot be empty!",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 保存配置
        configStorage.setPythonPath(pythonPath);
        configStorage.setDefaultTimeout(timeout);
        configStorage.setMaxRules(maxRules);
        configStorage.setEnableLogging(enableLogging);
        configStorage.saveConfig();

        JOptionPane.showMessageDialog(this,
                "Settings saved successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        // 更新Python状态
        updatePythonStatus();
    }

    /**
     * 浏览Python路径
     */
    private void browsePythonPath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Python Executable");

        // 根据操作系统设置文件过滤器
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Executable Files", "exe"));
        }

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            pythonPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    /**
     * 测试Python路径
     */
    private void testPythonPath() {
        String pythonPath = pythonPathField.getText().trim();
        if (pythonPath.isEmpty()) {
            pythonStatusLabel.setText("Please enter a Python path");
            pythonStatusLabel.setForeground(Color.RED);
            return;
        }

        pythonStatusLabel.setText("Testing...");
        pythonStatusLabel.setForeground(Color.ORANGE);

        // 在后台线程中测试
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    Process process = new ProcessBuilder(pythonPath, "--version")
                            .redirectErrorStream(true)
                            .start();
                    int exitCode = process.waitFor();
                    return exitCode == 0;
                } catch (Exception e) {
                    return false;
                }
            }

            @Override
            protected void done() {
                try {
                    boolean valid = get();
                    if (valid) {
                        pythonStatusLabel.setText("Python path is valid");
                        pythonStatusLabel.setForeground(new Color(0, 128, 0)); // 绿色
                    } else {
                        pythonStatusLabel.setText("Invalid Python path");
                        pythonStatusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    pythonStatusLabel.setText("Error testing Python path");
                    pythonStatusLabel.setForeground(Color.RED);
                }
            }
        };

        worker.execute();
    }

    /**
     * 更新Python状态显示
     */
    private void updatePythonStatus() {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return configStorage.isPythonPathValid();
            }

            @Override
            protected void done() {
                try {
                    boolean valid = get();
                    if (valid) {
                        pythonStatusLabel.setText("Python path is valid");
                        pythonStatusLabel.setForeground(new Color(0, 128, 0));
                    } else {
                        pythonStatusLabel.setText("Python path is not valid");
                        pythonStatusLabel.setForeground(Color.RED);
                    }
                } catch (Exception e) {
                    pythonStatusLabel.setText("Unknown");
                    pythonStatusLabel.setForeground(Color.GRAY);
                }
            }
        };

        worker.execute();
    }
}
