package id.edu.homeschool.service;

import id.edu.homeschool.model.InvitationStatus;
import java.time.Instant;

public record InvitationSummary(String id, Instant createdAt, Instant expiresAt, InvitationStatus status,
                                int pendingRequests) { }
