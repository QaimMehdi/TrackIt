package com.trackit.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import com.trackit.model.Habit;
import com.trackit.db.HabitDAO;
import java.sql.SQLException;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import javax.swing.border.MatteBorder;

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
        super("TrackIt - Habit Tracker");
        this.habitDAO = new HabitDAO();
        initComponents();
        loadHabits();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
    }

    private void initComponents() {
        setLayout(new BorderLayout(15, 15));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Header Panel with gradient
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Main content panel with list and stats
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setBorder(null);
        splitPane.setDividerLocation(650);
        splitPane.setResizeWeight(0.7);
        
        // Habits list panel
        JPanel habitsPanel = new JPanel(new BorderLayout(10, 10));
        habitsPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true)
        ));
        
        // Search panel
        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        JTextField searchField = new JTextField();
        searchField.putClientProperty("JTextField.placeholderText", "Search habits...");
        searchField.putClientProperty("JTextField.leadingIcon", new FlatSVGIcon("icons/search.svg"));
        searchPanel.add(searchField, BorderLayout.CENTER);
        habitsPanel.add(searchPanel, BorderLayout.NORTH);
        
        listModel = new DefaultListModel<>();
        habitList = new JList<>(listModel);
        habitList.setCellRenderer(new ModernHabitListCellRenderer());
        habitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitList.putClientProperty("List.selectionBackground", UIManager.getColor("Component.accentColor"));
        habitList.setFixedCellHeight(60);
        
        JScrollPane scrollPane = new JScrollPane(habitList);
        scrollPane.setBorder(null);
        habitsPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Selected habit progress
        JPanel selectedProgressPanel = new JPanel(new BorderLayout(10, 5));
        selectedProgressPanel.setBorder(new EmptyBorder(10, 5, 5, 5));
        JLabel progressLabel = new JLabel("Selected Habit Progress");
        progressLabel.setFont(progressLabel.getFont().deriveFont(Font.BOLD));
        selectedProgressPanel.add(progressLabel, BorderLayout.NORTH);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 20));
        progressBar.setStringPainted(true);
        progressBar.setFont(progressBar.getFont().deriveFont(Font.BOLD));
        selectedProgressPanel.add(progressBar, BorderLayout.CENTER);
        habitsPanel.add(selectedProgressPanel, BorderLayout.SOUTH);
        
        splitPane.setLeftComponent(habitsPanel);
        
        // Stats Panel
        JPanel statsPanel = createStatsPanel();
        splitPane.setRightComponent(statsPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        addListeners();
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                GradientPaint gradient = new GradientPaint(
                    0, 0, UIManager.getColor("Component.accentColor").brighter(),
                    getWidth(), 0, UIManager.getColor("Component.accentColor").darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2d.dispose();
            }
        };
        headerPanel.setOpaque(false);
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        headerPanel.setPreferredSize(new Dimension(headerPanel.getPreferredSize().width, 80));

        // App title
        JLabel titleLabel = new JLabel("TrackIt");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 24f));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Toolbar
        JPanel toolbar = createToolBar();
        headerPanel.add(toolbar, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createToolBar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        toolbar.setOpaque(false);

        addHabitButton = new JButton("New Habit");
        addHabitButton.setFont(addHabitButton.getFont().deriveFont(Font.BOLD));
        addHabitButton.putClientProperty("FlatLaf.style", "foreground: #000000");
        
        viewDetailsButton = new JButton("View Details");
        viewDetailsButton.setEnabled(false);
        viewDetailsButton.putClientProperty("FlatLaf.style", "foreground: #000000");
        
        deleteHabitButton = new JButton("Delete");
        deleteHabitButton.setEnabled(false);
        deleteHabitButton.putClientProperty("FlatLaf.style", "foreground: #000000");

        toolbar.add(addHabitButton);
        toolbar.add(viewDetailsButton);
        toolbar.add(deleteHabitButton);

        return toolbar;
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = new JPanel(new BorderLayout(10, 10));
        statsPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor"), 1, true)
        ));

        // Title
        JLabel statsTitle = new JLabel("Statistics");
        statsTitle.setFont(statsTitle.getFont().deriveFont(Font.BOLD, 18f));
        statsTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        statsPanel.add(statsTitle, BorderLayout.NORTH);

        // Stats content
        JPanel statsContent = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 15, 5);
        gbc.weightx = 1.0;

        // Overall progress panel
        JPanel overallProgressPanel = new JPanel(new BorderLayout(5, 5));
        overallProgressPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UIManager.getColor("Component.borderColor")),
            "Overall Progress"
        ));
        
        overallProgressBar = new JProgressBar(0, 100);
        overallProgressBar.setPreferredSize(new Dimension(overallProgressBar.getPreferredSize().width, 20));
        overallProgressBar.setStringPainted(true);
        overallProgressBar.setFont(overallProgressBar.getFont().deriveFont(Font.BOLD));
        overallProgressPanel.add(overallProgressBar, BorderLayout.CENTER);
        statsContent.add(overallProgressPanel, gbc);

        // Habit counts with icons
        gbc.gridy++;
        totalHabitsLabel = createStatsLabel("Total Habits", "0");
        statsContent.add(totalHabitsLabel, gbc);

        gbc.gridy++;
        activeHabitsLabel = createStatsLabel("Active Habits", "0");
        statsContent.add(activeHabitsLabel, gbc);

        gbc.gridy++;
        completedHabitsLabel = createStatsLabel("Completed Habits", "0");
        statsContent.add(completedHabitsLabel, gbc);

        // Add filler
        gbc.gridy++;
        gbc.weighty = 1.0;
        statsContent.add(Box.createVerticalGlue(), gbc);

        statsPanel.add(statsContent, BorderLayout.CENTER);
        return statsPanel;
    }

    private JLabel createStatsLabel(String title, String value) {
        JLabel label = new JLabel(String.format("<html><b>%s:</b> %s</html>", title, value));
        label.setFont(label.getFont().deriveFont(14f));
        label.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 1, 0, UIManager.getColor("Component.borderColor")),
            new EmptyBorder(5, 5, 5, 5)
        ));
        return label;
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

    private class ModernHabitListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
            JList<?> list, Object value, int index,
            boolean isSelected, boolean cellHasFocus) {
            
            JPanel panel = new JPanel(new BorderLayout(10, 5));
            panel.setBorder(new EmptyBorder(8, 10, 8, 10));
            
            if (isSelected) {
                panel.setBackground(UIManager.getColor("List.selectionBackground"));
                panel.setForeground(UIManager.getColor("List.selectionForeground"));
            } else {
                panel.setBackground(index % 2 == 0 ? 
                    UIManager.getColor("List.background") : 
                    UIManager.getColor("List.background").brighter());
                panel.setForeground(UIManager.getColor("List.foreground"));
            }
            
            if (value instanceof Habit) {
                Habit habit = (Habit) value;
                
                // Name and progress
                JPanel namePanel = new JPanel(new BorderLayout(5, 2));
                namePanel.setOpaque(false);
                
                JLabel nameLabel = new JLabel(habit.getName());
                nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14f));
                namePanel.add(nameLabel, BorderLayout.NORTH);
                
                // Progress bar
                JProgressBar progressBar = new JProgressBar(0, 100);
                progressBar.setValue((int) habit.getProgress());
                progressBar.setStringPainted(true);
                progressBar.setString(String.format("%.1f%%", habit.getProgress()));
                progressBar.setPreferredSize(new Dimension(progressBar.getPreferredSize().width, 5));
                namePanel.add(progressBar, BorderLayout.SOUTH);
                
                panel.add(namePanel, BorderLayout.CENTER);
                
                // Status and details
                JPanel detailsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
                detailsPanel.setOpaque(false);
                
                JLabel detailsLabel = new JLabel(String.format("%d days / %d frequency",
                    habit.getGoalDays(), habit.getFrequency()));
                detailsLabel.setFont(detailsLabel.getFont().deriveFont(Font.PLAIN, 12f));
                detailsPanel.add(detailsLabel);
                
                panel.add(detailsPanel, BorderLayout.EAST);
            }
            
            return panel;
        }
    }
} 