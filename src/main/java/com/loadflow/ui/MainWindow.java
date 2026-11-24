package com.loadflow.ui;

import com.loadflow.engine.BotManager;
import com.loadflow.engine.TestOrchestrator;
import com.loadflow.export.ReportExporter;
import com.loadflow.model.TestConfig;
import com.loadflow.model.TestMetrics;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

public class MainWindow extends JFrame {
    private final TestConfig config = new TestConfig();
    private final BotManager botManager = new BotManager();
    private final TestOrchestrator orchestrator = new TestOrchestrator(botManager);

    private final JTabbedPane tabbedPane = new JTabbedPane();
    private DashboardPanel dashboardPanel;
    private BotMonitorPanel botMonitorPanel;
    private ConfigPanel configPanel;
    private LogConsolePanel logConsolePanel;

    private JLabel lblFooterStatus;
    private JLabel lblFooterMemory;
    private JLabel mcBadge;
    private TestMetrics lastMetrics = new TestMetrics();

    public MainWindow() {
        setTitle("LoadFlow — Minecraft Load Tester (v1.0)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        initComponents();
        setupOrchestratorHooks();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                orchestrator.shutdown();
            }
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Header superior
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(12, 20, 12, 20));
        headerPanel.setBackground(new Color(28, 30, 36));

        JLabel appTitle = new JLabel("⚡ LoadFlow — Minecraft Load Tester");
        appTitle.setFont(appTitle.getFont().deriveFont(Font.BOLD, 16f));
        appTitle.setForeground(Color.WHITE);

        mcBadge = new JLabel(" Multiversión: 1.16 a 1.21.4 | Detección Automática & Proxies ");
        mcBadge.setOpaque(true);
        mcBadge.setBackground(new Color(45, 52, 64));
        mcBadge.setForeground(new Color(160, 174, 192));
        mcBadge.setFont(mcBadge.getFont().deriveFont(Font.BOLD, 11f));

        headerPanel.add(appTitle, BorderLayout.WEST);
        headerPanel.add(mcBadge, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Pestañas
        dashboardPanel = new DashboardPanel(
            orchestrator,
            () -> tabbedPane.setSelectedIndex(2), // ir a configuración
            this::exportReportDialog
        );

        botMonitorPanel = new BotMonitorPanel();
        configPanel = new ConfigPanel(config, (cfg) -> {
            mcBadge.setText(" Protocolo: " + cfg.getVersion().getName() + " (" + cfg.getProtocolVersion() + ") | " + cfg.getTargetType() + " ");
            mcBadge.setBackground(new Color(30, 70, 45));
            mcBadge.setForeground(new Color(144, 238, 144));
            tabbedPane.setSelectedIndex(0); // volver a Dashboard
            orchestrator.startTest(cfg);
        });
        logConsolePanel = new LogConsolePanel();

        tabbedPane.addTab("  📊 Dashboard & Control  ", dashboardPanel);
        tabbedPane.addTab("  🤖 Monitor de Bots  ", botMonitorPanel);
        tabbedPane.addTab("  ⚙️ Configuración  ", configPanel);
        tabbedPane.addTab("  📜 Registro de Eventos  ", logConsolePanel);

        add(tabbedPane, BorderLayout.CENTER);

        // Barra inferior de estado
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBorder(new EmptyBorder(6, 15, 6, 15));
        footerPanel.setBackground(new Color(24, 26, 31));

        lblFooterStatus = new JLabel("Listo para iniciar pruebas en modo laboratorio.");
        lblFooterStatus.setForeground(Color.GRAY);

        lblFooterMemory = new JLabel("JVM RAM: " + getMemoryUsage());
        lblFooterMemory.setForeground(Color.GRAY);

        footerPanel.add(lblFooterStatus, BorderLayout.WEST);
        footerPanel.add(lblFooterMemory, BorderLayout.EAST);
        add(footerPanel, BorderLayout.SOUTH);

        // Timer para actualizar memoria en footer
        Timer memTimer = new Timer(3000, e -> lblFooterMemory.setText("JVM RAM: " + getMemoryUsage()));
        memTimer.start();
    }

    private void setupOrchestratorHooks() {
        orchestrator.setLogListener(entry -> logConsolePanel.appendLog(entry));

        orchestrator.setMetricsListener(metrics -> {
            this.lastMetrics = metrics;
            dashboardPanel.updateMetrics(metrics);
            botMonitorPanel.updateSessions(botManager.getSessions());

            lblFooterStatus.setText(String.format("Bots: %d conectados / %d objetivo | Éxito: %.1f%%",
                metrics.getConnectedBots(), metrics.getTargetBots(), metrics.getSuccessRate()));
        });

        orchestrator.setStateListener(running -> {
            dashboardPanel.setTestRunning(running);
            if (!running) {
                lblFooterStatus.setText("Prueba finalizada. Resultados listos para exportar.");
            }
        });
    }

    private void exportReportDialog() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Selecciona la carpeta para guardar los informes");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = chooser.getSelectedFile();
            try {
                File jsonFile = ReportExporter.exportJson(selectedDir, config, lastMetrics, botManager.getSessions());
                File csvFile = ReportExporter.exportCsv(selectedDir, botManager.getSessions());

                JOptionPane.showMessageDialog(this,
                    "Informes exportados exitosamente:\n- " + jsonFile.getName() + "\n- " + csvFile.getName(),
                    "Exportación Completa", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error al exportar los reportes: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String getMemoryUsage() {
        Runtime rt = Runtime.getRuntime();
        long usedMb = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long maxMb = rt.maxMemory() / (1024 * 1024);
        return usedMb + " MB / " + maxMb + " MB";
    }
}
