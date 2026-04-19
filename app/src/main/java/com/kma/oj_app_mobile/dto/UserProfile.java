package com.kma.oj_app_mobile.dto;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("id")
    private String id;
    @SerializedName("username")
    private String username;
    @SerializedName("email")
    private String email;
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("avatarUrl")
    private String avatarUrl;
    @SerializedName("bio")
    private String bio;
    @SerializedName("gender")
    private String gender;
    @SerializedName("phoneNumber")
    private String phoneNumber;
    @SerializedName("school")
    private String school;
    @SerializedName("major")
    private String major;
    @SerializedName("address")
    private String address;
    @SerializedName("city")
    private String city;
    @SerializedName("country")
    private String country;
    @SerializedName("githubUrl")
    private String githubUrl;
    @SerializedName("linkedInUrl")
    private String linkedInUrl;
    @SerializedName("website")
    private String website;
    @SerializedName("createdDate")
    private String createdDate;

    // Stats
    @SerializedName("solvedCount")
    private Integer solvedCount;
    @SerializedName("submissionCount")
    private Integer submissionCount;
    @SerializedName("acCount")
    private Integer acCount;
    @SerializedName("totalScore")
    private Double totalScore;

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getBio() { return bio; }
    public String getGender() { return gender; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getSchool() { return school; }
    public String getMajor() { return major; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getGithubUrl() { return githubUrl; }
    public String getLinkedInUrl() { return linkedInUrl; }
    public String getWebsite() { return website; }
    public String getCreatedDate() { return createdDate; }

    public Integer getSolvedCount() { return solvedCount; }
    public Integer getSubmissionCount() { return submissionCount; }
    public Integer getAcCount() { return acCount; }
    public Double getTotalScore() { return totalScore; }
}
