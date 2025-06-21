// UserProfile.java - Model for user profile
package com.taskshabitstracker.model;

public class UserProfile {
    private final String email;
    private final String name;
    private final String profilePicture;

    public UserProfile(String email, String name, String profilePicture) {
        this.email = email;
        this.name = name;
        this.profilePicture = profilePicture;
    }

    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getProfilePicture() { return profilePicture; }
}