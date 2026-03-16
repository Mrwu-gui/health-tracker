package com.healthtracker.dto;

import jakarta.validation.constraints.NotNull;

public class AdminSubscribeTaskUpdateRequest {
    @NotNull
    private Long id;

    private Integer status;
    private String sendTime;
    private String response;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
