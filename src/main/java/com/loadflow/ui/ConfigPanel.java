package com.loadflow.ui;

import com.loadflow.model.TestConfig;
import com.loadflow.protocol.MinecraftVersion;
import com.loadflow.protocol.ServerPingResult;
import com.loadflow.protocol.ServerPingService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.function.Consumer;

public class ConfigPanel extends JPanel {
    private final TestConfig config;
    private final Consumer<TestConfig> onStartTest;

    private JTextField txtHost;
    private JSpinner spnPort;
    private JButton btnDetectServer;
    private JLabel lblDetectResult;

    private JComboBox<String> cmbVersion;
    private JComboBox<String> cmbTargetType;
    private JTextField txtInitialCommand;

    private JSpinner spnTargetBots;
    private JSpinner spnRampRate;
    private JSpinner spnDuration;
    private JTextField txtPrefix;
    private JComboBox<String> cmbProfile;
    private JCheckBox chkCircuitBreaker;
    private JSpinner spnThreshold;

    private final String[] VERSION_OPTIONS = new String[]{
        "⭐ Auto-Detectar desde el Servidor",
        "Minecraft 1.21.4 (Protocolo 769)",
        "Minecraft 1.21.2 / 1.21.3 (Protocolo 768)",
        "Minecraft 1.21 / 1.21.1 (Protocolo 767)",
        "Minecraft 1.20.5 / 1.20.6 (Protocolo 766)",
        "Minecraft 1.20.4 (Protocolo 765)",
        "Minecraft 1.20.2 (Protocolo 764)",
        "Minecraft 1.20 / 1.20.1 (Protocolo 763)",
        "Minecraft 1.19.4 (Protocolo 762)",
        "Minecraft 1.19.2 (Protocolo 760)",
        "Minecraft 1.18.2 (Protocolo 758)",
        "Minecraft 1.17.1 (Protocolo 756)",
        "Minecraft 1.16.5 (Protocolo 754)"
    };

    public ConfigPanel(TestConfig config, Consumer<TestConfig> onStartTest) {
        this.config = config;
        this.onStartTest = onStartTest;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(15, 25, 15, 25));

        initComponents();
    }

    private void initComponents() {
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        // 1. Destino de Red y Auto-Detección
        JPanel pnlTarget = new JPanel(new BorderLayout(10, 10));
        pnlTarget.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "1. Servidor de Destino y Detección Automática", TitledBorder.LEFT, TitledBorder.TOP));

        JPanel targetGrid = new JPanel(new GridLayout(2, 3, 10, 10));
        targetGrid.add(new JLabel("Host / Dirección IP:"));
        txtHost = new JTextField(config.getHost());
        targetGrid.add(txtHost);

        btnDetectServer = new JButton("🔍 Auto-Detectar Servidor");
        btnDetectServer.setFont(btnDetectServer.getFont().deriveFont(Font.BOLD, 12f));
        btnDetectServer.setBackground(new Color(52, 152, 219));
        btnDetectServer.setForeground(Color.WHITE);
        targetGrid.add(btnDetectServer);

        targetGrid.add(new JLabel("Puerto:"));
        spnPort = new JSpinner(new SpinnerNumberModel(config.getPort(), 1, 65535, 1));
        targetGrid.add(spnPort);

        lblDetectResult = new JLabel("Haz clic en 'Auto-Detectar Servidor' para probar ping y versión.");
        lblDetectResult.setForeground(Color.GRAY);
        targetGrid.add(lblDetectResult);

        pnlTarget.add(targetGrid, BorderLayout.CENTER);
        formContainer.add(pnlTarget);
        formContainer.add(Box.createVerticalStrut(10));

        // 2. Multiversión y Soporte de Proxies
        JPanel pnlVersion = new JPanel(new GridLayout(3, 2, 12, 10));
        pnlVersion.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "2. Compatibilidad Multiversión y Proxies (Velocity / BungeeCord)", TitledBorder.LEFT, TitledBorder.TOP));

        pnlVersion.add(new JLabel("Versión del Protocolo:"));
        cmbVersion = new JComboBox<>(VERSION_OPTIONS);
        cmbVersion.setEditable(true); // Permite escribir versiones o protocolos personalizados
        cmbVersion.setSelectedIndex(4); // 1.20.5 por defecto
        pnlVersion.add(cmbVersion);

        pnlVersion.add(new JLabel("Tipo de Destino / Red:"));
        cmbTargetType = new JComboBox<>(new String[]{
            "Directo (Paper / Purpur / Spigot)",
            "Proxy Velocity",
            "Proxy BungeeCord / Waterfall"
        });
        pnlVersion.add(cmbTargetType);

        pnlVersion.add(new JLabel("Comando inicial tras conectar (opcional, ej: /server lobby):"));
        txtInitialCommand = new JTextField(config.getInitialCommand());
        pnlVersion.add(txtInitialCommand);

        formContainer.add(pnlVersion);
        formContainer.add(Box.createVerticalStrut(10));

        // 3. Parámetros de Carga
        JPanel pnlLoad = new JPanel(new GridLayout(4, 2, 12, 8));
        pnlLoad.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "3. Parámetros de Carga y Simulación", TitledBorder.LEFT, TitledBorder.TOP));

        pnlLoad.add(new JLabel("Cantidad Objetivo de Bots:"));
        spnTargetBots = new JSpinner(new SpinnerNumberModel(config.getTargetBots(), 1, 5000, 5));
        pnlLoad.add(spnTargetBots);

        pnlLoad.add(new JLabel("Rampa (Bots Nuevos por Segundo):"));
        spnRampRate = new JSpinner(new SpinnerNumberModel(config.getRampRatePerSec(), 1, 200, 1));
        pnlLoad.add(spnRampRate);

        pnlLoad.add(new JLabel("Duración Máxima de la Prueba (segundos, 0 = ilimitado):"));
        spnDuration = new JSpinner(new SpinnerNumberModel(config.getDurationSeconds(), 0, 86400, 30));
        pnlLoad.add(spnDuration);

        pnlLoad.add(new JLabel("Prefijo de Nombre de Bots:"));
        txtPrefix = new JTextField(config.getBotPrefix());
        pnlLoad.add(txtPrefix);

        formContainer.add(pnlLoad);
        formContainer.add(Box.createVerticalStrut(10));

        // 4. Comportamiento y Seguridad
        JPanel pnlSafety = new JPanel(new GridLayout(3, 2, 12, 8));
        pnlSafety.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "4. Perfil de Comportamiento y Circuit Breaker", TitledBorder.LEFT, TitledBorder.TOP));

        pnlSafety.add(new JLabel("Perfil de Jugador:"));
        cmbProfile = new JComboBox<>(new String[]{"AFK (Rotación ligera / Anti-inactividad)", "EXPLORER (Movimiento y pasos aleatorios)"});
        pnlSafety.add(cmbProfile);

        pnlSafety.add(new JLabel("Detener automáticamente si hay demasiados errores:"));
        chkCircuitBreaker = new JCheckBox("Activar Circuit Breaker", config.isAutoStopOnError());
        pnlSafety.add(chkCircuitBreaker);

        pnlSafety.add(new JLabel("Umbral Máximo de Fallos (%):"));
        spnThreshold = new JSpinner(new SpinnerNumberModel(config.getMaxErrorThresholdPct(), 5.0, 100.0, 5.0));
        pnlSafety.add(spnThreshold);

        formContainer.add(pnlSafety);

        add(new JScrollPane(formContainer), BorderLayout.CENTER);

        // Barra inferior con botón de inicio
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLaunch = new JButton("▶ Iniciar Prueba de Carga");
        btnLaunch.setFont(btnLaunch.getFont().deriveFont(Font.BOLD, 14f));
        btnLaunch.setBackground(new Color(40, 167, 69));
        btnLaunch.setForeground(Color.WHITE);
        btnLaunch.setPreferredSize(new Dimension(280, 42));

        btnLaunch.addActionListener(e -> {
            applyValuesToConfig();
            onStartTest.accept(config);
        });

        bottomBar.add(btnLaunch);
        add(bottomBar, BorderLayout.SOUTH);

        // Action de detección automática
        btnDetectServer.addActionListener(e -> performServerDetection());
    }

    private void performServerDetection() {
        String host = txtHost.getText().trim();
        int port = (Integer) spnPort.getValue();

        btnDetectServer.setEnabled(false);
        lblDetectResult.setText("Consultando estado del servidor...");
        lblDetectResult.setForeground(Color.YELLOW);

        ServerPingService.pingServerAsync(host, port).thenAccept(result -> SwingUtilities.invokeLater(() -> {
            btnDetectServer.setEnabled(true);
            if (result.isSuccess()) {
                lblDetectResult.setText(result.getSummary());
                lblDetectResult.setForeground(new Color(46, 204, 113));

                // Seleccionar versión en el combo
                MinecraftVersion detected = result.getMatchedVersion();
                for (int i = 0; i < VERSION_OPTIONS.length; i++) {
                    if (VERSION_OPTIONS[i].contains(detected.getName()) || VERSION_OPTIONS[i].contains(String.valueOf(detected.getProtocol()))) {
                        cmbVersion.setSelectedIndex(i);
                        break;
                    }
                }

                // Ajustar proxy si se detecta Velocity o BungeeCord
                if (result.isVelocity()) {
                    cmbTargetType.setSelectedIndex(1); // Velocity
                } else if (result.isBungeeCord()) {
                    cmbTargetType.setSelectedIndex(2); // BungeeCord
                } else {
                    cmbTargetType.setSelectedIndex(0); // Direct
                }
            } else {
                lblDetectResult.setText("❌ " + result.getErrorMessage());
                lblDetectResult.setForeground(new Color(231, 76, 60));
            }
        }));
    }

    private void applyValuesToConfig() {
        config.setHost(txtHost.getText().trim());
        config.setPort((Integer) spnPort.getValue());

        String versionSel = cmbVersion.getSelectedItem() != null ? cmbVersion.getSelectedItem().toString() : "";
        config.setVersion(MinecraftVersion.fromString(versionSel));
        config.setVersionInput(versionSel);

        int targetIdx = cmbTargetType.getSelectedIndex();
        config.setTargetType(targetIdx == 1 ? "VELOCITY" : (targetIdx == 2 ? "BUNGEECORD" : "DIRECT"));
        config.setInitialCommand(txtInitialCommand.getText().trim());

        config.setTargetBots((Integer) spnTargetBots.getValue());
        config.setRampRatePerSec((Integer) spnRampRate.getValue());
        config.setDurationSeconds((Integer) spnDuration.getValue());
        config.setBotPrefix(txtPrefix.getText().trim());
        config.setBehaviorProfile(cmbProfile.getSelectedIndex() == 1 ? "EXPLORER" : "AFK");
        config.setAutoStopOnError(chkCircuitBreaker.isSelected());
        config.setMaxErrorThresholdPct((Double) spnThreshold.getValue());
    }
}
