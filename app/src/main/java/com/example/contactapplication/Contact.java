package com.example.contactapplication;

import java.io.Serializable;

public class Contact implements Serializable {

    private int id;
    private String name;
    private String phone;
    private String profilePictureUri;

    public Contact() {
    }

    public Contact(int id, String name, String phone, String profilePictureUri) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.profilePictureUri = profilePictureUri;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public void setProfilePictureUri(String profilePictureUri) {
        this.profilePictureUri = profilePictureUri;
    }

    public void updateContact(int id, String name, String phone, String profilePictureUri) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.profilePictureUri = profilePictureUri;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
