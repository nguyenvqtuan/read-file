package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

@Entity
public class User {
    @Id
    @Column(length = 10)
    private String id;

    @Column(nullable = false)
    private String fullName;

    @Column(length = 8, nullable = false)
    private String birthday;

    @Column(nullable = false)
    private int experience;

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
} 