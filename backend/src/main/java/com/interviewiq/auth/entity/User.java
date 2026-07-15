package com.interviewiq.auth.entity;

import com.interviewiq.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    private String targetRole;

    private String experienceLevel;

    private String targetCompanies;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean active = true;

    protected User() {
    }

    public User(String email, String passwordHash, String fullName) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
    }

    public void updateProfile(String fullName, String targetRole, String experienceLevel, String targetCompanies) {
        this.fullName = fullName;
        this.targetRole = targetRole;
        this.experienceLevel = experienceLevel;
        this.targetCompanies = targetCompanies;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public String getExperienceLevel() {
        return experienceLevel;
    }

    public String getTargetCompanies() {
        return targetCompanies;
    }

    public Role getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }
}

