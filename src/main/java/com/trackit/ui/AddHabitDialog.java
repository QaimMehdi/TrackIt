package com.trackit.ui;

import com.trackit.db.HabitDAO;
import com.trackit.model.Habit;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class AddHabitDialog extends JDialog {
    private final HabitDAO habitDAO;
    private JTextField nameField;
    private JSpinner goalDaysSpinner;
    private JSpinner frequencySpinner;
    private JTextArea notesArea;
    private JDateChooser startDateChooser;
    private boolean confirmed = false;

    public AddHabitDialog(Frame owner) {
        super(owner, "Add New Habit", true);
        this.habitDAO = new HabitDAO();
        initComponents();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPanel);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Goal days spinner
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Goal Days:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        goalDaysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        formPanel.add(goalDaysSpinner, gbc);

        // Frequency spinner
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Frequency (days):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        frequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        formPanel.add(frequencySpinner, gbc);

        // Start date chooser
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        startDateChooser = new JDateChooser(new Date());
        formPanel.add(startDateChooser, gbc);

        // Notes area
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        formPanel.add(notesScroll, gbc);

        contentPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.addActionListener(e -> saveHabit());
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    private void saveHabit() {
        // Validate input
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter a habit name.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return;
        }

        if (startDateChooser.getDate() == null) {
            JOptionPane.showMessageDialog(this,
                "Please select a start date.",
                "Validation Error",
                JOptionPane.ERROR_MESSAGE);
            startDateChooser.requestFocus();
            return;
        }

        try {
            Habit habit = new Habit();
            habit.setName(nameField.getText().trim());
            habit.setGoalDays((Integer) goalDaysSpinner.getValue());
            habit.setFrequency((Integer) frequencySpinner.getValue());
            habit.setNotes(notesArea.getText().trim());
            habit.setStartDate(LocalDateTime.ofInstant(
                startDateChooser.getDate().toInstant(),
                ZoneId.systemDefault()
            ));
            habit.setStatus(Habit.Status.ACTIVE.name());

            habitDAO.createHabit(habit);
            confirmed = true;
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error saving habit: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 