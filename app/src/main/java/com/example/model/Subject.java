package com.example.model;

public class Subject {
    private String id;
    private String name;
    private String track;
    private int orderIndex;

    public Subject() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTrack() { return track; }
    public void setTrack(String track) { this.track = track; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
}
