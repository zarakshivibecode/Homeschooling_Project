package id.edu.homeschool.service;

import java.time.LocalDateTime;

/**
 * One entry of the recent activity feed. The dashboard builds the feed from real records, so the
 * list always reflects the data that is actually stored.
 */
public record ActivityEntry(LocalDateTime when, String category, String title, String detail, String status) {
}
