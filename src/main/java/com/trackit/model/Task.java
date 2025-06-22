package com.trackit.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    private Integer id;
    private Integer habitId;
    private Integer taskNumber;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime dueDate;
    private LocalDateTime completionDate;
    private String notes;

    public enum Status {
        PENDING,
        IN_PROGRESS,
        COMPLETED,
        FAILED
    }

    public boolean isCompleted() {
        return Status.COMPLETED.name().equals(status);
    }

    public boolean isPending() {
        return Status.PENDING.name().equals(status);
    }

    public boolean isInProgress() {
        return Status.IN_PROGRESS.name().equals(status);
    }

    public boolean isFailed() {
        return Status.FAILED.name().equals(status);
    }

    public boolean isOverdue() {
        return dueDate != null && 
               LocalDateTime.now().isAfter(dueDate) && 
               !isCompleted();
    }
} 