package com.trackit.ui;

import com.trackit.db.HabitDAO;
import com.trackit.db.TaskDAO;
import com.trackit.model.Habit;
import com.trackit.model.Task;
import com.toedter.calendar.JDateChooser;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Vector;

public class HabitDetailsDialog extends JDialog {
    private final HabitDAO habitDAO;
    private final TaskDAO taskDAO;
    private final Habit habit;
    private JTextField nameField;
    private JSpinner goalDaysSpinner;
    private JSpinner frequencySpinner;
    private JTextArea notesArea;
    private JDateChooser startDateChooser;
    private JTable tasksTable;
    private DefaultTableModel tableModel;
    private JProgressBar progressBar;
    private JButton markCompletedButton;
    private boolean confirmed = false;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public HabitDetailsDialog(Frame owner, Habit habit) {
        super(owner, "Habit Details", true);
        this.habitDAO = new HabitDAO();
        this.taskDAO = new TaskDAO();
        this.habit = habit;
        initComponents();
        loadData();
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(new Dimension(600, 500));
    }

    private void initComponents() {
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPanel);

        // Details Panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(new TitledBorder("Habit Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Name field
        detailsPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        nameField = new JTextField(20);
        detailsPanel.add(nameField, gbc);

        // Goal days spinner
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Goal Days:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        goalDaysSpinner = new JSpinner(new SpinnerNumberModel(30, 1, 365, 1));
        detailsPanel.add(goalDaysSpinner, gbc);

        // Frequency spinner
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Frequency (days):"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        frequencySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 30, 1));
        detailsPanel.add(frequencySpinner, gbc);

        // Start date chooser
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Start Date:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        startDateChooser = new JDateChooser();
        detailsPanel.add(startDateChooser, gbc);

        // Notes area
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 0.5;
        notesArea = new JTextArea(4, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        detailsPanel.add(notesScroll, gbc);

        contentPanel.add(detailsPanel, BorderLayout.NORTH);

        // Tasks Panel
        JPanel tasksPanel = new JPanel(new BorderLayout(5, 5));
        tasksPanel.setBorder(new TitledBorder("Tasks"));

        // Create table model with custom date rendering
        String[] columnNames = {"#", "Status", "Start Date", "Due Date", "Completion Date", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only status column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                return String.class;
            }
        };
        
        tasksTable = new JTable(tableModel);
        tasksTable.setFillsViewportHeight(true);
        
        // Set column widths
        tasksTable.getColumnModel().getColumn(0).setPreferredWidth(30);  // #
        tasksTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Status
        tasksTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Start Date
        tasksTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Due Date
        tasksTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Completion Date
        tasksTable.getColumnModel().getColumn(5).setPreferredWidth(200); // Notes
        
        // Add combo box for status column
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
            Task.Status.PENDING.name(),
            Task.Status.IN_PROGRESS.name(),
            Task.Status.COMPLETED.name(),
            Task.Status.FAILED.name()
        });
        tasksTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(statusCombo));

        JScrollPane tableScroll = new JScrollPane(tasksTable);
        tasksPanel.add(tableScroll, BorderLayout.CENTER);

        // Progress Panel
        JPanel progressPanel = new JPanel(new BorderLayout(5, 5));
        progressPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        progressPanel.add(new JLabel("Progress:"), BorderLayout.WEST);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        tasksPanel.add(progressPanel, BorderLayout.SOUTH);

        contentPanel.add(tasksPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        // Add Mark Completed button
        markCompletedButton = new JButton("Mark Habit Completed");
        markCompletedButton.addActionListener(e -> markHabitCompleted());
        buttonPanel.add(markCompletedButton);
        
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

    private void loadData() {
        nameField.setText(habit.getName());
        goalDaysSpinner.setValue(habit.getGoalDays());
        frequencySpinner.setValue(habit.getFrequency());
        notesArea.setText(habit.getNotes());
        startDateChooser.setDate(Date.from(
            habit.getStartDate().atZone(ZoneId.systemDefault()).toInstant()
        ));

        // Load tasks with formatted dates
        tableModel.setRowCount(0);
        for (Task task : habit.getTasks()) {
            Vector<Object> row = new Vector<>();
            row.add(task.getTaskNumber());
            row.add(task.getStatus());
            row.add(formatDate(task.getStartDate()));
            row.add(formatDate(task.getDueDate()));
            row.add(task.getCompletionDate() != null ? formatDate(task.getCompletionDate()) : "");
            row.add(task.getNotes());
            tableModel.addRow(row);
        }

        updateProgress();
        updateMarkCompletedButtonState();
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(dateFormatter) : "";
    }

    private void updateProgress() {
        double progress = habit.getProgress();
        progressBar.setValue((int) progress);
        progressBar.setString(String.format("%.1f%%", progress));
        updateMarkCompletedButtonState();
    }

    private void updateMarkCompletedButtonState() {
        boolean isCompleted = Habit.Status.COMPLETED.name().equals(habit.getStatus());
        markCompletedButton.setEnabled(!isCompleted && habit.getProgress() > 0);
        markCompletedButton.setText(isCompleted ? "Habit Completed" : "Mark Habit Completed");
    }

    private void markHabitCompleted() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to mark this habit as completed?",
            "Confirm Completion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            habit.setStatus(Habit.Status.COMPLETED.name());
            habit.setCompletionDate(LocalDateTime.now());
            
            try {
                // Mark all remaining tasks as completed
                for (Task task : habit.getTasks()) {
                    if (!Task.Status.COMPLETED.name().equals(task.getStatus())) {
                        task.setStatus(Task.Status.COMPLETED.name());
                        task.setCompletionDate(LocalDateTime.now());
                        // Save each task update to the database
                        taskDAO.updateTask(task);
                    }
                }
                
                habitDAO.updateHabit(habit);
                updateMarkCompletedButtonState();
                loadData(); // Refresh the display
                JOptionPane.showMessageDialog(
                    this,
                    "Habit marked as completed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(
                    this,
                    "Error marking habit as completed: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
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
            // Check if frequency or goal days have changed
            boolean frequencyChanged = habit.getFrequency() != (Integer) frequencySpinner.getValue();
            boolean goalDaysChanged = habit.getGoalDays() != (Integer) goalDaysSpinner.getValue();
            
            // If either changed, ask user for confirmation
            if (frequencyChanged || goalDaysChanged) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Changing frequency or goal days will reset all tasks and progress. Continue?",
                    "Confirm Changes",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            // Update habit details
            habit.setName(nameField.getText().trim());
            habit.setGoalDays((Integer) goalDaysSpinner.getValue());
            habit.setFrequency((Integer) frequencySpinner.getValue());
            habit.setNotes(notesArea.getText().trim());
            habit.setStartDate(LocalDateTime.ofInstant(
                startDateChooser.getDate().toInstant(),
                ZoneId.systemDefault()
            ));

            // If frequency or goal days changed, delete old tasks and generate new ones
            if (frequencyChanged || goalDaysChanged) {
                // First delete all existing tasks
                taskDAO.deleteTasksForHabit(habit.getId());
                
                // Save the basic habit details first
                habitDAO.updateHabit(habit);
                
                // Generate new tasks
                habitDAO.generateTasks(habit);
                
                // Reload the habit to get the new tasks
                habit.setTasks(taskDAO.getTasksForHabit(habit.getId()));
                
                // Refresh the table
                loadData();
            } else {
                // Update task statuses
                for (int i = 0; i < tableModel.getRowCount(); i++) {
                    Task task = habit.getTasks().get(i);
                    String newStatus = (String) tableModel.getValueAt(i, 1);
                    if (!task.getStatus().equals(newStatus)) {
                        task.setStatus(newStatus);
                        if (newStatus.equals(Task.Status.COMPLETED.name())) {
                            task.setCompletionDate(LocalDateTime.now());
                        } else {
                            task.setCompletionDate(null);
                        }
                        // Save each task update to the database
                        taskDAO.updateTask(task);
                    }
                }

                // Save habit to database
                habitDAO.updateHabit(habit);
            }
            
            confirmed = true;
            updateProgress();
            JOptionPane.showMessageDialog(this,
                "Habit updated successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error saving habit: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isConfirmed() {
        return confirmed;
    }
} 