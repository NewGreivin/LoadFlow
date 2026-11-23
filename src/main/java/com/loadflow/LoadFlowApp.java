package com.loadflow;

import com.formdev.flatlaf.FlatDarkLaf;
import com.loadflow.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;

public class LoadFlowApp {
    private static final Logger log = LoggerFactory.getLogger(LoadFlowApp.class);

    public static void main(String[] args) {
        // Configurar tema FlatLaf Dark moderno
        try {
            System.setProperty("flatlaf.useWindowDecorations", "true");
            System.setProperty("flatlaf.menuBarEmbedded", "true");
            UIManager.put("Component.arrowType", "chevron");
            UIManager.put("Component.focusWidth", 1);
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("ProgressBar.arc", 8);

            FlatDarkLaf.setup();
            log.info("Tema FlatLaf Dark inicializado correctamente.");
        } catch (Exception e) {
            log.warn("No se pudo inicializar FlatDarkLaf, usando tema por defecto del sistema.", e);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
        }

        // Lanzar interfaz en el Event Dispatch Thread de Swing
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                log.info("Ventana principal de LoadFlow iniciada.");
            } catch (Exception e) {
                log.error("Error fatal al inicializar la interfaz de usuario: {}", e.getMessage(), e);
                JOptionPane.showMessageDialog(null,
                    "Error al inicializar LoadFlow: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
