package com.loadflow.ui;

import com.loadflow.engine.TestOrchestrator;
import com.loadflow.model.TestMetrics;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final TestOrchestrator orchestrator;
    private final Runnable openConfigAction;
    private final Runnable exportReportAction;

    private JLabel lblTargetBots;
    private JLabel lblConnectedBots;
    private JLabel lblAvgLatency;
    private JLabel lblSuccessRate;
    private JLabel lblElapsedTime;
    private JLabel lblPackets;
    private JLabel lblStatusBadge;

    private JButton btnStart;
    private JButton btnStopAll;
    private JButton btnExport;

    public DashboardPanel(TestOrchestrator orchestrator, Runnable openConfigAction, Runnable exportReportAction) {
        this.orchestrator = orchestrator;
        this.openConfigAction = openConfigAction;
        this.exportReportAction = exportReportAction;

        setLayout(new BorderLayout(15, 18));
        setBackground(new Color(20, 22, 27));
        setBorder(new EmptyBorder(22, 35, 22, 35));

        initComponents();
    }

    private void initComponents() {
        // --- TOP TOOLBAR & QUICK ACTIONS ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        titleGroup.setOpaque(false);

        JLabel title = new JLabel("📊 Panel de Control de Carga");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 19f));
        title.setForeground(new Color(243, 244, 246));

        lblStatusBadge = new JLabel(" ● EN ESPERA ");
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(45, 52, 64));
        lblStatusBadge.setForeground(new Color(156, 163, 175));
        lblStatusBadge.setFont(lblStatusBadge.getFont().deriveFont(Font.BOLD, 11.5f));
        lblStatusBadge.setBorder(new EmptyBorder(4, 10, 4, 10));

        titleGroup.add(title);
        titleGroup.add(lblStatusBadge);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionGroup.setOpaque(false);

        btnStart = new JButton("▶ Iniciar Prueba");
        btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD, 13f));
        btnStart.setBackground(new Color(16, 185, 129));
        btnStart.setForeground(Color.WHITE);
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStart.setPreferredSize(new Dimension(150, 38));
        btnStart.putClientProperty("JButton.buttonType", "roundRect");

        btnStopAll = new JButton("⏹ STOP ALL");
        btnStopAll.setFont(btnStopAll.getFont().deriveFont(Font.BOLD, 13f));
        btnStopAll.setBackground(new Color(239, 68, 68));
        btnStopAll.setForeground(Color.WHITE);
        btnStopAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStopAll.setPreferredSize(new Dimension(140, 38));
        btnStopAll.putClientProperty("JButton.buttonType", "roundRect");
        btnStopAll.setEnabled(false);

        btnExport = new JButton("📥 Exportar Informe");
        btnExport.setFont(btnExport.getFont().deriveFont(Font.BOLD, 12.5f));
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.setPreferredSize(new Dimension(170, 38));
        btnExport.putClientProperty("JButton.buttonType", "roundRect");
        btnExport.setEnabled(false);

        actionGroup.add(btnStart);
        actionGroup.add(btnStopAll);
        actionGroup.add(btnExport);

        topBar.add(titleGroup, BorderLayout.WEST);
        topBar.add(actionGroup, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- KPI CARDS GRID ---
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 18, 18));
        cardsGrid.setOpaque(false);

        lblConnectedBots = new JLabel("0 / 0", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("JUGADORES SIMULADOS", lblConnectedBots, new Color(16, 185, 129)));

        lblAvgLatency = new JLabel("0,0 ms", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("LATENCIA MEDIA (RTT)", lblAvgLatency, new Color(59, 130, 246)));

        lblSuccessRate = new JLabel("100,0%", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("TASA DE ÉXITO", lblSuccessRate, new Color(168, 85, 247)));

        lblElapsedTime = new JLabel("00:00", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("TIEMPO TRANSCURRIDO", lblElapsedTime, new Color(234, 179, 8)));

        lblPackets = new JLabel("0 / 0", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("PAQUETES (TX / RX)", lblPackets, new Color(249, 115, 22)));

        lblTargetBots = new JLabel("En espera", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("ESTADO DE CARGA", lblTargetBots, new Color(107, 114, 128)));

        add(cardsGrid, BorderLayout.CENTER);

        // --- BOTTOM HINT ---
        JPanel bottomHint = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomHint.setOpaque(false);
        JLabel hint = new JLabel("💡 Consejo: En la pestaña 'Configuración' puedes activar la detección automática del servidor y modos Velocity.");
        hint.setForeground(new Color(156, 163, 175));
        hint.setFont(hint.getFont().deriveFont(12f));
        bottomHint.add(hint);
        add(bottomHint, BorderLayout.SOUTH);

        btnStart.addActionListener(e -> openConfigAction.run());
        btnStopAll.addActionListener(e -> orchestrator.stopTest("Detención manual desde el Dashboard"));
        btnExport.addActionListener(e -> exportReportAction.run());
    }

    private JPanel createKpiCard(String titleText, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(new Color(27, 30, 38));
        card.setBorder(new CompoundBorder(
            BorderFactory.createCompoundBorder(
                new LineBorder(new Color(42, 47, 60), 1, true),
                BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor)
            ),
            new EmptyBorder(16, 18, 16, 18)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
        title.setForeground(new Color(156, 163, 175));

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 26f));
        valueLabel.setForeground(new Color(243, 244, 246));

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void updateMetrics(TestMetrics metrics) {
        SwingUtilities.invokeLater(() -> {
            lblConnectedBots.setText(String.format("%d / %d", metrics.getConnectedBots(), metrics.getTargetBots()));
            lblAvgLatency.setText(String.format("%.1f ms (p95: %d ms)", metrics.getAvgLatencyMs(), (int) metrics.getP95LatencyMs()));
            lblSuccessRate.setText(String.format("%.1f%%", metrics.getSuccessRate()));

            long s = metrics.getElapsedSeconds();
            lblElapsedTime.setText(String.format("%02d:%02d", s / 60, s % 60));

            lblPackets.setText(String.format("%d / %d", metrics.getTotalPacketsSent(), metrics.getTotalPacketsReceived()));

            if (metrics.getConnectingBots() > 0) {
                lblTargetBots.setText(metrics.getConnectingBots() + " conectando...");
                lblTargetBots.setForeground(new Color(234, 179, 8));
            } else if (metrics.getConnectedBots() == metrics.getTargetBots() && metrics.getTargetBots() > 0) {
                lblTargetBots.setText("Carga estable");
                lblTargetBots.setForeground(new Color(16, 185, 129));
            } else if (metrics.getFailedBots() > 0) {
                lblTargetBots.setText(metrics.getFailedBots() + " fallidos");
                lblTargetBots.setForeground(new Color(239, 68, 68));
            } else {
                lblTargetBots.setText("Activo");
                lblTargetBots.setForeground(new Color(243, 244, 246));
            }
        });
    }

    public void setTestRunning(boolean running) {
        SwingUtilities.invokeLater(() -> {
            btnStart.setEnabled(!running);
            btnStopAll.setEnabled(running);
            btnExport.setEnabled(!running);

            if (running) {
                lblStatusBadge.setText(" ● EN PRUEBA ");
                lblStatusBadge.setBackground(new Color(16, 185, 129));
                lblStatusBadge.setForeground(Color.WHITE);
            } else {
                lblStatusBadge.setText(" ● FINALIZADA ");
                lblStatusBadge.setBackground(new Color(55, 65, 81));
                lblStatusBadge.setForeground(new Color(209, 213, 219));
            }
        });
    }
}
