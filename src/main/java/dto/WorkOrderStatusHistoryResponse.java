package com.zidio.keystone.dto;

import java.time.LocalDateTime;

public class WorkOrderStatusHistoryResponse {

    private Long id;
    private String fromStatus;
    private String toStatus;
    private String changedBy;
    private LocalDateTime changedAt;
    private String note;

    public WorkOrderStatusHistoryResponse() {
    }

    public WorkOrderStatusHistoryResponse(
            Long id,
            String fromStatus,
            String toStatus,
            String changedBy,
            LocalDateTime changedAt,
            String note) {

        this.id = id;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changedBy = changedBy;
        this.changedAt = changedAt;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public String getFromStatus() {
        return fromStatus;
    }

    public String getToStatus() {
        return toStatus;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }
}