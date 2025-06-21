// UserProfile.java - Model for user profile
package com.taskshabitstracker.model;

public class UserProfile {
    private String id;
    private String email;
    private String name;
//    private String profilePicture;

    // Default constructor
    public UserProfile() {}
    // Constructor with all fields
    public UserProfile(String id, String email, String name, String profilePicture) {
        this.id = id;
        this.email = email;
        this.name = name;
//        this.profilePicture = profilePicture;
    }

    // Constructor without profilePicture
    public UserProfile(String id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
//        this.profilePicture = null;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
//    public String getProfilePicture() { return profilePicture; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
//    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}