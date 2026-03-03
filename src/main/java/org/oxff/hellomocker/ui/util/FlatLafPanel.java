package org.oxff.hellomocker.ui.util;

import javax.swing.*;
import java.awt.*;

/**
 * FlatLaf样式面板基类
 * 注意：由于FlatLaf会影响RSyntaxTextArea编辑功能，暂时移除所有特殊处理
 * 直接使用标准JPanel
 *
 * @author oxff
 * @version 1.0
 */
public class FlatLafPanel extends JPanel {

    public FlatLafPanel() {
        super();
    }

    public FlatLafPanel(LayoutManager layout) {
        super(layout);
    }

    public FlatLafPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
    }

    public FlatLafPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
    }
}
