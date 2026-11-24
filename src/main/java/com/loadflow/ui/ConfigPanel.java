package com.loadflow.ui;

import com.loadflow.model.TestConfig;
import com.loadflow.protocol.MinecraftVersion;
import com.loadflow.protocol.ServerPingResult;
import com.loadflow.protocol.ServerPingService;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.function.Consumer;

public class ConfigPanel extends JPanel {
    private final TestConfig config;
    private final Consumer<TestConfig> onStartTest;

    private JTextField txtHost;
    private JSpinner spnPort;
    private JButton btnDetectServer;
    private JPanel pnlDetectResult;
    private JLabel lblDetectIcon;
    private JLabel lblDetectText;

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

        setLayout(new BorderLayout());
        setBackground(new Color(20, 22, 27));

        initComponents();
    }

    private void initComponents() {
        // Contenedor principal con scroll suave
        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setBorder(new EmptyBorder(20, 35, 20, 35));
        mainContent.setBackground(new Color(20, 22, 27));

        // 1. CARD: SERVIDOR Y DETECCIÓN
        mainContent.add(createTargetCard());
        mainContent.add(Box.createVerticalStrut(18));

        // 2. CARD: PROTOCOLO & PROXIES
        mainContent.add(createProtocolCard());
        mainContent.add(Box.createVerticalStrut(18));

        // 3. CARD: PARÁMETROS DE CARGA
        mainContent.add(createLoadParamsCard());
        mainContent.add(Box.createVerticalStrut(18));

        // 4. CARD: COMPORTAMIENTO & SEGURIDAD
        mainContent.add(createSafetyCard());
        mainContent.add(Box.createVerticalStrut(20));

        JScrollPane scrollPane = new JScrollPane(mainContent);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(20, 22, 27));
        add(scrollPane, BorderLayout.CENTER);

        // BARRA INFERIOR MODERNA
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBackground(new Color(26, 29, 36));
        bottomBar.setBorder(new CompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(42, 47, 58)),
            new EmptyBorder(14, 35, 14, 35)
        ));

        JLabel lblSummary = new JLabel("💡 Configura los parámetros y haz clic en Iniciar para ejecutar la rampa.");
        lblSummary.setForeground(new Color(156, 163, 175));
        lblSummary.setFont(lblSummary.getFont().deriveFont(12.5f));
        bottomBar.add(lblSummary, BorderLayout.WEST);

        JButton btnLaunch = new JButton("▶ Iniciar Prueba de Carga");
        btnLaunch.setFont(btnLaunch.getFont().deriveFont(Font.BOLD, 14f));
        btnLaunch.setBackground(new Color(16, 185, 129));
        btnLaunch.setForeground(Color.WHITE);
        btnLaunch.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLaunch.setPreferredSize(new Dimension(260, 44));
        btnLaunch.putClientProperty("JButton.buttonType", "roundRect");

        btnLaunch.addActionListener(e -> {
            applyValuesToConfig();
            onStartTest.accept(config);
        });

        bottomBar.add(btnLaunch, BorderLayout.EAST);
        add(bottomBar, BorderLayout.SOUTH);

        btnDetectServer.addActionListener(e -> performServerDetection());
    }

    private JPanel createTargetCard() {
        JPanel card = createCardContainer("🌐 1. Servidor de Destino & Detección Automática");
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Fila 1: Host
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
        content.add(createFieldLabel("Host / IP:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.55;
        txtHost = new JTextField(config.getHost());
        txtHost.setPreferredSize(new Dimension(220, 36));
        txtHost.putClientProperty("JTextField.placeholderText", "127.0.0.1 o mc.servidor.com");
        content.add(txtHost, gbc);

        // Puerto
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        content.add(createFieldLabel("Puerto:"), gbc);

        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 0.15;
        spnPort = new JSpinner(new SpinnerNumberModel(config.getPort(), 1, 65535, 1));
        spnPort.setPreferredSize(new Dimension(90, 36));
        content.add(spnPort, gbc);

        // Botón Detectar
        gbc.gridx = 4; gbc.gridy = 0; gbc.weightx = 0.3;
        btnDetectServer = new JButton("🔍 Auto-Detectar Servidor");
        btnDetectServer.setFont(btnDetectServer.getFont().deriveFont(Font.BOLD, 12f));
        btnDetectServer.setBackground(new Color(37, 99, 235));
        btnDetectServer.setForeground(Color.WHITE);
        btnDetectServer.setPreferredSize(new Dimension(190, 36));
        btnDetectServer.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDetectServer.putClientProperty("JButton.buttonType", "roundRect");
        content.add(btnDetectServer, gbc);

        // Fila 2: Chip de resultado de detección
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5; gbc.weightx = 1.0;
        gbc.insets = new Insets(10, 8, 4, 8);

        pnlDetectResult = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlDetectResult.setBackground(new Color(28, 32, 42));
        pnlDetectResult.setBorder(new CompoundBorder(
            new LineBorder(new Color(45, 52, 68), 1, true),
            new EmptyBorder(2, 8, 2, 8)
        ));

        lblDetectIcon = new JLabel("⚡");
        lblDetectText = new JLabel("Listo para probar conexión. Haz clic en 'Auto-Detectar Servidor' para identificar versión y proxy.");
        lblDetectText.setForeground(new Color(156, 163, 175));
        lblDetectText.setFont(lblDetectText.getFont().deriveFont(12f));

        pnlDetectResult.add(lblDetectIcon);
        pnlDetectResult.add(lblDetectText);
        content.add(pnlDetectResult, gbc);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProtocolCard() {
        JPanel card = createCardContainer("🔄 2. Compatibilidad Multiversión & Redes / Proxies");
        JPanel content = new JPanel(new GridLayout(2, 2, 20, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Versión
        JPanel pnlVer = new JPanel(new BorderLayout(0, 4));
        pnlVer.setOpaque(false);
        pnlVer.add(createFieldLabel("Versión del Protocolo:"), BorderLayout.NORTH);
        cmbVersion = new JComboBox<>(VERSION_OPTIONS);
        cmbVersion.setPreferredSize(new Dimension(280, 36));
        cmbVersion.setEditable(true);
        cmbVersion.setSelectedIndex(0); // Auto-detectar por defecto
        pnlVer.add(cmbVersion, BorderLayout.CENTER);
        content.add(pnlVer);

        // Tipo de Red
        JPanel pnlNet = new JPanel(new BorderLayout(0, 4));
        pnlNet.setOpaque(false);
        pnlNet.add(createFieldLabel("Tipo de Red / Destino:"), BorderLayout.NORTH);
        cmbTargetType = new JComboBox<>(new String[]{
            "Directo (Paper / Purpur / Spigot)",
            "Proxy Velocity",
            "Proxy BungeeCord / Waterfall"
        });
        cmbTargetType.setPreferredSize(new Dimension(280, 36));
        pnlNet.add(cmbTargetType, BorderLayout.CENTER);
        content.add(pnlNet);

        // Comando Inicial
        JPanel pnlCmd = new JPanel(new BorderLayout(0, 4));
        pnlCmd.setOpaque(false);
        pnlCmd.add(createFieldLabel("Comando inicial tras conectar (opcional):"), BorderLayout.NORTH);
        txtInitialCommand = new JTextField(config.getInitialCommand());
        txtInitialCommand.setPreferredSize(new Dimension(280, 36));
        txtInitialCommand.putClientProperty("JTextField.placeholderText", "ej: /server lobby o /server modalidad");
        pnlCmd.add(txtInitialCommand, BorderLayout.CENTER);
        content.add(pnlCmd);

        // Explicación
        JPanel pnlHint = new JPanel(new BorderLayout(0, 4));
        pnlHint.setOpaque(false);
        pnlHint.add(createFieldLabel("Modo de Operación:"), BorderLayout.NORTH);
        JLabel lblHint = new JLabel("Soporta desde 1.16.5 hasta 1.21.4+. En proxies, los bots se moverán con el comando inicial.");
        lblHint.setForeground(new Color(156, 163, 175));
        lblHint.setFont(lblHint.getFont().deriveFont(11.5f));
        pnlHint.add(lblHint, BorderLayout.CENTER);
        content.add(pnlHint);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createLoadParamsCard() {
        JPanel card = createCardContainer("⚡ 3. Parámetros de Carga & Simulación");
        JPanel content = new JPanel(new GridLayout(2, 2, 20, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Objetivo Bots
        JPanel pnlBots = new JPanel(new BorderLayout(0, 4));
        pnlBots.setOpaque(false);
        pnlBots.add(createFieldLabel("Cantidad Objetivo de Bots:"), BorderLayout.NORTH);
        spnTargetBots = new JSpinner(new SpinnerNumberModel(config.getTargetBots(), 1, 10000, 10));
        spnTargetBots.setPreferredSize(new Dimension(200, 36));
        pnlBots.add(spnTargetBots, BorderLayout.CENTER);
        content.add(pnlBots);

        // Rampa
        JPanel pnlRamp = new JPanel(new BorderLayout(0, 4));
        pnlRamp.setOpaque(false);
        pnlRamp.add(createFieldLabel("Rampa de Conexión (Bots por segundo):"), BorderLayout.NORTH);
        spnRampRate = new JSpinner(new SpinnerNumberModel(config.getRampRatePerSec(), 1, 500, 1));
        spnRampRate.setPreferredSize(new Dimension(200, 36));
        pnlRamp.add(spnRampRate, BorderLayout.CENTER);
        content.add(pnlRamp);

        // Duración
        JPanel pnlDur = new JPanel(new BorderLayout(0, 4));
        pnlDur.setOpaque(false);
        pnlDur.add(createFieldLabel("Duración Máxima (segundos, 0 = sin límite):"), BorderLayout.NORTH);
        spnDuration = new JSpinner(new SpinnerNumberModel(config.getDurationSeconds(), 0, 86400, 30));
        spnDuration.setPreferredSize(new Dimension(200, 36));
        pnlDur.add(spnDuration, BorderLayout.CENTER);
        content.add(pnlDur);

        // Prefijo
        JPanel pnlPre = new JPanel(new BorderLayout(0, 4));
        pnlPre.setOpaque(false);
        pnlPre.add(createFieldLabel("Prefijo de Nombre de Bots:"), BorderLayout.NORTH);
        txtPrefix = new JTextField(config.getBotPrefix());
        txtPrefix.setPreferredSize(new Dimension(200, 36));
        pnlPre.add(txtPrefix, BorderLayout.CENTER);
        content.add(pnlPre);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createSafetyCard() {
        JPanel card = createCardContainer("🛡️ 4. Perfil de Comportamiento & Circuit Breaker");
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 12));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(6, 8, 6, 8));

        // Perfil
        JPanel pnlProf = new JPanel(new BorderLayout(0, 4));
        pnlProf.setOpaque(false);
        pnlProf.add(createFieldLabel("Perfil de Comportamiento:"), BorderLayout.NORTH);
        cmbProfile = new JComboBox<>(new String[]{
            "AFK (Rotación ligera / Anti-inactividad)",
            "EXPLORER (Movimiento y pasos aleatorios)"
        });
        cmbProfile.setPreferredSize(new Dimension(280, 36));
        pnlProf.add(cmbProfile, BorderLayout.CENTER);
        content.add(pnlProf);

        // Circuit Breaker
        JPanel pnlCB = new JPanel(new BorderLayout(0, 4));
        pnlCB.setOpaque(false);
        pnlCB.add(createFieldLabel("Protección Automática (Circuit Breaker):"), BorderLayout.NORTH);

        JPanel cbControls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        cbControls.setOpaque(false);
        chkCircuitBreaker = new JCheckBox("Activar parada si errores >", config.isAutoStopOnError());
        chkCircuitBreaker.setOpaque(false);

        spnThreshold = new JSpinner(new SpinnerNumberModel(config.getMaxErrorThresholdPct(), 5.0, 100.0, 5.0));
        spnThreshold.setPreferredSize(new Dimension(75, 34));

        JLabel lblPct = new JLabel("%");
        lblPct.setForeground(Color.GRAY);

        cbControls.add(chkCircuitBreaker);
        cbControls.add(spnThreshold);
        cbControls.add(lblPct);

        pnlCB.add(cbControls, BorderLayout.CENTER);
        content.add(pnlCB);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    private JPanel createCardContainer(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(new Color(27, 30, 38));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(42, 47, 60), 1, true),
            new EmptyBorder(14, 18, 16, 18)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD, 13.5f));
        lblTitle.setForeground(new Color(229, 231, 235));
        card.add(lblTitle, BorderLayout.NORTH);

        return card;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 11.5f));
        label.setForeground(new Color(156, 163, 175));
        return label;
    }

    private void performServerDetection() {
        String host = txtHost.getText().trim();
        int port = (Integer) spnPort.getValue();

        btnDetectServer.setEnabled(false);
        lblDetectIcon.setText("⏳");
        lblDetectText.setText("Consultando ping y versión del servidor...");
        lblDetectText.setForeground(new Color(234, 179, 8));

        ServerPingService.pingServerAsync(host, port).thenAccept(result -> SwingUtilities.invokeLater(() -> {
            btnDetectServer.setEnabled(true);
            if (result.isSuccess()) {
                lblDetectIcon.setText("✔");
                lblDetectText.setText(result.getSummary());
                lblDetectText.setForeground(new Color(52, 211, 153));
                pnlDetectResult.setBackground(new Color(16, 38, 30));
                pnlDetectResult.setBorder(new CompoundBorder(
                    new LineBorder(new Color(16, 185, 129), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
                ));

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
                    cmbTargetType.setSelectedIndex(1);
                } else if (result.isBungeeCord()) {
                    cmbTargetType.setSelectedIndex(2);
                } else {
                    cmbTargetType.setSelectedIndex(0);
                }
            } else {
                lblDetectIcon.setText("❌");
                lblDetectText.setText("No se pudo conectar: " + result.getErrorMessage());
                lblDetectText.setForeground(new Color(248, 113, 113));
                pnlDetectResult.setBackground(new Color(45, 20, 24));
                pnlDetectResult.setBorder(new CompoundBorder(
                    new LineBorder(new Color(239, 68, 68), 1, true),
                    new EmptyBorder(2, 8, 2, 8)
                ));
            }
        }));
    }

    private void applyValuesToConfig() {
        config.setHost(txtHost.getText().trim());
        config.setPort((Integer) spnPort.getValue());

        String versionSel = cmbVersion.getSelectedItem() != null ? cmbVersion.getSelectedItem().toString() : "";
        if (versionSel.contains("Auto-Detectar")) {
            ServerPingResult ping = ServerPingService.pingServer(config.getHost(), config.getPort());
            if (ping.isSuccess()) {
                config.setVersion(ping.getMatchedVersion());
                config.setVersionInput(ping.getVersionName());
            } else {
                config.setVersion(MinecraftVersion.V1_20_5);
                config.setVersionInput("1.20.5");
            }
        } else {
            config.setVersion(MinecraftVersion.fromString(versionSel));
            config.setVersionInput(versionSel);
        }

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
