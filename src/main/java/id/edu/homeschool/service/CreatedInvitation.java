package id.edu.homeschool.service;

import java.time.Instant;

/** Plain invitation code is returned only at creation and must be shown once by the UI. */
public record CreatedInvitation(String code, Instant expiresAt) {
    @Override public String toString() { return "CreatedInvitation[code=REDACTED, expiresAt=" + expiresAt + "]"; }
}
