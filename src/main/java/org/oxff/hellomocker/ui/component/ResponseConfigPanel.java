package org.oxff.hellomocker.ui.component;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 响应配置面板
 * 用于配置Mock响应的各种模式
 *
 * @author oxff
 * @version 1.0
 */
public class ResponseConfigPanel extends JPanel {

    private final MontoyaApi api;

    private JComboBox<String> responseTypeCombo;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // 静态响应面板组件
    private JSpinner statusCodeSpinner;
    private JTextArea headersArea;
    private JTextArea bodyArea;

    // Python脚本面板组件
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea pythonEditor;
    private JTextField pythonFileField;
    private JPanel previewPanel;  // 代码预览面板容器

    // 代理转发面板组件
    private JTextField targetHostField;
    private JSpinner targetPortSpinner;
    private JCheckBox useSslCheckBox;

    public ResponseConfigPanel(MontoyaApi api) {
        this.api = api;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));

        // 响应类型选择
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Response Type:"));
        String[] types = {"STATIC", "PYTHON_SCRIPT", "PROXY_FORWARD"};
        responseTypeCombo = new JComboBox<>(types);
        responseTypeCombo.addActionListener(e -> switchCard());
        typePanel.add(responseTypeCombo);
        add(typePanel, BorderLayout.NORTH);

        // 卡片布局面板
        cardLayout = new CardLayout();
        cardsPanel = new JPanel(cardLayout);

        // 添加各种响应类型的面板
        cardsPanel.add(createStaticResponsePanel(), "STATIC");
        cardsPanel.add(createPythonScriptPanel(), "PYTHON_SCRIPT");
        cardsPanel.add(createProxyForwardPanel(), "PROXY_FORWARD");

        add(cardsPanel, BorderLayout.CENTER);

        // 默认显示静态响应面板
        switchCard();
    }

    private JPanel createStaticResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 状态码
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Status Code:"));
        SpinnerNumberModel statusModel = new SpinnerNumberModel(200, 100, 599, 1);
        statusCodeSpinner = new JSpinner(statusModel);
        topPanel.add(statusCodeSpinner);
        panel.add(topPanel, BorderLayout.NORTH);

        // 分割面板（Headers和Body）
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.3);

        // Headers
        JPanel headersPanel = new JPanel(new BorderLayout());
        headersPanel.setBorder(BorderFactory.createTitledBorder("Headers (Name: Value per line)"));
        headersArea = new JTextArea(5, 40);
        headersArea.setText("Content-Type: application/json");
        headersPanel.add(new JScrollPane(headersArea), BorderLayout.CENTER);
        splitPane.setTopComponent(headersPanel);

        // Body
        JPanel bodyPanel = new JPanel(new BorderLayout());
        bodyPanel.setBorder(BorderFactory.createTitledBorder("Response Body"));
        bodyArea = new JTextArea(10, 40);
        bodyPanel.add(new JScrollPane(bodyArea), BorderLayout.CENTER);
        splitPane.setBottomComponent(bodyPanel);

        panel.add(splitPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPythonScriptPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // 顶部面板：模式选择和文件导入
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        
        // 模式选择
        JPanel modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modePanel.add(new JLabel("Script Mode:"));
        String[] modes = {"Online Editor", "Import File"};
        JComboBox<String> modeCombo = new JComboBox<>(modes);
        modeCombo.addActionListener(e -> togglePythonMode(modeCombo.getSelectedIndex()));
        modePanel.add(modeCombo);
        topPanel.add(modePanel, BorderLayout.NORTH);
        
        // 文件导入面板（默认隐藏）
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.add(new JLabel("Script File:"), BorderLayout.WEST);
        pythonFileField = new JTextField();
        filePanel.add(pythonFileField, BorderLayout.CENTER);
        
        // 按钮面板（Browse + Export Template）
        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browsePythonFile());
        fileButtonPanel.add(browseButton);
        
        JButton exportTemplateButton = new JButton("Export Template");
        exportTemplateButton.setToolTipText("Export the built-in Python script template to a file");
        exportTemplateButton.addActionListener(e -> exportPythonTemplate());
        fileButtonPanel.add(exportTemplateButton);
        
        filePanel.add(fileButtonPanel, BorderLayout.EAST);
        filePanel.setVisible(false);  // 默认隐藏
        topPanel.add(filePanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);

        // 代码编辑器区域（RSyntaxTextArea）
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createTitledBorder("Python Script"));
        
        // 使用自定义子类来修复BurpSuite主题导致的输入问题
        pythonEditor = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea(20, 60) {
            @Override
            public void addNotify() {
                super.addNotify();
                // 组件被添加到容器后，延迟修复InputMap
                // 这是因为BurpSuite的主题会在组件添加后才应用
                org.fife.ui.rsyntaxtextarea.RSyntaxTextArea editor = this;
                SwingUtilities.invokeLater(() -> {
                    ResponseConfigPanel.this.fixEditorInputMap(editor);
                });
            }
        };
        pythonEditor.setSyntaxEditingStyle(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_PYTHON);
        pythonEditor.setCodeFoldingEnabled(true);
        
        // 关键修复：添加KeyListener直接处理键盘输入
        // 这是最可靠的方式，绕过InputMap的问题
        pythonEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                // KeyTyped事件表示字符输入，让默认处理继续
                // 如果默认处理被阻止，这里手动插入字符
                if (e.isConsumed()) {
                    char c = e.getKeyChar();
                    if (c != java.awt.event.KeyEvent.CHAR_UNDEFINED && !Character.isISOControl(c)) {
                        try {
                            int pos = pythonEditor.getCaretPosition();
                            pythonEditor.getDocument().insertString(pos, String.valueOf(c), null);
                        } catch (Exception ex) {
                            // 忽略
                        }
                    }
                }
            }
        });
        
        // 从资源文件加载默认代码模板
        String pythonTemplate = ResourceLoader.loadPythonScriptTemplate();
        pythonEditor.setText(pythonTemplate);

        org.fife.ui.rtextarea.RTextScrollPane scrollPane = new org.fife.ui.rtextarea.RTextScrollPane(pythonEditor);
        editorPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(editorPanel, BorderLayout.CENTER);

        // 可折叠的帮助面板
        JPanel helpPanel = createCollapsibleHelpPanel();
        panel.add(helpPanel, BorderLayout.SOUTH);
        
        // 保存引用以便切换
        this.previewPanel = filePanel;

        return panel;
    }
    
    private JPanel createCollapsibleHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 帮助按钮
        JButton helpButton = new JButton("Show Help ▼");
        helpButton.setFocusPainted(false);
        panel.add(helpButton, BorderLayout.NORTH);
        
        // 帮助内容面板（默认隐藏）
        JPanel helpContentPanel = new JPanel(new BorderLayout());
        helpContentPanel.setVisible(false);
        
        // 从资源文件加载帮助文本
        String helpContent = ResourceLoader.loadPythonHelpText();
        JTextArea helpText = new JTextArea(helpContent, 10, 50);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // 添加滚动条
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        helpContentPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(helpContentPanel, BorderLayout.CENTER);
        
        // 点击按钮切换显示/隐藏
        helpButton.addActionListener(e -> {
            boolean isVisible = helpContentPanel.isVisible();
            helpContentPanel.setVisible(!isVisible);
            helpButton.setText(isVisible ? "Show Help ▼" : "Hide Help ▲");
            panel.revalidate();
            panel.repaint();
        });
        
        return panel;
    }

    private void togglePythonMode(int mode) {
        if (mode == 0) {
            // Online Editor mode
            previewPanel.setVisible(false);
            pythonEditor.setEditable(true);
        } else {
            // Import File mode
            previewPanel.setVisible(true);
            // 如果选择了文件，读取内容到编辑器（只读显示）
            String filePath = pythonFileField.getText().trim();
            if (!filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), 
                            java.nio.charset.StandardCharsets.UTF_8);
                        pythonEditor.setText(content);
                    } catch (Exception e) {
                        // 忽略读取错误
                    }
                }
            }
        }
        revalidate();
        repaint();
    }

    private JPanel createProxyForwardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // 目标主机
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Target Host:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        targetHostField = new JTextField("127.0.0.1", 30);
        targetHostField.setToolTipText("Target host IP or domain (e.g., 127.0.0.1 or localhost)");
        panel.add(targetHostField, gbc);

        // 目标端口
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        panel.add(new JLabel("Target Port:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        SpinnerNumberModel portModel = new SpinnerNumberModel(8765, 1, 65535, 1);
        targetPortSpinner = new JSpinner(portModel);
        panel.add(targetPortSpinner, gbc);

        // SSL选项和导出按钮放在同一行
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Use SSL:"), gbc);

        JPanel sslAndExportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        useSslCheckBox = new JCheckBox();
        sslAndExportPanel.add(useSslCheckBox);
        
        // 添加分隔
        sslAndExportPanel.add(Box.createHorizontalStrut(20));
        
        // FastAPI导出按钮
        JButton exportFastapiButton = new JButton("Export FastAPI Server");
        exportFastapiButton.setToolTipText("Export FastAPI server template to receive forwarded requests");
        exportFastapiButton.addActionListener(e -> exportFastAPIServerTemplate());
        sslAndExportPanel.add(exportFastapiButton);
        
        // Flask导出按钮
        JButton exportFlaskButton = new JButton("Export Flask Server");
        exportFlaskButton.setToolTipText("Export Flask server template to receive forwarded requests");
        exportFlaskButton.addActionListener(e -> exportFlaskServerTemplate());
        sslAndExportPanel.add(exportFlaskButton);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sslAndExportPanel, gbc);

        // 帮助文本
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea helpText = new JTextArea(
            "Proxy Forward Help:\n" +
            "- Forwards the matched request to the specified target\n" +
            "- Target Host: IP address or domain name (default: 127.0.0.1)\n" +
            "- Target Port: Port number (default: 8765)\n" +
            "- Use SSL: Enable HTTPS connection\n\n" +
            "Export Server Templates:\n" +
            "- FastAPI Server: pip install fastapi uvicorn\n" +
            "- Flask Server: pip install flask\n" +
            "\n" +
            "Example:\n" +
            "- Target Host: 127.0.0.1\n" +
            "- Target Port: 8765\n" +
            "- Use SSL: unchecked\n" +
            "=> Forwards to http://127.0.0.1:8765"
        );
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        panel.add(helpText, gbc);

        return panel;
    }

    private void switchCard() {
        String selectedType = (String) responseTypeCombo.getSelectedItem();
        if (selectedType != null) {
            cardLayout.show(cardsPanel, selectedType);
        }
    }

    private void browsePythonFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Python Script");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            
            // 验证Python语法
            if (validatePythonSyntax(filePath)) {
                pythonFileField.setText(filePath);
            }
        }
    }
    
    /**
     * 导出Python脚本模板
     */
    private void exportPythonTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Python Script Template");
        fileChooser.setSelectedFile(new File("helloMocker_template.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // 检查文件是否存在，提示是否覆盖
            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                // 从资源文件读取模板
                String template = ResourceLoader.loadPythonScriptTemplate();
                java.nio.file.Files.write(selectedFile.toPath(), 
                        template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                JOptionPane.showMessageDialog(this,
                        "Template exported successfully to:\n" + selectedFile.getAbsolutePath(),
                        "Export Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export template: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 导出FastAPI服务器模板
     */
    private void exportFastAPIServerTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export FastAPI Server Template");
        fileChooser.setSelectedFile(new File("fastapi_receiver_server.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // 检查文件是否存在，提示是否覆盖
            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                // 从资源文件读取FastAPI模板
                String template = ResourceLoader.loadFastAPIServerTemplate();
                java.nio.file.Files.write(selectedFile.toPath(), 
                        template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                JOptionPane.showMessageDialog(this,
                        "FastAPI server template exported successfully!\n\n" +
                        "Location: " + selectedFile.getAbsolutePath() + "\n\n" +
                        "Prerequisites:\n" +
                        "  pip install fastapi uvicorn\n\n" +
                        "Usage:\n" +
                        "  python " + selectedFile.getName() + "\n" +
                        "  Server will start on http://127.0.0.1:8765",
                        "Export Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export template: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 导出Flask服务器模板
     */
    private void exportFlaskServerTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Flask Server Template");
        fileChooser.setSelectedFile(new File("flask_receiver_server.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            // 检查文件是否存在，提示是否覆盖
            if (selectedFile.exists()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        "File already exists. Overwrite?",
                        "Confirm Overwrite",
                        JOptionPane.YES_NO_OPTION);
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            try {
                // 从资源文件读取Flask模板
                String template = ResourceLoader.loadFlaskServerTemplate();
                java.nio.file.Files.write(selectedFile.toPath(), 
                        template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                JOptionPane.showMessageDialog(this,
                        "Flask server template exported successfully!\n\n" +
                        "Location: " + selectedFile.getAbsolutePath() + "\n\n" +
                        "Prerequisites:\n" +
                        "  pip install flask\n\n" +
                        "Usage:\n" +
                        "  python " + selectedFile.getName() + "\n" +
                        "  Server will start on http://127.0.0.1:8765",
                        "Export Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Failed to export template: " + e.getMessage(),
                        "Export Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * 验证Python脚本语法
     * 
     * @param filePath Python文件路径
     * @return 语法是否正确
     */
    private boolean validatePythonSyntax(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return false;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        
        String pythonPath = api.persistence().extensionData().getString("hellomocker.config.pythonPath");
        if (pythonPath == null || pythonPath.trim().isEmpty()) {
            pythonPath = "python3"; // 默认使用python3
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    "-m", "py_compile",
                    filePath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
            // 读取错误输出
            StringBuilder errors = new StringBuilder();
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    errors.append(line).append("\n");
                }
            }
            
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                // 语法错误
                JOptionPane.showMessageDialog(this,
                        "Python syntax error in file:\n" + filePath + "\n\n" + errors.toString(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception e) {
            // 验证失败（可能是Python路径问题），但不阻止导入，只记录日志
            System.err.println("Python syntax validation failed: " + e.getMessage());
            return true; // 允许导入，即使验证失败
        }
    }

    /**
     * 获取响应配置
     */
    public ResponseConfig getResponseConfig() {
        String typeStr = (String) responseTypeCombo.getSelectedItem();
        ResponseConfig.ResponseType type = ResponseConfig.parseResponseType(typeStr);

        ResponseConfig.ResponseConfigBuilder builder = ResponseConfig.builder()
                .type(type);

        switch (type) {
            case STATIC -> {
                builder.statusCode((Integer) statusCodeSpinner.getValue());
                builder.headers(parseHeaders(headersArea.getText()));
                builder.body(bodyArea.getText());
            }
            case PYTHON_SCRIPT -> {
                // 如果是在线编辑器模式，保存代码内容
                if (pythonEditor != null) {
                    String script = pythonEditor.getText();
                    if (script != null && !script.trim().isEmpty()) {
                        builder.pythonScript(script);
                    }
                }
                // 如果选择了文件，也保存文件路径
                String filePath = pythonFileField.getText().trim();
                if (!filePath.isEmpty()) {
                    builder.pythonFilePath(filePath);
                }
            }
            case PROXY_FORWARD -> {
                builder.targetHost(targetHostField.getText().trim());
                builder.targetPort((Integer) targetPortSpinner.getValue());
                builder.useSsl(useSslCheckBox.isSelected());
            }
        }

        return builder.build();
    }

    /**
     * 解析Headers文本
     */
    private Map<String, String> parseHeaders(String text) {
        Map<String, String> headers = new HashMap<>();
        if (text == null || text.trim().isEmpty()) {
            return headers;
        }

        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String name = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                if (!name.isEmpty()) {
                    headers.put(name, value);
                }
            }
        }

        return headers;
    }

    /**
     * 格式化Headers为文本
     */
    private String formatHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * 设置响应配置
     */
    public void setResponseConfig(ResponseConfig config) {
        if (config == null) {
            return;
        }

        if (config.getType() != null) {
            responseTypeCombo.setSelectedItem(config.getType().name());
        }

        switch (config.getType()) {
            case STATIC -> {
                statusCodeSpinner.setValue(config.getStatusCode());
                if (config.getHeaders() != null) {
                    headersArea.setText(formatHeaders(config.getHeaders()));
                }
                if (config.getBody() != null) {
                    bodyArea.setText(config.getBody());
                }
            }
            case PYTHON_SCRIPT -> {
                // 恢复Python脚本代码（优先）
                if (config.getPythonScript() != null && !config.getPythonScript().trim().isEmpty()) {
                    pythonEditor.setText(config.getPythonScript());
                }
                // 恢复文件路径
                if (config.getPythonFilePath() != null) {
                    pythonFileField.setText(config.getPythonFilePath());
                }
            }
            case PROXY_FORWARD -> {
                if (config.getTargetHost() != null) {
                    targetHostField.setText(config.getTargetHost());
                }
                targetPortSpinner.setValue(config.getTargetPort());
                useSslCheckBox.setSelected(config.isUseSsl());
            }
        }
    }
    
    /**
     * 修复RSyntaxTextArea在FlatLaf/BurpSuite主题下无法输入的问题
     * 
     * 问题原因：BurpSuite使用FlatLaf主题，当applyThemeToComponent被调用后，
     * 会覆盖RSyntaxTextArea的InputMap，导致键盘输入事件无法正确传递到编辑器。
     * 
     * 解决方案：使用KeyEventPostProcessor在事件处理完成后检查，
     * 如果字符没有被插入，则手动插入。
     * 
     * @param textArea 需要修复的RSyntaxTextArea组件
     */
    private void fixEditorInputMap(org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea) {
        // 确保编辑器可编辑
        textArea.setEditable(true);
        textArea.setEnabled(true);
        textArea.setFocusable(true);
        
        // 启用输入法支持
        textArea.enableInputMethods(true);
        
        // 设置焦点遍历键，确保Tab键不会被拦截（如果需要Tab缩进）
        textArea.setFocusTraversalKeysEnabled(false);
        
        // 关键修复：注册KeyEventPostProcessor在事件处理后进行补救
        // PostProcessor在事件被所有组件处理后调用
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventPostProcessor(new java.awt.KeyEventPostProcessor() {
                @Override
                public boolean postProcessKeyEvent(java.awt.event.KeyEvent e) {
                    // 只处理目标编辑器获得焦点时的KEY_TYPED事件
                    if (e.getComponent() == textArea && textArea.hasFocus()
                        && e.getID() == java.awt.event.KeyEvent.KEY_TYPED
                        && textArea.isEditable()) {
                        
                        // 如果事件没有被消费，说明InputMap没有正确处理
                        if (!e.isConsumed()) {
                            char c = e.getKeyChar();
                            // 检查是否是可输入字符（非控制键）
                            if (c != java.awt.event.KeyEvent.CHAR_UNDEFINED 
                                && !e.isControlDown() && !e.isAltDown() && !e.isMetaDown()) {
                                
                                try {
                                    // 处理回车
                                    if (c == '\n' || c == '\r') {
                                        textArea.replaceSelection("\n");
                                        e.consume();
                                    } 
                                    // 处理Tab
                                    else if (c == '\t') {
                                        textArea.replaceSelection("\t");
                                        e.consume();
                                    }
                                    // 处理退格键
                                    else if (c == '\b') {
                                        int pos = textArea.getCaretPosition();
                                        if (pos > 0 && textArea.getSelectedText() == null) {
                                            textArea.getDocument().remove(pos - 1, 1);
                                        } else if (textArea.getSelectedText() != null) {
                                            textArea.replaceSelection("");
                                        }
                                        e.consume();
                                    }
                                    // 处理普通可打印字符
                                    else if (c >= ' ' && c != '\u007F') {
                                        textArea.replaceSelection(String.valueOf(c));
                                        e.consume();
                                    }
                                } catch (Exception ex) {
                                    // 忽略异常
                                }
                            }
                        }
                    }
                    return false; // 让事件继续传递
                }
            });
    }
    
    /**
     * 重置文本组件的InputMap，恢复基本的文本编辑键绑定
     * 注意：此方法已被新的KeyEventDispatcher方案替代，保留以备后用
     */
    @SuppressWarnings("unused")
    private void resetTextInputMap(org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea) {
        // 此方法已废弃，保留签名以兼容
    }
}
