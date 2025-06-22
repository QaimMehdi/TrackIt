package com.trackit.db;

import com.trackit.model.Task;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {
    private final Connection connection;

    public TaskDAO() {
        this.connection = DBConnection.getInstance().getConnection();
    }

    public Task createTask(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (habit_id, task_number, status, start_date, due_date) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, task.getHabitId());
            stmt.setInt(2, task.getTaskNumber());
            stmt.setString(3, task.getStatus());
            stmt.setTimestamp(4, Timestamp.valueOf(task.getStartDate()));
            stmt.setTimestamp(5, Timestamp.valueOf(task.getDueDate()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating task failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                    return task;
                } else {
                    throw new SQLException("Creating task failed, no ID obtained.");
                }
            }
        }
    }

    public List<Task> getTasksForHabit(int habitId) throws SQLException {
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

    public void updateTask(Task task) throws SQLException {
        String sql = "UPDATE tasks SET status = ?, completion_date = ?, notes = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, task.getStatus());
            
            if (task.getCompletionDate() != null) {
                stmt.setTimestamp(2, Timestamp.valueOf(task.getCompletionDate()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            
            stmt.setString(3, task.getNotes());
            stmt.setInt(4, task.getId());
            
            stmt.executeUpdate();
        }
    }

    public void updateTaskStatus(int taskId, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ?, completion_date = ? WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, status);
            
            if (Task.Status.COMPLETED.name().equals(status)) {
                stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            } else {
                stmt.setNull(2, Types.TIMESTAMP);
            }
            
            stmt.setInt(3, taskId);
            stmt.executeUpdate();
        }
    }

    public void deleteTask(int id) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void deleteTasksForHabit(int habitId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE habit_id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, habitId);
            stmt.executeUpdate();
        }
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