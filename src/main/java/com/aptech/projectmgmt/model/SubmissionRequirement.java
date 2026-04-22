package com.aptech.projectmgmt.model;

public class SubmissionRequirement {

    private int requirementId;
    private int requestId;
    private String requirementName;
    private String requiredExtension;
    private int sortOrder;
    private boolean required;

    public int getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(int requirementId) {
        this.requirementId = requirementId;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public String getRequirementName() {
        return requirementName;
    }

    public void setRequirementName(String requirementName) {
        this.requirementName = requirementName;
    }

    public String getRequiredExtension() {
        return requiredExtension;
    }

    public void setRequiredExtension(String requiredExtension) {
        this.requiredExtension = requiredExtension;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }
}
