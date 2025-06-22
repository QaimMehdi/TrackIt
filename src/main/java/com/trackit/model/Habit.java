package com.trackit.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Habit {
    private Integer id;
    private String name;
    private Integer goalDays;
    private Integer frequency;
    private String notes;
    private LocalDateTime creationDate;
    private LocalDateTime startDate;
    private LocalDateTime completionDate;
    private String status;
    private List<Task> tasks;

    public enum Status {
        ACTIVE,
        COMPLETED,
        ABANDONED
    }

    public double getProgress() {
        if (tasks == null || tasks.isEmpty()) {
            return 0.0;
        }
        
        long completedTasks = tasks.stream()
                .filter(task -> task.getStatus().equals(Task.Status.COMPLETED.name()))
                .count();
                
        return (double) completedTasks / tasks.size() * 100;
    }

    public boolean isCompleted() {
        return Status.COMPLETED.name().equals(status);
    }

    public boolean isActive() {
        return Status.ACTIVE.name().equals(status);
    }

    public void addTask(Task task) {
        if (tasks == null) {
            tasks = new ArrayList<>();
        }
        tasks.add(task);
    }
} 