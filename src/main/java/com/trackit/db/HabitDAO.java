package com.trackit.db;

import com.trackit.model.Habit;
import com.trackit.model.Task;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {
    private final Connection connection;

    public HabitDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public Habit createHabit(Habit habit) throws SQLException {
        String sql = "INSERT INTO habits (name, goal_days, frequency, notes, start_date) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, habit.getName());
            stmt.setInt(2, habit.getGoalDays());
            stmt.setInt(3, habit.getFrequency());
            stmt.setString(4, habit.getNotes());
            stmt.setTimestamp(5, Timestamp.valueOf(habit.getStartDate()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating habit failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    habit.setId(generatedKeys.getInt(1));
                    generateTasks(habit);
                    return habit;
                } else {
                    throw new SQLException("Creating habit failed, no ID obtained.");
                }
            }
        }
    }

    public List<Habit> getAllHabits() throws SQLException {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits ORDER BY creation_date DESC";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Habit habit = mapResultSetToHabit(rs);
                habit.setTasks(getTasksForHabit(habit.getId()));
                habits.add(habit);
            }
        }
        
        return habits;
    }

    public Habit getHabitById(int id) throws SQLException {
        String sql = "SELECT * FROM habits WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Habit habit = mapResultSetToHabit(rs);
                    habit.setTasks(getTasksForHabit(id));
                    return habit;
                }
            }
        }
        
        return null;
    }

    public void updateHabit(Habit habit) throws SQLException {
        String sql = "UPDATE habits SET name = ?, goal_days = ?, frequency = ?, notes = ?, " +
                    "start_date = ?, completion_date = ?, status = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, habit.getName());
            stmt.setInt(2, habit.getGoalDays());
            stmt.setInt(3, habit.getFrequency());
            stmt.setString(4, habit.getNotes());
            stmt.setTimestamp(5, Timestamp.valueOf(habit.getStartDate()));
            stmt.setTimestamp(6, habit.getCompletionDate() != null ? 
                            Timestamp.valueOf(habit.getCompletionDate()) : null);
            stmt.setString(7, habit.getStatus());
            stmt.setInt(8, habit.getId());
            
            stmt.executeUpdate();
        }
    }

    public void deleteHabit(int id) throws SQLException {
        String sql = "DELETE FROM habits WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    private List<Task> getTasksForHabit(int habitId) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks WHERE habit_id = ? ORDER BY task_number";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapResultSetToTask(rs));
                }
            }
        }
        
        return tasks;
    }

    private void generateTasks(Habit habit) throws SQLException {
        LocalDateTime startDate = habit.getStartDate();
        int goalDays = habit.getGoalDays();
        int frequency = habit.getFrequency();
        
        // Calculate number of regular check-in tasks
        int regularTasks = goalDays;
        
        // Calculate milestone tasks (every frequency days)
        int milestoneTasks = (int) Math.ceil((double) goalDays / frequency);
        
        String sql = "INSERT INTO tasks (habit_id, task_number, status, start_date, due_date, notes) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int taskNumber = 1;
            
            // Generate daily check-in tasks
            for (int day = 0; day < regularTasks; day++) {
                stmt.setInt(1, habit.getId());
                stmt.setInt(2, taskNumber++);
                stmt.setString(3, Task.Status.PENDING.name());
                LocalDateTime taskStartDate = startDate.plusDays(day);
                LocalDateTime taskDueDate = taskStartDate.plusDays(1);
                stmt.setTimestamp(4, Timestamp.valueOf(taskStartDate));
                stmt.setTimestamp(5, Timestamp.valueOf(taskDueDate));
                stmt.setString(6, "Daily check-in");
                stmt.addBatch();
            }
            
            // Generate milestone tasks
            for (int milestone = 0; milestone < milestoneTasks; milestone++) {
                stmt.setInt(1, habit.getId());
                stmt.setInt(2, taskNumber++);
                stmt.setString(3, Task.Status.PENDING.name());
                LocalDateTime milestoneStartDate = startDate.plusDays(milestone * frequency);
                LocalDateTime milestoneDueDate = milestoneStartDate.plusDays(frequency);
                stmt.setTimestamp(4, Timestamp.valueOf(milestoneStartDate));
                stmt.setTimestamp(5, Timestamp.valueOf(milestoneDueDate));
                
                // Create descriptive milestone message
                int milestoneNumber = milestone + 1;
                int progressDays = milestone * frequency;
                int remainingDays = goalDays - progressDays;
                String milestoneMsg = String.format("Milestone %d of %d - %d day streak! %s", 
                    milestoneNumber, 
                    milestoneTasks,
                    frequency,
                    remainingDays <= frequency ? "Final stretch!" : 
                        String.format("%d days remaining", remainingDays)
                );
                stmt.setString(6, milestoneMsg);
                stmt.addBatch();
            }
            
            // Add a final completion milestone
            stmt.setInt(1, habit.getId());
            stmt.setInt(2, taskNumber);
            stmt.setString(3, Task.Status.PENDING.name());
            LocalDateTime finalStartDate = startDate.plusDays(goalDays - 1);
            LocalDateTime finalDueDate = finalStartDate.plusDays(1);
            stmt.setTimestamp(4, Timestamp.valueOf(finalStartDate));
            stmt.setTimestamp(5, Timestamp.valueOf(finalDueDate));
            stmt.setString(6, String.format("Final Goal - Complete %d day journey!", goalDays));
            stmt.addBatch();
            
            stmt.executeBatch();
        }
    }

    private Habit mapResultSetToHabit(ResultSet rs) throws SQLException {
        Habit habit = new Habit();
        habit.setId(rs.getInt("id"));
        habit.setName(rs.getString("name"));
        habit.setGoalDays(rs.getInt("goal_days"));
        habit.setFrequency(rs.getInt("frequency"));
        habit.setNotes(rs.getString("notes"));
        habit.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
        habit.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        
        Timestamp completionDate = rs.getTimestamp("completion_date");
        if (completionDate != null) {
            habit.setCompletionDate(completionDate.toLocalDateTime());
        }
        
        habit.setStatus(rs.getString("status"));
        return habit;
    }

    private Task mapResultSetToTask(ResultSet rs) throws SQLException {
        Task task = new Task();
        task.setId(rs.getInt("id"));
        task.setHabitId(rs.getInt("habit_id"));
        task.setTaskNumber(rs.getInt("task_number"));
        task.setStatus(rs.getString("status"));
        task.setStartDate(rs.getTimestamp("start_date").toLocalDateTime());
        task.setDueDate(rs.getTimestamp("due_date").toLocalDateTime());
        
        Timestamp completionDate = rs.getTimestamp("completion_date");
        if (completionDate != null) {
            task.setCompletionDate(completionDate.toLocalDateTime());
        }
        
        task.setNotes(rs.getString("notes"));
        return task;
    }
} 