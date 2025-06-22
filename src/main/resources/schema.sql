-- Create the database
CREATE DATABASE IF NOT EXISTS trackit;
USE trackit;

-- Create habits table
CREATE TABLE IF NOT EXISTS habits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    goal_days INT NOT NULL,
    frequency INT NOT NULL,
    notes TEXT,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATETIME NOT NULL,
    completion_date DATETIME,
    status VARCHAR(50) DEFAULT 'ACTIVE' -- ACTIVE, COMPLETED, ABANDONED
);

-- Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    habit_id INT NOT NULL,
    task_number INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING', -- PENDING, IN_PROGRESS, COMPLETED, FAILED
    start_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    completion_date DATETIME,
    notes TEXT,
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_habit_status ON habits(status);
CREATE INDEX idx_task_status ON tasks(status);
CREATE INDEX idx_task_habit_id ON tasks(habit_id);

-- Insert some sample data
INSERT INTO habits (name, goal_days, frequency, notes, start_date)
VALUES 
('Daily Exercise', 30, 1, 'Exercise for at least 30 minutes', NOW()),
('Read Books', 90, 7, 'Read at least one chapter', DATE_ADD(NOW(), INTERVAL 1 DAY)),
('Learn Programming', 60, 2, 'Practice coding on LeetCode', DATE_ADD(NOW(), INTERVAL 2 DAY)); 