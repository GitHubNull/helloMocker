package org.oxff.hellomocker.ui.component;

import org.oxff.hellomocker.handler.JarExtensionHandler;
import org.oxff.hellomocker.model.ResponseConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * JAR扩展配置面板
 *
 * @author oxff
 * @version 1.0
 */
public class JarExtensionPanel extends JPanel {

    private JTextField jarPathField;
    private JTextField handlerClassField;
    private JButton browseButton;
    private JButton loadButton;
    private JButton unloadButton;
    private JLabel statusLabel;
    private JTextArea infoArea;

    private JarExtensionHandler handler;

    public JarExtensionPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 配置面板
        JPanel configPanel = new JPanel(new GridBagLayout());
        configPanel.setBorder(BorderFactory.createTitledBorder("JAR Extension Configuration"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // JAR文件路径
        gbc.gridx = 0;
        gbc.gridy = 0;
        configPanel.add(new JLabel("JAR File:*"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        jarPathField = new JTextField(30);
        configPanel.add(jarPathField, gbc);

        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        browseButton = new JButton("Browse...");
        browseButton.addActionListener(e -> browseJarFile());
        configPanel.add(browseButton, gbc);

        // 处理器类名
        gbc.gridx = 0;
        gbc.gridy = 1;
        configPanel.add(new JLabel("Handler Class:*"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        handlerClassField = new JTextField(30);
        handlerClassField.setToolTipText("Full qualified class name (e.g., com.example.MyHandler)");
        configPanel.add(handlerClassField, gbc);

        // 按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        
        loadButton = new JButton("Load");
        loadButton.addActionListener(e -> loadJar());
        buttonPanel.add(loadButton);

        unloadButton = new JButton("Unload");
        unloadButton.setEnabled(false);
        unloadButton.addActionListener(e -> unloadJar());
        buttonPanel.add(unloadButton);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        configPanel.add(buttonPanel, gbc);

        // 状态标签
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        statusLabel = new JLabel("Status: Not loaded");
        statusLabel.setForeground(Color.GRAY);
        configPanel.add(statusLabel, gbc);

        add(configPanel, BorderLayout.NORTH);

        // 信息面板
        infoArea = new JTextArea(
            "JAR Extension Help:\n\n" +
            "1. Select a JAR file containing your custom handler\n" +
            "2. Enter the full qualified class name\n" +
            "3. Click 'Load' to load the handler\n\n" +
            "Requirements:\n" +
            "- Class must implement IMockHandler interface\n" +
            "- Must have a public no-arg constructor\n\n" +
            "Example handler class:\n" +
            "  package com.example;\n" +
            "  public class MyHandler implements IMockHandler {\n" +
            "      public HttpResponse handleRequest(HttpRequest request) {\n" +
            "          // Your logic here\n" +
            "      }\n" +
            "  }"
        );
        infoArea.setEditable(false);
        infoArea.setBackground(getBackground());
        infoArea.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Help"));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void browseJarFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select JAR File");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "JAR Files", "jar"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            jarPathField.setText(selectedFile.getAbsolutePath());
        }
    }

    private void loadJar() {
        // This will be implemented when integrated with MontoyaApi
        JOptionPane.showMessageDialog(this,
                "JAR loading will be available when the handler is integrated.",
                "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void unloadJar() {
        if (handler != null) {
            handler.unload();
            handler = null;
            updateStatus();
        }
    }

    private void updateStatus() {
        if (handler != null && handler.isLoaded()) {
            statusLabel.setText("Status: Loaded - " + handler.getHandlerName());
            statusLabel.setForeground(new Color(0, 128, 0));
            loadButton.setEnabled(false);
            unloadButton.setEnabled(true);
        } else {
            statusLabel.setText("Status: Not loaded");
            statusLabel.setForeground(Color.GRAY);
            loadButton.setEnabled(true);
            unloadButton.setEnabled(false);
        }
    }

    public String getJarPath() {
        return jarPathField.getText().trim();
    }

    public void setJarPath(String jarPath) {
        jarPathField.setText(jarPath);
    }

    public String getHandlerClassName() {
        return handlerClassField.getText().trim();
    }

    public void setHandlerClassName(String className) {
        handlerClassField.setText(className);
    }

    public boolean isLoaded() {
        return handler != null && handler.isLoaded();
    }
}