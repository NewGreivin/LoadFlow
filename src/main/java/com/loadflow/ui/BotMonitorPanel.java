package com.loadflow.ui;

import com.loadflow.model.BotSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BotMonitorPanel extends JPanel {
    private final BotTableModel tableModel = new BotTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField txtSearch = new JTextField(15);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{"Todos", "Conectados", "Conectando", "Fallidos / Expulsados"});

    public BotMonitorPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        initComponents();
    }

    private void initComponents() {
        // Toolbar de filtros
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel("Buscar Bot:"));
        toolbar.add(txtSearch);
        toolbar.add(new JLabel("Filtrar Estado:"));
        toolbar.add(cmbFilter);

        JButton btnRefresh = new JButton("Actualizar Vista");
        btnRefresh.addActionListener(e -> tableModel.fireTableDataChanged());
        toolbar.add(btnRefresh);

        add(toolbar, BorderLayout.NORTH);

        // Tabla
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(2).setCellRenderer(new StateCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        txtSearch.addCaretListener(e -> applyFilter());
        cmbFilter.addActionListener(e -> applyFilter());
    }

    public void updateSessions(List<BotSession> sessions) {
        SwingUtilities.invokeLater(() -> {
            tableModel.setAllSessions(sessions);
            applyFilter();
        });
    }

    private void applyFilter() {
        String query = txtSearch.getText().trim().toLowerCase();
        String filter = (String) cmbFilter.getSelectedItem();

        tableModel.filter(query, filter);
    }

    private static class BotTableModel extends AbstractTableModel {
        private final String[] columns = {"#", "Alias / Nombre", "Estado", "Latencia", "Duración", "Tx / Rx", "Detalle / Desconexión"};
        private List<BotSession> allSessions = new ArrayList<>();
        private List<BotSession> displayedSessions = new ArrayList<>();

        public void setAllSessions(List<BotSession> sessions) {
            this.allSessions = new ArrayList<>(sessions);
        }

        public void filter(String query, String filter) {
            int previousSize = displayedSessions.size();
            displayedSessions = allSessions.stream().filter(s -> {
                boolean matchQuery = query.isEmpty() || s.getUsername().toLowerCase().contains(query);
                boolean matchFilter = true;
                if ("Conectados".equals(filter)) {
                    matchFilter = s.getState() == com.loadflow.model.BotState.CONNECTED;
                } else if ("Conectando".equals(filter)) {
                    matchFilter = s.getState() == com.loadflow.model.BotState.CONNECTING ||
                                  s.getState() == com.loadflow.model.BotState.LOGGING_IN ||
                                  s.getState() == com.loadflow.model.BotState.CONFIGURATION;
                } else if ("Fallidos / Expulsados".equals(filter)) {
                    matchFilter = s.getState() == com.loadflow.model.BotState.KICKED ||
                                  s.getState() == com.loadflow.model.BotState.ERROR;
                }
                return matchQuery && matchFilter;
            }).toList();

            if (displayedSessions.size() == previousSize && previousSize > 0) {
                fireTableRowsUpdated(0, previousSize - 1);
            } else {
                fireTableDataChanged();
            }
        }

        @Override
        public int getRowCount() { return displayedSessions.size(); }
        @Override
        public int getColumnCount() { return columns.length; }
        @Override
        public String getColumnName(int col) { return columns[col]; }

        @Override
        public Object getValueAt(int row, int col) {
            if (row >= displayedSessions.size()) return "";
            BotSession s = displayedSessions.get(row);
            return switch (col) {
                case 0 -> s.getId();
                case 1 -> s.getUsername();
                case 2 -> s.getState().getDisplayName();
                case 3 -> s.getLatencyMs() >= 0 ? s.getLatencyMs() + " ms" : "-";
                case 4 -> {
                    long sec = s.getOnlineDurationMs() / 1000;
                    yield String.format("%02d:%02d", sec / 60, sec % 60);
                }
                case 5 -> s.getPacketsSent() + " / " + s.getPacketsReceived();
                case 6 -> s.getDisconnectReason().isEmpty() ? "-" : s.getDisconnectReason();
                default -> "";
            };
        }
    }

    private static class StateCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String val = value != null ? value.toString() : "";
            if (val.contains("Conectado")) {
                setForeground(new Color(46, 204, 113));
            } else if (val.contains("Conectando") || val.contains("Autenticando") || val.contains("Configuración")) {
                setForeground(new Color(241, 196, 15));
            } else if (val.contains("Expulsado") || val.contains("Error")) {
                setForeground(new Color(231, 76, 60));
            } else {
                setForeground(Color.GRAY);
            }
            return c;
        }
    }
}
