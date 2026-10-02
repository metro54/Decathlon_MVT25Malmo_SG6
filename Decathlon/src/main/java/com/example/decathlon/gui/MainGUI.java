package com.example.decathlon.gui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.example.decathlon.core.CompetitionService;
import com.example.decathlon.core.ScoringService;
import com.example.decathlon.core.ScoringService.Discipline;
import com.example.decathlon.core.ScoringService.EventDef;

public class MainGUI {

    private final ScoringService scoringService = new ScoringService();
    private final CompetitionService competitionService = new CompetitionService(scoringService);

    private JComboBox<String> disciplineBox;
    private JTextField addNameField;
    private JLabel addErrorLabel;
    private JTextField resultNameField;
    private JComboBox<EventItem> eventBox;
    private JTextField resultField;
    private JLabel resultMsgLabel;
    private JTable standingsTable;
    private DefaultTableModel standingsModel;
    private List<EventDef> currentEvents;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI().createAndShowGUI());
    }

    private Discipline currentDiscipline() {
        return "Heptathlon".equals(disciplineBox.getSelectedItem())
                ? Discipline.HEPTATHLON
                : Discipline.DECATHLON;
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Decathlon / Heptathlon Scoring");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        root.add(buildCompetitionPanel());
        root.add(Box.createVerticalStrut(8));
        root.add(buildAddCompetitorPanel());
        root.add(Box.createVerticalStrut(8));
        root.add(buildEnterResultPanel());
        root.add(Box.createVerticalStrut(8));
        root.add(buildStandingsPanel());

        frame.add(root);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        refreshEvents();
        refreshStandings();
    }

    private JPanel titled(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), title,
                TitledBorder.LEFT, TitledBorder.TOP));
        return p;
    }

    private JPanel buildCompetitionPanel() {
        JPanel panel = titled("Competition");
        panel.setLayout(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel("Discipline:"));
        disciplineBox = new JComboBox<>(new String[]{"Decathlon", "Heptathlon"});
        disciplineBox.addActionListener(e -> {
            refreshEvents();
            refreshStandings();
        });
        panel.add(disciplineBox);
        return panel;
    }

    private JPanel buildAddCompetitorPanel() {
        JPanel panel = titled("Add competitor");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Name:"));
        addNameField = new JTextField(16);
        row.add(addNameField);
        JButton addButton = new JButton("Add competitor");
        addButton.addActionListener(e -> onAddCompetitor());
        row.add(addButton);
        panel.add(row);

        addErrorLabel = new JLabel(" ");
        addErrorLabel.setForeground(new Color(0xB0, 0x00, 0x20));
        addErrorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(addErrorLabel);

        return panel;
    }

    private JPanel buildEnterResultPanel() {
        JPanel panel = titled("Enter result");
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT));
        row.add(new JLabel("Name:"));
        resultNameField = new JTextField(12);
        row.add(resultNameField);
        row.add(new JLabel("Event:"));
        eventBox = new JComboBox<>();
        row.add(eventBox);
        row.add(new JLabel("Result:"));
        resultField = new JTextField(6);
        row.add(resultField);
        JButton saveButton = new JButton("Save score");
        saveButton.addActionListener(e -> onSaveScore());
        row.add(saveButton);
        panel.add(row);

        resultMsgLabel = new JLabel(" ");
        resultMsgLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(resultMsgLabel);

        return panel;
    }

    private JPanel buildStandingsPanel() {
        JPanel panel = titled("Standings");
        panel.setLayout(new BorderLayout(0, 6));

        standingsModel = new DefaultTableModel(new Object[]{"#", "Name", "Total"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        standingsTable = new JTable(standingsModel);
        JScrollPane scrollPane = new JScrollPane(standingsTable,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(680, 220));
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton exportButton = new JButton("Export CSV");
        exportButton.addActionListener(e -> onExportCsv());
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomRow.add(exportButton);
        panel.add(bottomRow, BorderLayout.SOUTH);

        return panel;
    }

    private void onAddCompetitor() {
        String name = addNameField.getText();
        try {
            competitionService.addCompetitor(name);
            addErrorLabel.setText(" ");
            addNameField.setText("");
        } catch (CompetitionService.InvalidNameException ex) {
            addErrorLabel.setText(ex.getMessage());
        }
        refreshStandings();
    }

    private void onSaveScore() {
        String name = resultNameField.getText();
        EventItem event = (EventItem) eventBox.getSelectedItem();
        if (event == null) {
            resultMsgLabel.setForeground(new Color(0xB0, 0x00, 0x20));
            resultMsgLabel.setText("No event selected");
            return;
        }
        double raw;
        try {
            raw = Double.parseDouble(resultField.getText());
        } catch (NumberFormatException ex) {
            resultMsgLabel.setForeground(new Color(0xB0, 0x00, 0x20));
            resultMsgLabel.setText("Please enter a valid number for the result");
            return;
        }
        try {
            int pts = competitionService.score(name, event.def.id(), raw);
            resultMsgLabel.setForeground(new Color(0x00, 0x70, 0x00));
            resultMsgLabel.setText("Saved: " + pts + " pts");
        } catch (CompetitionService.CompetitorNotFoundException ex) {
            resultMsgLabel.setForeground(new Color(0xB0, 0x00, 0x20));
            resultMsgLabel.setText(ex.getMessage());
        }
        refreshStandings();
    }

    private void onExportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("results.csv"));
        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(competitionService.exportCsv());
                JOptionPane.showMessageDialog(null, "Exported to " + file.getAbsolutePath());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "Export failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshEvents() {
        currentEvents = scoringService.eventDefs(currentDiscipline());
        eventBox.removeAllItems();
        for (EventDef def : currentEvents) {
            eventBox.addItem(new EventItem(def));
        }
        rebuildStandingsColumns();
    }

    private void rebuildStandingsColumns() {
        Object[] columns = new Object[currentEvents.size() + 3];
        columns[0] = "#";
        columns[1] = "Name";
        for (int i = 0; i < currentEvents.size(); i++) {
            columns[2 + i] = currentEvents.get(i).label();
        }
        columns[columns.length - 1] = "Total";
        standingsModel.setColumnIdentifiers(columns);
    }

    private void refreshStandings() {
        standingsModel.setRowCount(0);
        List<Map<String, Object>> standings = competitionService.standings();
        int position = 1;
        for (Map<String, Object> row : standings) {
            @SuppressWarnings("unchecked")
            Map<String, Integer> scores = (Map<String, Integer>) row.get("scores");
            Object[] rowData = new Object[currentEvents.size() + 3];
            rowData[0] = position++;
            rowData[1] = row.get("name");
            for (int i = 0; i < currentEvents.size(); i++) {
                Integer pts = scores.get(currentEvents.get(i).id());
                rowData[2 + i] = pts == null ? "" : pts;
            }
            rowData[rowData.length - 1] = row.get("total");
            standingsModel.addRow(rowData);
        }
    }

    private static class EventItem {
        final EventDef def;
        EventItem(EventDef def) { this.def = def; }
        @Override
        public String toString() { return def.label() + " (" + def.unit() + ")"; }
    }
}
