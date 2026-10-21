package com.app.alhawanajd;


public class AudioCategory {
    private String id;
    private String name;
    private String arabic_name;

    // Constructor
    public AudioCategory(String id, String name, String arabic_name) {
        this.id = id;
        this.name = name;
        this.arabic_name = arabic_name;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getArabicName() {
        return arabic_name;
    }

    // For logging/debugging
    @Override
    public String toString() {
        return "AudioCategory{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", arabic_name='" + arabic_name + '\'' +
                '}';
    }
}
