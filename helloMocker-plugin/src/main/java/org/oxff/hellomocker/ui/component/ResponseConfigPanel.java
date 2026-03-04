package org.oxff.hellomocker.ui.component;

import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.responses.HttpResponse;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import org.oxff.hellomocker.model.ResponseConfig;
import org.oxff.hellomocker.util.HttpResponseUtils;
import org.oxff.hellomocker.util.ResourceLoader;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * 响应配置面板
 * 用于配置Mock响应的各种模式
 *
 * @author oxff
 * @version 2.0 - 使用BurpSuite原生HttpResponseEditor替代文本编辑器
 */
public class ResponseConfigPanel extends JPanel {

    private final MontoyaApi api;

    private JComboBox<String> responseTypeCombo;
    private CardLayout cardLayout;
    private JPanel cardsPanel;

    // 静态响应面板组件 - 使用Burp原生编辑器
    private HttpResponseEditor httpResponseEditor;

    // Python脚本面板组件
    private org.fife.ui.rsyntaxtextarea.RSyntaxTextArea pythonEditor;
    private JTextField pythonFileField;
    private JPanel previewPanel;

    // 代理转发面板组件
    private JTextField targetHostField;
    private JSpinner targetPortSpinner;
    private JCheckBox useSslCheckBox;

    // JAR扩展面板组件
    private JarExtensionPanel jarExtensionPanel;

    public ResponseConfigPanel(MontoyaApi api) {
        this.api = api;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(5, 5));

        // 响应类型选择
        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        typePanel.add(new JLabel("Response Type:"));
        String[] types = {"STATIC", "PYTHON_SCRIPT", "PROXY_FORWARD", "JAR_EXTENSION"};
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
        cardsPanel.add(createJarExtensionPanel(), "JAR_EXTENSION");

        add(cardsPanel, BorderLayout.CENTER);

        // 默认显示静态响应面板
        switchCard();
    }

    /**
     * 创建静态响应面板 - 使用BurpSuite原生HttpResponseEditor
     */
    private JPanel createStaticResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("HTTP Response Editor"));

        // 创建Burp原生HTTP响应编辑器
        httpResponseEditor = api.userInterface().createHttpResponseEditor();
        
        // 将编辑器组件添加到面板 - 使用uiComponent()方法
        Component editorComponent = httpResponseEditor.uiComponent();
        panel.add(editorComponent, BorderLayout.CENTER);

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
        
        // 按钮面板
        JPanel fileButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browsePythonFile());
        fileButtonPanel.add(browseButton);
        
        JButton exportTemplateButton = new JButton("Export Template");
        exportTemplateButton.setToolTipText("Export the built-in Python script template to a file");
        exportTemplateButton.addActionListener(e -> exportPythonTemplate());
        fileButtonPanel.add(exportTemplateButton);
        
        filePanel.add(fileButtonPanel, BorderLayout.EAST);
        filePanel.setVisible(false);
        topPanel.add(filePanel, BorderLayout.CENTER);
        
        panel.add(topPanel, BorderLayout.NORTH);

        // 代码编辑器区域
        JPanel editorPanel = new JPanel(new BorderLayout());
        editorPanel.setBorder(BorderFactory.createTitledBorder("Python Script"));
        
        pythonEditor = new org.fife.ui.rsyntaxtextarea.RSyntaxTextArea(20, 60) {
            @Override
            public void addNotify() {
                super.addNotify();
                org.fife.ui.rsyntaxtextarea.RSyntaxTextArea editor = this;
                SwingUtilities.invokeLater(() -> {
                    ResponseConfigPanel.this.fixEditorInputMap(editor);
                });
            }
        };
        pythonEditor.setSyntaxEditingStyle(org.fife.ui.rsyntaxtextarea.SyntaxConstants.SYNTAX_STYLE_PYTHON);
        pythonEditor.setCodeFoldingEnabled(true);
        
        pythonEditor.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
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
        
        String pythonTemplate = ResourceLoader.loadPythonScriptTemplate();
        pythonEditor.setText(pythonTemplate);

        org.fife.ui.rtextarea.RTextScrollPane scrollPane = new org.fife.ui.rtextarea.RTextScrollPane(pythonEditor);
        editorPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(editorPanel, BorderLayout.CENTER);

        // 可折叠的帮助面板
        JPanel helpPanel = createCollapsibleHelpPanel();
        panel.add(helpPanel, BorderLayout.SOUTH);
        
        this.previewPanel = filePanel;

        return panel;
    }
    
    private JPanel createCollapsibleHelpPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JButton helpButton = new JButton("Show Help ▼");
        helpButton.setFocusPainted(false);
        panel.add(helpButton, BorderLayout.NORTH);
        
        JPanel helpContentPanel = new JPanel(new BorderLayout());
        helpContentPanel.setVisible(false);
        
        String helpContent = ResourceLoader.loadPythonHelpText();
        JTextArea helpText = new JTextArea(helpContent, 10, 50);
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        helpContentPanel.add(scrollPane, BorderLayout.CENTER);
        panel.add(helpContentPanel, BorderLayout.CENTER);
        
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
            previewPanel.setVisible(false);
            pythonEditor.setEditable(true);
        } else {
            previewPanel.setVisible(true);
            String filePath = pythonFileField.getText().trim();
            if (!filePath.isEmpty()) {
                File file = new File(filePath);
                if (file.exists()) {
                    try {
                        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), 
                            java.nio.charset.StandardCharsets.UTF_8);
                        pythonEditor.setText(content);
                    } catch (Exception e) {
                        // 忽略
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Target Host:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        targetHostField = new JTextField("127.0.0.1", 30);
        targetHostField.setToolTipText("Target host IP or domain");
        panel.add(targetHostField, gbc);

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

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Use SSL:"), gbc);

        JPanel sslAndExportPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        useSslCheckBox = new JCheckBox();
        sslAndExportPanel.add(useSslCheckBox);
        sslAndExportPanel.add(Box.createHorizontalStrut(20));
        
        JButton exportFastapiButton = new JButton("Export FastAPI Server");
        exportFastapiButton.setToolTipText("Export FastAPI server template");
        exportFastapiButton.addActionListener(e -> exportFastAPIServerTemplate());
        sslAndExportPanel.add(exportFastapiButton);
        
        JButton exportFlaskButton = new JButton("Export Flask Server");
        exportFlaskButton.setToolTipText("Export Flask server template");
        exportFlaskButton.addActionListener(e -> exportFlaskServerTemplate());
        sslAndExportPanel.add(exportFlaskButton);
        
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(sslAndExportPanel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea helpText = new JTextArea(
            "Proxy Forward Help:\n" +
            "- Forwards matched requests to the target\n" +
            "- Target Host: IP or domain (default: 127.0.0.1)\n" +
            "- Target Port: Port number (default: 8765)\n" +
            "- Use SSL: Enable HTTPS\n\n" +
            "Export Server Templates:\n" +
            "- FastAPI: pip install fastapi uvicorn\n" +
            "- Flask: pip install flask"
        );
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        panel.add(helpText, gbc);

        return panel;
    }

    private JPanel createJarExtensionPanel() {
        jarExtensionPanel = new JarExtensionPanel();
        jarExtensionPanel.setApi(api);
        return jarExtensionPanel;
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
            
            if (validatePythonSyntax(filePath)) {
                pythonFileField.setText(filePath);
            }
        }
    }
    
    private void exportPythonTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Python Script Template");
        fileChooser.setSelectedFile(new File("helloMocker_template.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
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
    
    private void exportFastAPIServerTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export FastAPI Server Template");
        fileChooser.setSelectedFile(new File("fastapi_receiver_server.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
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
                String template = ResourceLoader.loadFastAPIServerTemplate();
                java.nio.file.Files.write(selectedFile.toPath(), 
                        template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                JOptionPane.showMessageDialog(this,
                        "FastAPI server template exported successfully!",
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
    
    private void exportFlaskServerTemplate() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Flask Server Template");
        fileChooser.setSelectedFile(new File("flask_receiver_server.py"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Python Files", "py"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
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
                String template = ResourceLoader.loadFlaskServerTemplate();
                java.nio.file.Files.write(selectedFile.toPath(), 
                        template.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                
                JOptionPane.showMessageDialog(this,
                        "Flask server template exported successfully!",
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
            pythonPath = "python3";
        }
        
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonPath,
                    "-m", "py_compile",
                    filePath
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            
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
                JOptionPane.showMessageDialog(this,
                        "Python syntax error:\n" + errors.toString(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取响应配置 - 从Burp编辑器获取数据
     */
    public ResponseConfig getResponseConfig() {
        String typeStr = (String) responseTypeCombo.getSelectedItem();
        ResponseConfig.ResponseType type = ResponseConfig.parseResponseType(typeStr);

        ResponseConfig.ResponseConfigBuilder builder = ResponseConfig.builder()
                .type(type);

        switch (type) {
            case STATIC -> {
                // 从Burp原生编辑器获取响应
                if (httpResponseEditor != null) {
                    HttpResponse response = httpResponseEditor.getResponse();
                    if (response != null) {
                        ResponseConfig config = HttpResponseUtils.extractResponseConfig(response);
                        builder.statusCode(config.getStatusCode());
                        builder.headers(config.getHeaders());
                        builder.body(config.getBody());
                    }
                }
            }
            case PYTHON_SCRIPT -> {
                if (pythonEditor != null) {
                    String script = pythonEditor.getText();
                    if (script != null && !script.trim().isEmpty()) {
                        builder.pythonScript(script);
                    }
                }
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
            case JAR_EXTENSION -> {
                if (jarExtensionPanel != null) {
                    builder.jarPath(jarExtensionPanel.getJarPath());
                    builder.handlerClassName(jarExtensionPanel.getHandlerClassName());
                }
            }
        }

        return builder.build();
    }

    /**
     * 设置响应配置 - 加载到Burp编辑器
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
                // 加载到Burp原生编辑器
                if (httpResponseEditor != null) {
                    HttpResponse response = HttpResponseUtils.createHttpResponse(api, config);
                    if (response != null) {
                        httpResponseEditor.setResponse(response);
                    }
                }
            }
            case PYTHON_SCRIPT -> {
                if (config.getPythonScript() != null && !config.getPythonScript().trim().isEmpty()) {
                    pythonEditor.setText(config.getPythonScript());
                }
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
            case JAR_EXTENSION -> {
                if (jarExtensionPanel != null) {
                    if (config.getJarPath() != null) {
                        jarExtensionPanel.setJarPath(config.getJarPath());
                    }
                    if (config.getHandlerClassName() != null) {
                        jarExtensionPanel.setHandlerClassName(config.getHandlerClassName());
                    }
                }
            }
        }
    }
    
    /**
     * 修复RSyntaxTextArea在BurpSuite环境中无法输入的问题
     */
    private void fixEditorInputMap(org.fife.ui.rsyntaxtextarea.RSyntaxTextArea textArea) {
        textArea.setEditable(true);
        textArea.setEnabled(true);
        textArea.setFocusable(true);
        textArea.enableInputMethods(true);
        textArea.setFocusTraversalKeysEnabled(false);
        
        java.awt.KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventPostProcessor(new java.awt.KeyEventPostProcessor() {
                @Override
                public boolean postProcessKeyEvent(java.awt.event.KeyEvent e) {
                    if (e.getComponent() == textArea && textArea.hasFocus()
                        && e.getID() == java.awt.event.KeyEvent.KEY_TYPED
                        && textArea.isEditable()) {
                        
                        if (!e.isConsumed()) {
                            char c = e.getKeyChar();
                            if (c != java.awt.event.KeyEvent.CHAR_UNDEFINED 
                                && !e.isControlDown() && !e.isAltDown() && !e.isMetaDown()) {
                                
                                try {
                                    if (c == '\n' || c == '\r') {
                                        textArea.replaceSelection("\n");
                                        e.consume();
                                    } else if (c == '\t') {
                                        textArea.replaceSelection("\t");
                                        e.consume();
                                    } else if (c == '\b') {
                                        int pos = textArea.getCaretPosition();
                                        if (pos > 0 && textArea.getSelectedText() == null) {
                                            textArea.getDocument().remove(pos - 1, 1);
                                        } else if (textArea.getSelectedText() != null) {
                                            textArea.replaceSelection("");
                                        }
                                        e.consume();
                                    } else if (c >= ' ' && c != '\u007F') {
                                        textArea.replaceSelection(String.valueOf(c));
                                        e.consume();
                                    }
                                } catch (Exception ex) {
                                    // 忽略
                                }
                            }
                        }
                    }
                    return false;
                }
            });
    }
}