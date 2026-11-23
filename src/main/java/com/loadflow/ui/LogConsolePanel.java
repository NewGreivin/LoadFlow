package com.loadflow.ui;

import com.loadflow.model.LogEntry;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class LogConsolePanel extends JPanel {
    private final JTextPane textPane = new JTextPane();
    private final StyledDocument doc = textPane.getStyledDocument();

    private Style styleInfo;
    private Style styleWarn;
    private Style styleError;
    private Style styleSuccess;

    public LogConsolePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
        initStyles();
    }

    private void initComponents() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton btnClear = new JButton("Limpiar Consola");
        btnClear.addActionListener(e -> textPane.setText(""));
        toolbar.add(btnClear);

        add(toolbar, BorderLayout.NORTH);

        textPane.setEditable(false);
        textPane.setBackground(new Color(24, 26, 31));
        textPane.setFont(new Font("Consolas", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void initStyles() {
        styleInfo = doc.addStyle("INFO", null);
        StyleConstants.setForeground(styleInfo, new Color(173, 216, 230));

        styleWarn = doc.addStyle("WARN", null);
        StyleConstants.setForeground(styleWarn, new Color(255, 204, 0));

        styleError = doc.addStyle("ERROR", null);
        StyleConstants.setForeground(styleError, new Color(255, 102, 102));

        styleSuccess = doc.addStyle("SUCCESS", null);
        StyleConstants.setForeground(styleSuccess, new Color(144, 238, 144));
    }

    public void appendLog(LogEntry entry) {
        SwingUtilities.invokeLater(() -> {
            try {
                Style s = switch (entry.getLevel()) {
                    case WARN -> styleWarn;
                    case ERROR -> styleError;
                    case SUCCESS -> styleSuccess;
                    default -> styleInfo;
                };

                doc.insertString(doc.getLength(), entry.toString() + "\n", s);
                textPane.setCaretPosition(doc.getLength());
            } catch (BadLocationException ignored) {}
        });
    }
}
