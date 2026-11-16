package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;

/** Opaque session credential plus safe display details; the token is valid only in this process. */
public record UserSession(String token, String accountId, String name, String email, AccountRole role,
                          String educationLevel) {
    @Override public String toString() {
        return "UserSession[accountId=" + accountId + ", name=" + name + ", role=" + role + ", token=REDACTED]";
    }
}
