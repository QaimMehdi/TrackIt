# TrackIt - Habit Tracking Application

TrackIt is a desktop application built with Java Swing and MySQL that helps users track and maintain their habits. It provides a user-friendly interface for managing habits, tracking progress, and maintaining consistency in personal development goals.

## Features

- Create and manage habits with customizable goals and frequencies
- Track daily progress with detailed task breakdowns
- View overall statistics and completion rates
- Mark tasks as completed, in progress, or failed
- Visual progress tracking with progress bars
- Clean and intuitive user interface

## Technical Stack

- Java 11
- Java Swing for GUI
- MySQL 8.0 for database
- Maven for dependency management

## Prerequisites

- Java JDK 11 or higher
- MySQL 8.0 or higher
- Maven

## Database Setup

1. Install MySQL if not already installed
2. Create a database named `trackit`
3. Run the SQL scripts in `src/main/resources/schema.sql`

## Building and Running

1. Clone the repository:
```bash
git clone [your-repository-url]
cd TrackIt
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn exec:java
```

## Project Structure

- `src/main/java/com/trackit/`
  - `db/` - Database connection and DAO classes
  - `model/` - Data models (Habit, Task)
  - `ui/` - User interface components
  - `Main.java` - Application entry point

## Database Schema

### Habits Table
- id (Primary Key)
- name
- goal_days
- frequency
- notes
- creation_date
- start_date
- completion_date
- status

### Tasks Table
- id (Primary Key)
- habit_id (Foreign Key)
- task_number
- status
- start_date
- due_date
- completion_date
- notes

## Contributing

This project is part of a learning exercise. Feel free to fork and modify for your own use.

## License

This project is open source and available under the MIT License. 