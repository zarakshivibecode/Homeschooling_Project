package id.edu.homeschool.service;

public record ProgressSummary(int completedTasks, int totalTasks, int achievedGoals,
                              int totalGoals, int evaluationCount, double averageScore,
                              double averageMaximum, double averagePercent) { }
