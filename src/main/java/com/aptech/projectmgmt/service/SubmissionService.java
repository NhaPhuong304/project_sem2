package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.SubmissionFile;
import com.aptech.projectmgmt.model.SubmissionRecipientOption;
import com.aptech.projectmgmt.model.SubmissionRequirement;
import com.aptech.projectmgmt.model.SubmissionRequirementTemplate;
import com.aptech.projectmgmt.model.SubmissionRequest;
import com.aptech.projectmgmt.model.SubmissionTarget;
import com.aptech.projectmgmt.repository.MessageRepository;
import com.aptech.projectmgmt.repository.SubmissionRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SubmissionService {

    private static final String REQUEST_MESSAGE_PREFIX = "SUBMISSION_REQUEST|";
    private static final Path SUBMISSION_ROOT = Paths.get("submissions");
    private static final DateTimeFormatter FILE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final SubmissionRepository submissionRepository = new SubmissionRepository();
    private final MessageRepository messageRepository = new MessageRepository();

    public List<SubmissionRequirementTemplate> getDefaultRequirements() {
        return submissionRepository.findActiveTemplates();
    }

    public List<SubmissionRecipientOption> getRecipientOptionsForStaff(int staffId) {
        return submissionRepository.findRecipientOptionsForStaff(staffId);
    }

    public int createRequest(String title, String description, LocalDateTime deadline, int staffId,
            List<SubmissionRequirementTemplate> requirements, List<Integer> groupIds) {
        String normalizedTitle = title != null ? title.trim() : "";
        if (normalizedTitle.isEmpty()) {
            throw new RuntimeException("Tiêu đề yêu cầu không được để trống");
        }
        if (deadline == null) {
            throw new RuntimeException("Hạn nộp không được để trống");
        }
        if (requirements == null || requirements.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một nội dung cần nộp");
        }
        if (groupIds == null || groupIds.isEmpty()) {
            throw new RuntimeException("Vui lòng chọn ít nhất một nhóm nhận yêu cầu");
        }

        Map<Integer, Integer> targetLeaders = new LinkedHashMap<>();
        for (Integer groupId : groupIds) {
            Integer leaderStudentId = submissionRepository.findLeaderStudentIdByGroup(groupId);
            if (leaderStudentId != null) {
                targetLeaders.put(groupId, leaderStudentId);
            }
        }
        if (targetLeaders.isEmpty()) {
            throw new RuntimeException("Không tìm thấy leader đang hoạt động trong các nhóm đã chọn");
        }

        SubmissionRequest request = new SubmissionRequest();
        request.setTitle(normalizedTitle);
        request.setDescription(description != null ? description.trim() : "");
        request.setDeadline(deadline);
        request.setCreatedByStaffId(staffId);

        int requestId = submissionRepository.createRequestWithRequirements(request, requirements);
        for (Map.Entry<Integer, Integer> entry : targetLeaders.entrySet()) {
            int groupId = entry.getKey();
            int leaderStudentId = entry.getValue();
            submissionRepository.addTarget(requestId, groupId, leaderStudentId);
            sendRequestMessage(staffId, leaderStudentId, normalizedTitle, deadline);
        }
        return requestId;
    }

    public List<SubmissionRequest> getRequestsForStaff(int staffId) {
        return submissionRepository.findRequestsForStaff(staffId);
    }

    public List<SubmissionTarget> getTargetsForLeader(int leaderStudentId) {
        return submissionRepository.findTargetsForLeader(leaderStudentId);
    }

    public List<SubmissionTarget> getTargetsByRequest(int requestId) {
        return submissionRepository.findTargetsByRequest(requestId);
    }

    public List<SubmissionRequirement> getRequirements(int requestId) {
        return submissionRepository.findRequirementsByRequest(requestId);
    }

    public List<SubmissionFile> getFiles(int targetId) {
        return submissionRepository.findFilesByTarget(targetId);
    }

    public void saveSubmittedFile(SubmissionFile file) {
        if (file == null) {
            throw new RuntimeException("Thông tin file không hợp lệ");
        }
        submissionRepository.upsertFile(file);
    }

    public void saveSubmittedFile(int targetId, SubmissionRequirement requirement, File sourceFile, int studentId) {
        if (targetId <= 0 || requirement == null || sourceFile == null || studentId <= 0) {
            throw new RuntimeException("Thông tin file không hợp lệ");
        }
        if (!sourceFile.isFile()) {
            throw new RuntimeException("File đã chọn không tồn tại");
        }

        String originalName = sourceFile.getName();
        String requiredExtension = requirement.getRequiredExtension();
        if (!hasRequiredExtension(originalName, requiredExtension)) {
            throw new RuntimeException("File " + requirement.getRequirementName()
                    + " phải có định dạng " + requiredExtension);
        }

        try {
            Path targetDir = SUBMISSION_ROOT
                    .resolve("request_" + requirement.getRequestId())
                    .resolve("target_" + targetId);
            Files.createDirectories(targetDir);

            String storedFileName = targetId + "_" + requirement.getRequirementId() + "_"
                    + LocalDateTime.now().format(FILE_TIME_FORMATTER) + "_" + sanitizeFileName(originalName);
            Path storedPath = targetDir.resolve(storedFileName);
            Files.copy(sourceFile.toPath(), storedPath, StandardCopyOption.REPLACE_EXISTING);

            SubmissionFile file = new SubmissionFile();
            file.setTargetId(targetId);
            file.setRequirementId(requirement.getRequirementId());
            file.setOriginalFileName(originalName);
            file.setStoredFileName(storedFileName);
            file.setFilePath(storedPath.toString());
            file.setFileSize(Files.size(storedPath));
            file.setUploadedByStudentId(studentId);
            submissionRepository.upsertFile(file);
        } catch (IOException e) {
            throw new RuntimeException("Không thể lưu file nộp: " + e.getMessage(), e);
        }
    }

    public void markSubmitted(int targetId) {
        submissionRepository.markTargetSubmitted(targetId);
    }

    public void completeSubmission(int targetId, int requestId) {
        List<SubmissionRequirement> requirements = submissionRepository.findRequirementsByRequest(requestId);
        List<SubmissionFile> files = submissionRepository.findFilesByTarget(targetId);
        Set<Integer> uploadedRequirementIds = new HashSet<>();
        for (SubmissionFile file : files) {
            uploadedRequirementIds.add(file.getRequirementId());
        }
        for (SubmissionRequirement requirement : requirements) {
            if (requirement.isRequired() && !uploadedRequirementIds.contains(requirement.getRequirementId())) {
                throw new RuntimeException("Chưa nộp đủ file. Thiếu: " + requirement.getRequirementName());
            }
        }
        submissionRepository.markTargetSubmitted(targetId);
    }

    public int downloadFilesForTarget(SubmissionTarget target, Path destinationRoot) {
        if (target == null || destinationRoot == null) {
            throw new RuntimeException("Vui lòng chọn nhóm và thư mục tải file");
        }
        List<SubmissionFile> files = submissionRepository.findFilesByTarget(target.getTargetId());
        if (files.isEmpty()) {
            throw new RuntimeException("Nhóm này chưa nộp file nào");
        }

        String requestFolder = "request_" + target.getRequestId() + "_"
                + sanitizeFileName(target.getRequestTitle());
        String groupFolder = "group_" + target.getGroupId() + "_"
                + sanitizeFileName(target.getGroupName());
        Path outputDir = destinationRoot.resolve(requestFolder).resolve(groupFolder);

        try {
            Files.createDirectories(outputDir);
            int copied = 0;
            for (SubmissionFile file : files) {
                Path source = Path.of(file.getFilePath());
                if (!Files.exists(source)) {
                    throw new RuntimeException("Không tìm thấy file đã lưu: " + file.getOriginalFileName());
                }
                String outputName = sanitizeFileName(file.getRequirementName()) + "_"
                        + sanitizeFileName(file.getOriginalFileName());
                Files.copy(source, outputDir.resolve(outputName), StandardCopyOption.REPLACE_EXISTING);
                copied++;
            }
            return copied;
        } catch (IOException e) {
            throw new RuntimeException("Không thể tải file về máy: " + e.getMessage(), e);
        }
    }

    private void sendRequestMessage(int staffId, int leaderStudentId, String title, LocalDateTime deadline) {
        String content = REQUEST_MESSAGE_PREFIX
                + "Bạn có yêu cầu nộp đồ án mới: " + title
                + ". Hạn nộp: " + deadline;
        messageRepository.createMessage(staffId, leaderStudentId, null, content);
    }

    private boolean hasRequiredExtension(String fileName, String requiredExtension) {
        if (fileName == null || requiredExtension == null || requiredExtension.isBlank()) {
            return false;
        }
        return fileName.toLowerCase(Locale.ROOT).endsWith(requiredExtension.toLowerCase(Locale.ROOT));
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "submission";
        }
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
