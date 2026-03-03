package org.oxff.hellomocker.ui.component;

import burp.api.montoya.MontoyaApi;
import org.oxff.hellomocker.model.ResponseConfig;

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
    private JTextArea pythonEditor;
    private JTextField pythonFileField;

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

        // 文件导入
        JPanel filePanel = new JPanel(new BorderLayout(5, 5));
        filePanel.add(new JLabel("Script File:*"), BorderLayout.WEST);
        pythonFileField = new JTextField();
        filePanel.add(pythonFileField, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browsePythonFile());
        buttonPanel.add(browseButton);
        
        JButton viewButton = new JButton("View");
        viewButton.addActionListener(e -> viewPythonFile());
        buttonPanel.add(viewButton);
        
        filePanel.add(buttonPanel, BorderLayout.EAST);
        panel.add(filePanel, BorderLayout.NORTH);

        // 代码预览区域
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Script Preview (Read Only)"));
        
        pythonEditor = new JTextArea(15, 60);
        pythonEditor.setFont(new Font("Monospaced", Font.PLAIN, 12));
        pythonEditor.setEditable(false);
        pythonEditor.setLineWrap(false);
        
        JScrollPane scrollPane = new JScrollPane(pythonEditor);
        previewPanel.add(scrollPane, BorderLayout.CENTER);
        
        panel.add(previewPanel, BorderLayout.CENTER);

        // 帮助文本
        JTextArea helpText = new JTextArea(
            "Python Script Help:\n" +
            "- Create a .py file with handle_request(request) function\n" +
            "- Click 'Browse...' to select your Python script file\n" +
            "- Click 'View' to preview the script content\n" +
            "- Request dict contains: url, method, headers, body, path, query, host, port, protocol\n" +
            "- Return a dict with: status (int), headers (dict), body (str)\n" +
            "- Use 'body_base64' instead of 'body' for binary data"
        );
        helpText.setEditable(false);
        helpText.setBackground(panel.getBackground());
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        panel.add(helpText, BorderLayout.SOUTH);

        return panel;
    }
    
    private void viewPythonFile() {
        String filePath = pythonFileField.getText().trim();
        if (filePath.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please select a Python script file first.", 
                "No File Selected", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        File file = new File(filePath);
        if (!file.exists()) {
            JOptionPane.showMessageDialog(this, 
                "File not found: " + filePath, 
                "File Not Found", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
            pythonEditor.setText(content);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error reading file: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
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
        targetHostField = new JTextField(30);
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
        SpinnerNumberModel portModel = new SpinnerNumberModel(8080, 1, 65535, 1);
        targetPortSpinner = new JSpinner(portModel);
        panel.add(targetPortSpinner, gbc);

        // SSL选项
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Use SSL:"), gbc);

        gbc.gridx = 1;
        useSslCheckBox = new JCheckBox();
        panel.add(useSslCheckBox, gbc);

        // 帮助文本
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        JTextArea helpText = new JTextArea(
            "Proxy Forward Help:\n" +
            "- Forwards the matched request to the specified target\n" +
            "- Target Host: IP address or domain name\n" +
            "- Target Port: Port number (1-65535)\n" +
            "- Use SSL: Enable HTTPS connection\n\n" +
            "Example:\n" +
            "- Target Host: 127.0.0.1\n" +
            "- Target Port: 5000\n" +
            "- Use SSL: unchecked\n" +
            "=> Forwards to http://127.0.0.1:5000"
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
            pythonFileField.setText(selectedFile.getAbsolutePath());
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
                if (config.getPythonFilePath() != null) {
                    pythonFileField.setText(config.getPythonFilePath());
                }
                // 清空预览区域
                pythonEditor.setText("");
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
}
