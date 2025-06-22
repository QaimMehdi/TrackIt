# TrackIt - Habit Tracking Application
## Final Report

## Overview
TrackIt is a modern Java desktop application designed to help users track and maintain their habits. The application features a clean, user-friendly interface built with Swing and enhanced with FlatLaf for a contemporary look and feel.

## Key Features

### 1. User Interface
- Modern, responsive design with FlatLaf theming
- Dark/Light mode toggle
- Split-pane layout with habits list and statistics panel
- Progress bars for visual tracking
- Clean, intuitive navigation

### 2. Habit Management
- Create new habits with:
  - Custom names
  - Goal duration (1-365 days)
  - Frequency settings (1-30 days)
  - Start dates
  - Optional notes
- View detailed habit progress
- Delete unwanted habits
- Mark habits as completed

### 3. Task System
- Two-tier task tracking:
  1. Daily check-ins for regular progress
  2. Milestone tasks based on frequency settings
- Task statuses:
  - PENDING
  - IN_PROGRESS
  - COMPLETED
  - FAILED
- Automatic task generation based on habit settings

### 4. Progress Tracking
- Individual habit progress bars
- Overall progress statistics
- Habit counts:
  - Total habits
  - Active habits
  - Completed habits
- Detailed task view with dates and status

### 5. Database Integration
- MySQL database for persistent storage
- Tables:
  - `habits`: Store habit information
  - `tasks`: Track individual tasks and progress
- Efficient indexing for better performance

## Technical Implementation

### 1. Architecture
- Model-View-Controller (MVC) pattern
- DAO pattern for database operations
- Singleton pattern for database connection

### 2. Key Classes
- `MainWindow`: Main application UI
- `HabitDetailsDialog`: Detailed habit view and editing
- `AddHabitDialog`: New habit creation
- `HabitDAO`: Database operations for habits
- `TaskDAO`: Database operations for tasks
- `Habit`: Model class for habits
- `Task`: Model class for tasks

### 3. Dependencies
- FlatLaf: Modern UI components
- MySQL Connector/J: Database connectivity
- JCalendar: Date selection
- Project Lombok: Boilerplate reduction

### 4. Database Schema
```sql
CREATE TABLE habits (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    goal_days INT NOT NULL,
    frequency INT NOT NULL,
    notes TEXT,
    creation_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    start_date DATETIME NOT NULL,
    completion_date DATETIME,
    status VARCHAR(50) DEFAULT 'ACTIVE'
);

CREATE TABLE tasks (
    id INT PRIMARY KEY AUTO_INCREMENT,
    habit_id INT NOT NULL,
    task_number INT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    start_date DATETIME NOT NULL,
    due_date DATETIME NOT NULL,
    completion_date DATETIME,
    notes TEXT,
    FOREIGN KEY (habit_id) REFERENCES habits(id) ON DELETE CASCADE
);
```

## Features in Detail

### Habit Creation
1. Users can create habits with specific goals and frequencies
2. The system automatically generates:
   - Daily check-in tasks for continuous tracking
   - Milestone tasks for longer-term motivation
   - A final completion task

### Progress Tracking
1. Visual progress bars show:
   - Individual habit completion percentage
   - Overall progress across all habits
2. Task status updates automatically reflect in progress calculations
3. Completed habits are clearly marked and preserved for reference

### Data Management
1. All data is persistently stored in MySQL
2. Automatic cleanup of related tasks when habits are deleted
3. Data integrity maintained through foreign key relationships

## User Experience
- Clean, intuitive interface
- Responsive design
- Clear visual feedback
- Consistent styling
- Easy navigation
- Informative statistics

## Security and Data Integrity
1. SQL injection prevention through prepared statements
2. Proper error handling and user feedback
3. Data validation before saving
4. Cascading deletes for data consistency

## Future Enhancement Possibilities
1. User authentication system
2. Data export/import functionality
3. Habit categories and tags
4. Reminder notifications
5. Progress graphs and analytics
6. Mobile companion app
7. Cloud synchronization
8. Social sharing features

## Conclusion
TrackIt provides a robust, user-friendly platform for habit tracking with a solid foundation for future enhancements. The application successfully combines modern UI design with reliable data management to help users build and maintain positive habits. 