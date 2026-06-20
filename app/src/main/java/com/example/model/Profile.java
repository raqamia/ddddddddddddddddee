package com.example.model;

public class Profile {
    private String id;
    private String email;
    private String name;
    private String track;

    public Profile() {}

    public Profile(String id, String email, String name, String track) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.track = track;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }
}
