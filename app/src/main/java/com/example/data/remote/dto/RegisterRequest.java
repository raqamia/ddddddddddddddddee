package com.example.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("data")
    private Data data;

    public RegisterRequest(String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.data = new Data(name);
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Data getData() { return data; }

    public static class Data {
        @SerializedName("full_name")
        private String fullName;

        public Data(String fullName) {
            this.fullName = fullName;
        }

        public String getFullName() { return fullName; }
    }
}
