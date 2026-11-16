package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;

/**
 * A connected person together with the learning figures that the signed-in account is allowed
 * to see. It powers the directory pages so both roles get a meaningful overview.
 */
public record PersonSummary(String id, String name, String email, AccountRole role, String educationLevel,
                            int subjectCount, int materialCount, int taskCount, int completedTaskCount,
                            int evaluationCount, double averagePercent) {
}
