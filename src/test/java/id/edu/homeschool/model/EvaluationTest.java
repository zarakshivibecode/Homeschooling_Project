package id.edu.homeschool.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class EvaluationTest {
    @Test void calculatesPercentageFromScoreAndMaximum() {
        Evaluation evaluation = new Evaluation("math", LocalDate.now(), 18, 24, "quiz");
        assertEquals(75.0, evaluation.getPercentage(), 0.0001);
    }

    @Test void rejectsNegativeAndOverMaximumScores() {
        assertThrows(IllegalArgumentException.class, () -> new Evaluation("math", LocalDate.now(), -1, 10, ""));
        assertThrows(IllegalArgumentException.class, () -> new Evaluation("math", LocalDate.now(), 11, 10, ""));
    }

    @Test void updatesScoreAndMaximumAsOneValidatedPair() {
        Evaluation evaluation = new Evaluation("math", LocalDate.now(), 90, 100, "exam");
        evaluation.setScores(45, 50);
        assertEquals(90.0, evaluation.getPercentage(), 0.0001);
    }
}
