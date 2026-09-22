package com.cruz.financialcontrol.model.entity;

import com.cruz.financialcontrol.model.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "USERS")
public class User extends BaseEntity {

    @Column(length = 50, nullable = false)
    private String name;

    @Column(unique = true, length = 100, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    private Instant lockedUntil;
}

