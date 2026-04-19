package com.kma.oj_app_mobile.models;

public class Contest {
    private String id;
    private String title;
    private String contestKey;
    private String startTime;
    private String endTime;
    private String ruleType;
    private String contestStatus;
    private String visibility;
    private Long participantCount;
    private String status;
    private Integer durationMinutes;
    private String format;
    private Boolean allowLateRegistration;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContestKey() { return contestKey; }
    public void setContestKey(String contestKey) { this.contestKey = contestKey; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getRuleType() { return ruleType; }
    public void setRuleType(String ruleType) { this.ruleType = ruleType; }

    public String getContestStatus() { return contestStatus; }
    public void setContestStatus(String contestStatus) { this.contestStatus = contestStatus; }

    public String getVisibility() { return visibility; }
    public void setVisibility(String visibility) { this.visibility = visibility; }

    public Long getParticipantCount() { return participantCount; }
    public void setParticipantCount(Long participantCount) { this.participantCount = participantCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }

    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }

    public Boolean getAllowLateRegistration() { return allowLateRegistration; }
    public void setAllowLateRegistration(Boolean allowLateRegistration) { this.allowLateRegistration = allowLateRegistration; }
}
