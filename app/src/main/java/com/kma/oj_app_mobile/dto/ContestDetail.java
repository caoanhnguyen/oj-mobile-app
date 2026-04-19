package com.kma.oj_app_mobile.dto;

import com.google.gson.annotations.SerializedName;

public class ContestDetail {
    private String id;
    private String title;
    private String contestKey;
    private String description;
    private String startTime;
    private String endTime;
    private String ruleType;
    private String contestStatus;
    private String visibility;
    private Integer durationMinutes;
    private String format;
    private Boolean allowLateRegistration;
    private Long participantCount;
    private String scoreboardVisibility;
    
    @SerializedName("isRegistered")
    private boolean isRegistered;
    
    private String authorUsername;

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContestKey() { return contestKey; }
    public String getDescription() { return description; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getRuleType() { return ruleType; }
    public String getContestStatus() { return contestStatus; }
    public String getVisibility() { return visibility; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public String getFormat() { return format; }
    public Boolean getAllowLateRegistration() { return allowLateRegistration; }
    public Long getParticipantCount() { return participantCount; }
    public String getScoreboardVisibility() { return scoreboardVisibility; }
    public boolean isRegistered() { return isRegistered; }
    public void setRegistered(boolean registered) { isRegistered = registered; }
    public String getAuthorUsername() { return authorUsername; }
}
