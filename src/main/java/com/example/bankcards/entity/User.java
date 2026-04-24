package com.example.bankcards.entity;

import jakarta.persistence.Entity;
import java.time.Instant;

@Entity
public class User {
    private long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private Role role;
    private UserStatus status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant deletedAt;
}
