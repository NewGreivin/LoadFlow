package com.loadflow.ui;

import com.loadflow.engine.TestOrchestrator;
import com.loadflow.model.TestConfig;
import com.loadflow.model.TestMetrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
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

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        initComponents();
    }

    private void initComponents() {
        // --- TOP TOOLBAR & QUICK ACTIONS ---
        JPanel topBar = new JPanel(new BorderLayout());
        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JLabel title = new JLabel("Panel de Control de Carga");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        lblStatusBadge = new JLabel(" EN ESPERA ");
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(60, 65, 75));
        lblStatusBadge.setForeground(Color.LIGHT_GRAY);
        lblStatusBadge.setFont(lblStatusBadge.getFont().deriveFont(Font.BOLD, 12f));
        titleGroup.add(title);
        titleGroup.add(lblStatusBadge);

        JPanel actionGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        btnStart = new JButton("▶ Iniciar Prueba");
        btnStart.setFont(btnStart.getFont().deriveFont(Font.BOLD, 13f));
        btnStart.setBackground(new Color(40, 167, 69));
        btnStart.setForeground(Color.WHITE);

        btnStopAll = new JButton("⏹ STOP ALL");
        btnStopAll.setFont(btnStopAll.getFont().deriveFont(Font.BOLD, 13f));
        btnStopAll.setBackground(new Color(220, 53, 69));
        btnStopAll.setForeground(Color.WHITE);
        btnStopAll.setEnabled(false);

        btnExport = new JButton("📥 Exportar Informe");
        btnExport.setEnabled(false);

        actionGroup.add(btnStart);
        actionGroup.add(btnStopAll);
        actionGroup.add(btnExport);

        topBar.add(titleGroup, BorderLayout.WEST);
        topBar.add(actionGroup, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // --- KPI CARDS GRID ---
        JPanel cardsGrid = new JPanel(new GridLayout(2, 3, 15, 15));

        lblConnectedBots = new JLabel("0 / 0", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("JUGADORES SIMULADOS", lblConnectedBots, new Color(46, 204, 113)));

        lblAvgLatency = new JLabel("0 ms", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("LATENCIA MEDIA (RTT)", lblAvgLatency, new Color(52, 152, 219)));

        lblSuccessRate = new JLabel("100.0%", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("TASA DE ÉXITO", lblSuccessRate, new Color(155, 89, 182)));

        lblElapsedTime = new JLabel("00:00", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("TIEMPO TRANSCURRIDO", lblElapsedTime, new Color(241, 196, 15)));

        lblPackets = new JLabel("0 / 0", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("PAQUETES (TX / RX)", lblPackets, new Color(230, 126, 34)));

        lblTargetBots = new JLabel("En espera", SwingConstants.CENTER);
        cardsGrid.add(createKpiCard("ESTADO DE CARGA", lblTargetBots, new Color(149, 165, 166)));

        add(cardsGrid, BorderLayout.CENTER);

        // --- BOTTOM INFO / HINTS ---
        JPanel bottomHint = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel hint = new JLabel("💡 Consejo: Puedes ajustar la velocidad de entrada de bots y el límite en la pestaña 'Configuración'.");
        hint.setForeground(Color.GRAY);
        bottomHint.add(hint);
        add(bottomHint, BorderLayout.SOUTH);

        // Listeners
        btnStart.addActionListener(e -> openConfigAction.run());
        btnStopAll.addActionListener(e -> orchestrator.stopTest("Detención manual desde el Dashboard"));
        btnExport.addActionListener(e -> exportReportAction.run());
    }

    private JPanel createKpiCard(String titleText, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, accentColor),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel title = new JLabel(titleText);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 11f));
        title.setForeground(Color.GRAY);

        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 26f));

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
            } else if (metrics.getConnectedBots() == metrics.getTargetBots() && metrics.getTargetBots() > 0) {
                lblTargetBots.setText("Carga estable");
            } else if (metrics.getFailedBots() > 0) {
                lblTargetBots.setText(metrics.getFailedBots() + " fallidos");
            } else {
                lblTargetBots.setText("Activo");
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
                lblStatusBadge.setBackground(new Color(40, 167, 69));
                lblStatusBadge.setForeground(Color.WHITE);
            } else {
                lblStatusBadge.setText(" FINALIZADA ");
                lblStatusBadge.setBackground(new Color(60, 65, 75));
                lblStatusBadge.setForeground(Color.LIGHT_GRAY);
            }
        });
    }
}
