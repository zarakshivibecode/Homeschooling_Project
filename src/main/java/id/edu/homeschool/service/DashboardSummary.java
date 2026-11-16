package id.edu.homeschool.service;

public record DashboardSummary(int subjectCount, int studentCount, int teacherCount,
                               int sessionsToday, int openTasks,
                               int overdueTasks, int achievedGoals, int totalGoals,
                               double averageScorePercent) { }
