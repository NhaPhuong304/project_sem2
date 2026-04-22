package com.aptech.projectmgmt.model;

public class SubmissionRequirementTemplate {

    private int templateId;
    private String requirementName;
    private String requiredExtension;
    private int sortOrder;
    private boolean active;

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return requirementName + " (" + requiredExtension + ")";
    }
}
