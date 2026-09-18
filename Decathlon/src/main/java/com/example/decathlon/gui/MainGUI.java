package com.example.decathlon.gui;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.decathlon.core.ScoringService;

public class MainGUI {

    private final ScoringService scoringService = new ScoringService();

    private static final Map<String, String> EVENT_IDS = buildEventIds();

    private static Map<String, String> buildEventIds() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("100m", "100m");
        m.put("400m", "400m");
        m.put("1500m", "1500m");
        m.put("110m Hurdles", "110mHurdles");
        m.put("Long Jump", "longJump");
        m.put("High Jump", "highJump");
        m.put("Pole Vault", "poleVault");
        m.put("Discus Throw", "discusThrow");
        m.put("Javelin Throw", "javelinThrow");
        m.put("Shot Put", "shotPut");
        return m;
    }

    private JTextField nameField;
    private JTextField resultField;
    private JComboBox<String> disciplineBox;
    private JTextArea outputArea;

    public static void main(String[] args) {
        new MainGUI().createAndShowGUI();
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Track and Field Calculator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JPanel panel = new JPanel(new GridLayout(6, 1));

        nameField = new JTextField(20);
        panel.add(new JLabel("Enter Competitor's Name:"));
        panel.add(nameField);

        String[] disciplines = EVENT_IDS.keySet().toArray(new String[0]);
        disciplineBox = new JComboBox<>(disciplines);
        panel.add(new JLabel("Select Discipline:"));
        panel.add(disciplineBox);

        resultField = new JTextField(10);
        panel.add(new JLabel("Enter Result:"));
        panel.add(resultField);

        JButton calculateButton = new JButton("Calculate Score");
        calculateButton.addActionListener(new CalculateButtonListener());
        panel.add(calculateButton);

        outputArea = new JTextArea(5, 40);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        panel.add(scrollPane);

        frame.add(panel);
        frame.setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String name = nameField.getText();
            String discipline = (String) disciplineBox.getSelectedItem();
            String resultText = resultField.getText();

            try {
                double result = Double.parseDouble(resultText);
                String eventId = EVENT_IDS.get(discipline);
                int score = scoringService.score(eventId, result);

                outputArea.append("Competitor: " + name + "\n");
                outputArea.append("Discipline: " + discipline + "\n");
                outputArea.append("Result: " + result + "\n");
                outputArea.append("Score: " + score + "\n\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Please enter a valid number for the result.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
