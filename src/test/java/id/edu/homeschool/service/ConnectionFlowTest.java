package id.edu.homeschool.service;

import id.edu.homeschool.model.ConnectionRequestStatus;
import id.edu.homeschool.model.ConnectionStatus;
import id.edu.homeschool.model.InvitationStatus;
import id.edu.homeschool.support.TestEnvironment;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The teacher and student connection workflow end to end. */
class ConnectionFlowTest {

    @TempDir Path temp;
    private TestEnvironment environment;
    private UserSession teacher;
    private UserSession otherTeacher;
    private UserSession student;

    @BeforeEach void setUp() {
        environment = new TestEnvironment(temp);
        teacher = environment.registerTeacher("Pengajar Satu", "guru1@uji.test", "GuruPassword2040");
        otherTeacher = environment.registerTeacher("Pengajar Dua", "guru2@uji.test", "GuruPassword2041");
        student = environment.registerStudent("Siswa Satu", "siswa@uji.test", "SiswaPassword2040", "SMA");
    }

    @Test void invitationRequestApprovalAndActiveRelation() {
        CreatedInvitation invitation = environment.connections.createInvitation(teacher.token());
        assertEquals(1, environment.connections.ownInvitations(teacher.token()).size());

        ConnectionRequestSummary request = environment.connections.requestConnection(student.token(), invitation.code());
        assertEquals("Siswa Satu", request.studentName());
        assertEquals(ConnectionRequestStatus.PENDING, request.status());
        assertEquals(1, environment.connections.pendingForTeacher(teacher.token()).size());

        environment.connections.approve(teacher.token(), request.id());
        assertTrue(environment.connections.pendingForTeacher(teacher.token()).isEmpty());
        assertEquals("Pengajar Satu", environment.connections.activeTeachersForStudent(student.token()).getFirst().name());
        assertEquals("Siswa Satu", environment.connections.activeStudentsForTeacher(teacher.token()).getFirst().name());
        assertTrue(environment.connections.requestsForStudent(student.token()).stream()
                .anyMatch(value -> value.status() == ConnectionRequestStatus.APPROVED));
    }

    @Test void invitationStaysUsableUntilApprovedAndCanBeReusedAfterRejection() {
        CreatedInvitation invitation = environment.connections.createInvitation(teacher.token());
        ConnectionRequestSummary request = environment.connections.requestConnection(student.token(), invitation.code());

        // The code is not burned while the request is still waiting for a decision.
        assertEquals(InvitationStatus.ACTIVE, environment.repositories.invitations().findAll().getFirst().getStatus());
        assertEquals(1, environment.connections.ownInvitations(teacher.token()).getFirst().pendingRequests());

        environment.connections.reject(teacher.token(), request.id());
        assertEquals(InvitationStatus.ACTIVE, environment.repositories.invitations().findAll().getFirst().getStatus());

        // After a rejection the student may request again with the same code.
        ConnectionRequestSummary second = environment.connections.requestConnection(student.token(), invitation.code());
        assertEquals(ConnectionRequestStatus.PENDING, second.status());
        environment.connections.approve(teacher.token(), second.id());
        assertEquals(InvitationStatus.USED, environment.repositories.invitations().findAll().getFirst().getStatus());
    }

    @Test void duplicatePendingRequestIsRejectedButAnotherTeacherCanBeUsed() {
        CreatedInvitation first = environment.connections.createInvitation(teacher.token());
        environment.connections.requestConnection(student.token(), first.code());
        assertThrows(IllegalArgumentException.class,
                () -> environment.connections.requestConnection(student.token(), first.code()));

        CreatedInvitation second = environment.connections.createInvitation(otherTeacher.token());
        var request = environment.connections.requestConnection(student.token(), second.code());
        assertEquals("Pengajar Dua", request.teacherName());
    }

    @Test void teacherCannotApproveAnotherTeacherRequest() {
        CreatedInvitation invitation = environment.connections.createInvitation(teacher.token());
        var request = environment.connections.requestConnection(student.token(), invitation.code());
        assertThrows(SecurityException.class, () -> environment.connections.approve(otherTeacher.token(), request.id()));
        assertThrows(SecurityException.class, () -> environment.connections.reject(otherTeacher.token(), request.id()));
    }

    @Test void studentCannotUseTeacherOnlyActions() {
        assertThrows(SecurityException.class, () -> environment.connections.createInvitation(student.token()));
        assertThrows(SecurityException.class, () -> environment.connections.approve(student.token(), "apa saja"));
        assertThrows(SecurityException.class, () -> environment.connections.revokeInvitation(student.token(), "apa saja"));
    }

    @Test void invalidCodeIsRejectedWithoutTouchingAnyAccount() {
        assertThrows(IllegalArgumentException.class, () -> environment.connections.requestConnection(student.token(), "BUKAN-KODE"));
        assertThrows(IllegalArgumentException.class, () -> environment.connections.requestConnection(student.token(), "  "));
        assertTrue(environment.connections.activeTeachersForStudent(student.token()).isEmpty());
    }

    @Test void teacherCanEndAnActiveConnection() {
        environment.connect(teacher, student);
        assertEquals(1, environment.connections.activeStudentsForTeacher(teacher.token()).size());

        environment.connections.removeConnection(teacher.token(), student.accountId());
        assertTrue(environment.connections.activeStudentsForTeacher(teacher.token()).isEmpty());
        assertTrue(environment.connections.activeTeachersForStudent(student.token()).isEmpty());
        assertFalse(environment.repositories.connections().findAll().stream()
                .anyMatch(value -> value.getStatus() == ConnectionStatus.ACTIVE));
    }
}
