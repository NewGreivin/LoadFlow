package com.loadflow.ui;

import com.loadflow.model.TestConfig;

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
    private JSpinner spnProtocol;
    private JSpinner spnTargetBots;
    private JSpinner spnRampRate;
    private JSpinner spnDuration;
    private JTextField txtPrefix;
    private JComboBox<String> cmbProfile;
    private JCheckBox chkCircuitBreaker;
    private JSpinner spnThreshold;

    public ConfigPanel(TestConfig config, Consumer<TestConfig> onStartTest) {
        this.config = config;
        this.onStartTest = onStartTest;

        setLayout(new BorderLayout(15, 15));
        setBorder(new EmptyBorder(20, 30, 20, 30));

        initComponents();
    }

    private void initComponents() {
        JPanel formContainer = new JPanel();
        formContainer.setLayout(new BoxLayout(formContainer, BoxLayout.Y_AXIS));

        // 1. Destino de Red
        JPanel pnlTarget = new JPanel(new GridLayout(2, 2, 15, 10));
        pnlTarget.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "1. Servidor Minecraft de Destino (Paper / Spigot / Proxy)", TitledBorder.LEFT, TitledBorder.TOP));

        pnlTarget.add(new JLabel("Host / Dirección IP:"));
        txtHost = new JTextField(config.getHost());
        pnlTarget.add(txtHost);

        pnlTarget.add(new JLabel("Puerto:"));
        spnPort = new JSpinner(new SpinnerNumberModel(config.getPort(), 1, 65535, 1));
        pnlTarget.add(spnPort);

        formContainer.add(pnlTarget);
        formContainer.add(Box.createVerticalStrut(15));

        // 2. Parámetros de Carga
        JPanel pnlLoad = new JPanel(new GridLayout(4, 2, 15, 10));
        pnlLoad.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "2. Parámetros de Carga y Simulación", TitledBorder.LEFT, TitledBorder.TOP));

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
        formContainer.add(Box.createVerticalStrut(15));

        // 3. Comportamiento y Seguridad
        JPanel pnlSafety = new JPanel(new GridLayout(3, 2, 15, 10));
        pnlSafety.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "3. Perfil de Comportamiento y Circuit Breaker", TitledBorder.LEFT, TitledBorder.TOP));

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

        // Botón de Inicio
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLaunch = new JButton("▶ Guardar y Lanzar Prueba de Carga");
        btnLaunch.setFont(btnLaunch.getFont().deriveFont(Font.BOLD, 14f));
        btnLaunch.setBackground(new Color(40, 167, 69));
        btnLaunch.setForeground(Color.WHITE);
        btnLaunch.setPreferredSize(new Dimension(300, 42));

        btnLaunch.addActionListener(e -> {
            applyValuesToConfig();
            onStartTest.accept(config);
        });

        bottomBar.add(btnLaunch);
        add(bottomBar, BorderLayout.SOUTH);
    }

    private void applyValuesToConfig() {
        config.setHost(txtHost.getText().trim());
        config.setPort((Integer) spnPort.getValue());
        config.setTargetBots((Integer) spnTargetBots.getValue());
        config.setRampRatePerSec((Integer) spnRampRate.getValue());
        config.setDurationSeconds((Integer) spnDuration.getValue());
        config.setBotPrefix(txtPrefix.getText().trim());
        config.setBehaviorProfile(cmbProfile.getSelectedIndex() == 1 ? "EXPLORER" : "AFK");
        config.setAutoStopOnError(chkCircuitBreaker.isSelected());
        config.setMaxErrorThresholdPct((Double) spnThreshold.getValue());
    }
}
