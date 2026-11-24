package com.loadflow.ui;

import com.loadflow.model.BotSession;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class BotMonitorPanel extends JPanel {
    private final BotTableModel tableModel = new BotTableModel();
    private final JTable table = new JTable(tableModel);
    private final JTextField txtSearch = new JTextField(16);
    private final JComboBox<String> cmbFilter = new JComboBox<>(new String[]{"Todos", "Conectados", "Conectando", "Fallidos / Expulsados"});

    public BotMonitorPanel() {
        setLayout(new BorderLayout(12, 12));
        setBackground(new Color(20, 22, 27));
        setBorder(new EmptyBorder(18, 30, 18, 30));

        initComponents();
    }

    private void initComponents() {
        // Toolbar de filtros con diseño moderno
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(new Color(27, 30, 38));
        toolbar.setBorder(new CompoundBorder(
            new LineBorder(new Color(42, 47, 60), 1, true),
            new EmptyBorder(10, 16, 10, 16)
        ));

        JPanel leftFilters = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        leftFilters.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Buscar Bot:");
        lblSearch.setFont(lblSearch.getFont().deriveFont(Font.BOLD, 12f));
        lblSearch.setForeground(new Color(209, 213, 219));

        txtSearch.setPreferredSize(new Dimension(180, 34));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nombre o # del bot");

        JLabel lblFilter = new JLabel("Estado:");
        lblFilter.setFont(lblFilter.getFont().deriveFont(Font.BOLD, 12f));
        lblFilter.setForeground(new Color(209, 213, 219));

        cmbFilter.setPreferredSize(new Dimension(170, 34));

        leftFilters.add(lblSearch);
        leftFilters.add(txtSearch);
        leftFilters.add(lblFilter);
        leftFilters.add(cmbFilter);

        JButton btnRefresh = new JButton("↻ Actualizar");
        btnRefresh.setPreferredSize(new Dimension(110, 34));
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.putClientProperty("JButton.buttonType", "roundRect");
        btnRefresh.addActionListener(e -> applyFilter());

        toolbar.add(leftFilters, BorderLayout.WEST);
        toolbar.add(btnRefresh, BorderLayout.EAST);

        add(toolbar, BorderLayout.NORTH);

        // Tabla con diseño limpio
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 36));
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 12f));

        // Renderers centrados y coloreados
        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(2).setCellRenderer(new StateCellRenderer());
        table.getColumnModel().getColumn(3).setPreferredWidth(85);
        table.getColumnModel().getColumn(4).setPreferredWidth(85);
        table.getColumnModel().getColumn(5).setPreferredWidth(110);
        table.getColumnModel().getColumn(6).setPreferredWidth(250);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(new LineBorder(new Color(42, 47, 60), 1, true));
        scrollPane.getViewport().setBackground(new Color(24, 27, 34));
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
                setForeground(new Color(52, 211, 153));
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (val.contains("Conectando") || val.contains("Autenticando") || val.contains("Configuración")) {
                setForeground(new Color(251, 191, 36));
                setFont(getFont().deriveFont(Font.PLAIN));
            } else if (val.contains("Expulsado") || val.contains("Error")) {
                setForeground(new Color(248, 113, 113));
                setFont(getFont().deriveFont(Font.BOLD));
            } else {
                setForeground(new Color(156, 163, 175));
                setFont(getFont().deriveFont(Font.PLAIN));
            }
            return c;
        }
    }
}
