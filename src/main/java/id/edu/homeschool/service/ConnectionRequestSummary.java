package id.edu.homeschool.service;

import id.edu.homeschool.model.ConnectionRequestStatus;
import java.time.Instant;

public record ConnectionRequestSummary(String id, String studentName, String teacherName,
        ConnectionRequestStatus status, Instant createdAt, Instant updatedAt) { }
