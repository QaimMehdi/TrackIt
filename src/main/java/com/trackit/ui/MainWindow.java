package com.trackit.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import com.trackit.model.Habit;
import com.trackit.db.HabitDAO;
import java.sql.SQLException;

public class MainWindow extends JFrame {
    private JPanel contentPane;
    private JList<Habit> habitList;
    private DefaultListModel<Habit> listModel;
    private JButton addHabitButton;
    private JButton viewDetailsButton;
    private JButton deleteHabitButton;
    private JProgressBar progressBar;
    private JProgressBar overallProgressBar;
    private JLabel totalHabitsLabel;
    private JLabel activeHabitsLabel;
    private JLabel completedHabitsLabel;
    private HabitDAO habitDAO;

    public MainWindow() {
        setTitle("TrackIt - Habit Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 800, 600);
        setMinimumSize(new Dimension(600, 400));

        habitDAO = new HabitDAO();

        // Initialize components
        initComponents();
        
        // Load habits from database
        loadHabits();
        
        // Center the window on screen
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        contentPane.setLayout(new BorderLayout(10, 10));
        setContentPane(contentPane);

        // Create toolbar
        JToolBar toolBar = createToolBar();
        contentPane.add(toolBar, BorderLayout.NORTH);

        // Create main content panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(500);
        
        // Habits list panel
        JPanel habitsPanel = new JPanel(new BorderLayout(5, 5));
        habitsPanel.setBorder(new TitledBorder("Your Habits"));
        
        listModel = new DefaultListModel<>();
        habitList = new JList<>(listModel);
        habitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitList.setCellRenderer(new HabitListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(habitList);
        habitsPanel.add(scrollPane, BorderLayout.CENTER);

        // Selected habit progress
        JPanel selectedProgressPanel = new JPanel(new BorderLayout(5, 5));
        selectedProgressPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        selectedProgressPanel.add(new JLabel("Selected Habit Progress:"), BorderLayout.WEST);
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        selectedProgressPanel.add(progressBar, BorderLayout.CENTER);
        habitsPanel.add(selectedProgressPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(habitsPanel);
        
        // Stats Panel
        JPanel statsPanel = new JPanel(new GridBagLayout());
        statsPanel.setBorder(new TitledBorder("Overall Statistics"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Overall progress panel
        JPanel overallProgressPanel = new JPanel(new BorderLayout(5, 5));
        overallProgressPanel.setBorder(new TitledBorder("Overall Progress"));
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setStringPainted(true);
        overallProgressPanel.add(overallProgressBar, BorderLayout.CENTER);
        statsPanel.add(overallProgressPanel, gbc);
        
        // Habit counts
        gbc.gridy++;
        gbc.gridwidth = 1;
        totalHabitsLabel = new JLabel("Total Habits: 0");
        statsPanel.add(totalHabitsLabel, gbc);
        
        gbc.gridy++;
        activeHabitsLabel = new JLabel("Active Habits: 0");
        statsPanel.add(activeHabitsLabel, gbc);
        
        gbc.gridy++;
        completedHabitsLabel = new JLabel("Completed Habits: 0");
        statsPanel.add(completedHabitsLabel, gbc);
        
        splitPane.setRightComponent(statsPanel);
        
        contentPane.add(splitPane, BorderLayout.CENTER);

        // Add listeners
        addListeners();
    }

    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        addHabitButton = new JButton("Add Habit");
        viewDetailsButton = new JButton("View Details");
        deleteHabitButton = new JButton("Delete");

        // Initially disable buttons that require selection
        viewDetailsButton.setEnabled(false);
        deleteHabitButton.setEnabled(false);

        toolBar.add(addHabitButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(viewDetailsButton);
        toolBar.add(Box.createHorizontalStrut(5));
        toolBar.add(deleteHabitButton);

        return toolBar;
    }

    private void addListeners() {
        addHabitButton.addActionListener(e -> showAddHabitDialog());
        viewDetailsButton.addActionListener(e -> showHabitDetails());
        deleteHabitButton.addActionListener(e -> deleteSelectedHabit());

        habitList.addListSelectionListener(e -> {
            boolean hasSelection = !habitList.isSelectionEmpty();
            viewDetailsButton.setEnabled(hasSelection);
            deleteHabitButton.setEnabled(hasSelection);
            updateProgressBar();
        });
    }

    private void loadHabits() {
        try {
            listModel.clear();
            habitDAO.getAllHabits().forEach(listModel::addElement);
            updateProgressBar();
            updateStatistics();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading habits: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddHabitDialog() {
        AddHabitDialog dialog = new AddHabitDialog(this);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            loadHabits();
        }
    }

    private void showHabitDetails() {
        Habit selectedHabit = habitList.getSelectedValue();
        if (selectedHabit != null) {
            try {
                // Reload the habit to get the latest data
                Habit habit = habitDAO.getHabitById(selectedHabit.getId());
                if (habit != null) {
                    HabitDetailsDialog dialog = new HabitDetailsDialog(this, habit);
                    dialog.setVisible(true);
                    
                    if (dialog.isConfirmed()) {
                        loadHabits();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                    "Error loading habit details: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedHabit() {
        Habit selectedHabit = habitList.getSelectedValue();
        if (selectedHabit != null) {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete this habit?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    habitDAO.deleteHabit(selectedHabit.getId());
                    loadHabits();
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting habit: " + e.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void updateStatistics() {
        int totalHabits = listModel.size();
        int activeHabits = 0;
        int completedHabits = 0;
        double totalProgress = 0;
        
        for (int i = 0; i < listModel.size(); i++) {
            Habit habit = listModel.getElementAt(i);
            if (habit.isActive()) {
                activeHabits++;
            } else if (habit.isCompleted()) {
                completedHabits++;
            }
            totalProgress += habit.getProgress();
        }
        
        // Update labels
        totalHabitsLabel.setText(String.format("Total Habits: %d", totalHabits));
        activeHabitsLabel.setText(String.format("Active Habits: %d", activeHabits));
        completedHabitsLabel.setText(String.format("Completed Habits: %d", completedHabits));
        
        // Update overall progress
        double averageProgress = totalHabits > 0 ? totalProgress / totalHabits : 0;
        overallProgressBar.setValue((int) averageProgress);
        overallProgressBar.setString(String.format("%.1f%%", averageProgress));
    }

    private void updateProgressBar() {
        Habit selectedHabit = habitList.getSelectedValue();
        if (selectedHabit != null) {
            double progress = selectedHabit.getProgress();
            progressBar.setValue((int) progress);
            progressBar.setString(String.format("%.1f%%", progress));
        } else {
            progressBar.setValue(0);
            progressBar.setString("");
        }
        updateStatistics();
    }

    // Custom cell renderer for habit list
    private class HabitListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof Habit) {
                Habit habit = (Habit) value;
                setText(String.format("%s (%.1f%%)", 
                    habit.getName(), 
                    habit.getProgress()));
                setToolTipText(String.format("Goal: %d days, Frequency: %d days",
                    habit.getGoalDays(),
                    habit.getFrequency()));
            }
            
            return this;
        }
    }
} 