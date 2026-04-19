package com.kma.oj_app_mobile.dto;

public class RankingUser {
    private String id;
    private String username;
    private String avatarUrl;
    private Integer solvedCount;
    private Integer acCount;
    private Integer submissionCount;
    private Double totalScore;
    private Integer rank;

    // Getters
    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public Integer getSolvedCount() { return solvedCount; }
    public Integer getAcCount() { return acCount; }
    public Integer getSubmissionCount() { return submissionCount; }
    public Double getTotalScore() { return totalScore; }
    public Integer getRank() { return rank; }
}
