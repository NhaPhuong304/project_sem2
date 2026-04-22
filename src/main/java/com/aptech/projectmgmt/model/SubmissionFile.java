package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class SubmissionFile {

    private int fileId;
    private int targetId;
    private int requirementId;
    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private long fileSize;
    private int uploadedByStudentId;
    private LocalDateTime uploadedAt;
    private String requirementName;
    private String requiredExtension;

    public int getFileId() {
        return fileId;
    }

    public void setFileId(int fileId) {
        this.fileId = fileId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getRequirementId() {
        return requirementId;
    }

    public void setRequirementId(int requirementId) {
        this.requirementId = requirementId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public void setOriginalFileName(String originalFileName) {
        this.originalFileName = originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public void setStoredFileName(String storedFileName) {
        this.storedFileName = storedFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getUploadedByStudentId() {
        return uploadedByStudentId;
    }

    public void setUploadedByStudentId(int uploadedByStudentId) {
        this.uploadedByStudentId = uploadedByStudentId;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
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
}
