package id.edu.homeschool.service;

import java.util.List;

/** Progress figures derived from the learning records a caller is allowed to read. */
public record ProgressReport(int subjectCount, int materialCount, int sessionCount,
                             int taskCount, int completedTaskCount, int overdueTaskCount,
                             int goalCount, int achievedGoalCount,
                             int evaluationCount, double averagePercent,
                             int gradedSubjectCount,
                             List<SubjectProgress> subjects) {

    public record SubjectProgress(String subjectId, String subjectName, int taskCount, int completedTaskCount,
                                  int evaluationCount, double averagePercent) { }

    /** Share of finished tasks, expressed as a percentage of all tasks. */
    public double taskCompletionPercent() {
        return taskCount == 0 ? 0 : completedTaskCount * 100.0 / taskCount;
    }

    /** Share of achieved goals, expressed as a percentage of all goals. */
    public double goalAchievementPercent() {
        return goalCount == 0 ? 0 : achievedGoalCount * 100.0 / goalCount;
    }

    /**
     * A short Indonesian narrative of the report so the dashboard and the report page can show a
     * readable conclusion instead of only numbers.
     */
    public String summary() {
        if (subjectCount == 0) {
            return "Belum ada mata pelajaran yang tercatat untuk siswa ini. Mulai dengan menambah mata pelajaran, lalu materi, jadwal, dan evaluasi.";
        }
        StringBuilder text = new StringBuilder();
        text.append("Siswa ini belajar pada ").append(subjectCount).append(" mata pelajaran dengan ")
                .append(materialCount).append(" materi dan ").append(sessionCount).append(" sesi belajar. ");
        if (taskCount == 0) {
            text.append("Belum ada tugas yang diberikan. ");
        } else {
            text.append("Dari ").append(taskCount).append(" tugas, ").append(completedTaskCount)
                    .append(" sudah selesai (").append(Math.round(taskCompletionPercent())).append("%)");
            if (overdueTaskCount > 0) {
                text.append(" dan ").append(overdueTaskCount).append(" sudah melewati tenggat");
            }
            text.append(". ");
        }
        if (goalCount > 0) {
            text.append("Target belajar yang sudah tercapai ").append(achievedGoalCount).append(" dari ")
                    .append(goalCount).append(". ");
        }
        if (evaluationCount > 0) {
            text.append("Rata-rata nilai ").append(String.format(java.util.Locale.ROOT, "%.1f", averagePercent))
                    .append("% dari ").append(evaluationCount).append(" evaluasi.");
        } else {
            text.append("Belum ada evaluasi yang tercatat.");
        }
        return text.toString();
    }
}
