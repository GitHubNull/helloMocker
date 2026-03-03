package org.oxff.hellomocker.ui.util;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * FlatLaf样式面板基类
 * 只在当前面板及其子组件上应用FlatLaf样式，不影响全局UI
 *
 * @author oxff
 * @version 1.0
 */
public class FlatLafPanel extends JPanel {

    private static boolean flatLafInitialized = false;

    public FlatLafPanel() {
        super();
        initializeFlatLaf();
    }

    public FlatLafPanel(LayoutManager layout) {
        super(layout);
        initializeFlatLaf();
    }

    public FlatLafPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        initializeFlatLaf();
    }

    public FlatLafPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        initializeFlatLaf();
    }

    /**
     * 初始化FlatLaf样式（局部应用）
     */
    private void initializeFlatLaf() {
        // 只为当前面板及其子组件应用FlatLaf样式
        // 注意：我们不修改全局UIManager，只影响当前组件树
        
        // 设置面板背景色（使用FlatLaf风格）
        setBackground(UIManager.getColor("Panel.background"));
        
        // 注册FlatLaf客户端属性，用于微调组件样式
        putClientProperty(FlatClientProperties.STYLE, "background: @background");
        
        // 添加组件监听器，在组件添加时应用FlatLaf样式
        addContainerListener(new java.awt.event.ContainerAdapter() {
            @Override
            public void componentAdded(java.awt.event.ContainerEvent e) {
                applyFlatLafStyle(e.getChild());
            }
        });
    }

    /**
     * 递归应用FlatLaf样式到组件及其子组件
     */
    private void applyFlatLafStyle(Component component) {
        if (component == null) {
            return;
        }

        // 应用基本样式
        if (component instanceof JComponent) {
            JComponent jc = (JComponent) component;
            
            // 设置现代边框样式
            if (jc.getBorder() == null || jc.getBorder() instanceof javax.swing.border.EmptyBorder) {
                // 保持原有边框或应用FlatLaf默认边框
            }
            
            // 设置字体（使用系统默认字体）
            if (jc.getFont() == null) {
                jc.setFont(UIManager.getFont("defaultFont"));
            }
        }

        // 递归处理容器
        if (component instanceof Container) {
            Container container = (Container) component;
            for (Component child : container.getComponents()) {
                applyFlatLafStyle(child);
            }
        }
    }

    /**
     * 添加组件时自动应用FlatLaf样式
     */
    @Override
    public Component add(Component comp) {
        Component result = super.add(comp);
        applyFlatLafStyle(comp);
        return result;
    }

    @Override
    public Component add(Component comp, int index) {
        Component result = super.add(comp, index);
        applyFlatLafStyle(comp);
        return result;
    }

    @Override
    public void add(Component comp, Object constraints) {
        super.add(comp, constraints);
        applyFlatLafStyle(comp);
    }

    @Override
    public void add(Component comp, Object constraints, int index) {
        super.add(comp, constraints, index);
        applyFlatLafStyle(comp);
    }
}
