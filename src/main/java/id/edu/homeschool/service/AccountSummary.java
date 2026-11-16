package id.edu.homeschool.service;

import id.edu.homeschool.model.AccountRole;

public record AccountSummary(String id, String name, String email, AccountRole role, String educationLevel) { }
