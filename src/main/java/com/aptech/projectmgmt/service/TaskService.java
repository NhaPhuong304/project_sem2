package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.*;
import com.aptech.projectmgmt.repository.GroupRepository;
import com.aptech.projectmgmt.repository.MessageRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import com.aptech.projectmgmt.repository.StudentRepository;
import com.aptech.projectmgmt.repository.TaskRepository;
import com.aptech.projectmgmt.util.NotificationUtil;
import com.aptech.projectmgmt.util.SessionManager;

import java.time.LocalDateTime;
import java.util.List;

public class TaskService {

    private final TaskRepository taskRepository = new TaskRepository();
    private final GroupRepository groupRepository = new GroupRepository();
    private final MessageRepository messageRepository = new MessageRepository();
    private final StudentRepository studentRepository = new StudentRepository();
    private final StaffRepository staffRepository = new StaffRepository();
    private final MailService mailService = new MailService();

    public List<TaskViewModel> getTasksByGroup(int groupId) {
        return taskRepository.findByGroupId(groupId);
    }

    public void resetOverdueTasks() {
        taskRepository.resetOverdueTasks(LocalDateTime.now());
    }

    public void startTask(int taskId, int studentId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) throw new RuntimeException("Khong tim thay task");
        requireActiveMember(studentId, task.getGroupId());
        if (task.getStatus() != TaskStatus.PENDING) {
            throw new IllegalStateException("Task khong o trang thai PENDING");
        }
        if (task.getAssignedTo() == null || task.getAssignedTo() != studentId) {
            throw new RuntimeException("Ban khong duoc phan cong task nay");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = task.getEstimatedStartDate();
        if (start == null || now.isBefore(start)) {
            throw new RuntimeException("Chua den gio bat dau task");
        }
        if (now.isAfter(start.plusHours(1))) {
            throw new RuntimeException("Da qua thoi gian bat dau task (1 gio)");
        }
        taskRepository.updateStatus(taskId, TaskStatus.IN_PROGRESS.getValue());
        taskRepository.setActualStartDate(taskId, now);
        taskRepository.insertStatusHistory(taskId, TaskStatus.PENDING.getValue(), TaskStatus.IN_PROGRESS.getValue(), getCurrentAccountId(), now);
    }

    public void submitForReview(int taskId, int studentId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) throw new RuntimeException("Khong tim thay task");
        requireActiveMember(studentId, task.getGroupId());
        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new IllegalStateException("Task khong o trang thai IN_PROGRESS");
        }
        if (task.getAssignedTo() == null || task.getAssignedTo() != studentId) {
            throw new RuntimeException("Ban khong duoc phan cong task nay");
        }
        LocalDateTime now = LocalDateTime.now();
        taskRepository.updateStatus(taskId, TaskStatus.REVIEWING.getValue());
        taskRepository.insertStatusHistory(taskId, TaskStatus.IN_PROGRESS.getValue(), TaskStatus.REVIEWING.getValue(), getCurrentAccountId(), now);
    }

    public void requestRevision(int taskId, int studentId, String note) {
        Task task = taskRepository.findById(taskId);
        if (task == null) throw new RuntimeException("Khong tim thay task");
        requireActiveMember(studentId, task.getGroupId());
        if (task.getStatus() != TaskStatus.REVIEWING) {
            throw new IllegalStateException("Task khong o trang thai REVIEWING");
        }
        if (task.getReviewedBy() == null || task.getReviewedBy() != studentId) {
            throw new RuntimeException("Ban khong phai nguoi kiem tra task nay");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new RuntimeException("Noi dung yeu cau chinh sua khong duoc de trong");
        }
        LocalDateTime now = LocalDateTime.now();
        taskRepository.updateStatus(taskId, TaskStatus.REVISING.getValue());
        taskRepository.insertRevision(taskId, note, studentId, now);
        taskRepository.insertStatusHistory(taskId, TaskStatus.REVIEWING.getValue(), TaskStatus.REVISING.getValue(), getCurrentAccountId(), now);
        notifyRevisionRequested(task, studentId, note.trim());
    }

    public void confirmCompleted(int taskId, int studentId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) throw new RuntimeException("Khong tim thay task");
        requireActiveMember(studentId, task.getGroupId());
        if (task.getStatus() != TaskStatus.REVIEWING) {
            throw new IllegalStateException("Task khong o trang thai REVIEWING");
        }
        if (task.getReviewedBy() == null || task.getReviewedBy() != studentId) {
            throw new RuntimeException("Ban khong phai nguoi kiem tra task nay");
        }
        LocalDateTime now = LocalDateTime.now();
        taskRepository.updateStatus(taskId, TaskStatus.COMPLETED.getValue());
        taskRepository.setActualEndDate(taskId, now);
        taskRepository.insertStatusHistory(taskId, TaskStatus.REVIEWING.getValue(), TaskStatus.COMPLETED.getValue(), getCurrentAccountId(), now);
    }

    public void submitRevised(int taskId, int studentId) {
        Task task = taskRepository.findById(taskId);
        if (task == null) throw new RuntimeException("Khong tim thay task");
        requireActiveMember(studentId, task.getGroupId());
        if (task.getStatus() != TaskStatus.REVISING) {
            throw new IllegalStateException("Task khong o trang thai REVISING");
        }
        if (task.getAssignedTo() == null || task.getAssignedTo() != studentId) {
            throw new RuntimeException("Ban khong duoc phan cong task nay");
        }
        LocalDateTime now = LocalDateTime.now();
        taskRepository.updateStatus(taskId, TaskStatus.REVIEWING.getValue());
        taskRepository.insertStatusHistory(taskId, TaskStatus.REVISING.getValue(), TaskStatus.REVIEWING.getValue(), getCurrentAccountId(), now);
    }

    public void createTask(Task task) {
        requireActiveLeader(task.getCreatedBy(), task.getGroupId());
        if (groupRepository.countActiveMembers(task.getGroupId()) < 2) {
            throw new RuntimeException("Nhom phai co toi thieu 2 sinh vien dang hoat dong moi duoc giao task");
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new RuntimeException("Tieu de task khong duoc de trong");
        }
        if (task.getEstimatedStartDate() != null && task.getEstimatedEndDate() != null
                && task.getEstimatedEndDate().isBefore(task.getEstimatedStartDate())) {
            throw new RuntimeException("Ngay ket thuc phai sau ngay bat dau");
        }
        if (task.getEstimatedStartDate() != null
                && task.getEstimatedStartDate().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new RuntimeException("Ngay gio bat dau da qua hon 1 gio. Task se bi he thong tu dong bo phan cong.");
        }
        if (task.getAssignedTo() != null && task.getReviewedBy() != null
                && task.getAssignedTo().equals(task.getReviewedBy())) {
            throw new RuntimeException("Nguoi thuc hien va nguoi kiem tra phai khac nhau");
        }
        task.setStatus(TaskStatus.PENDING);
        int taskId = taskRepository.create(task);
        taskRepository.insertStatusHistory(taskId, TaskStatus.PENDING.getValue(), TaskStatus.PENDING.getValue(), getCurrentAccountId(), LocalDateTime.now());
    }

    public void sendReminderMessage(int staffId, int studentId, int taskId, String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Noi dung tin nhan khong duoc de trong");
        }
        LocalDateTime now = LocalDateTime.now();
        Message msg = new Message();
        msg.setSenderId(staffId);
        msg.setReceiverId(studentId);
        msg.setTaskId(taskId);
        msg.setContent(NotificationUtil.buildReminderContent(content.trim()));
        messageRepository.insert(msg, now);

        Task task = taskRepository.findById(taskId);
        Staff staff = staffRepository.findById(staffId);
        Student student = studentRepository.findById(studentId);
        if (task != null && staff != null && student != null && student.getEmail() != null && !student.getEmail().isBlank()) {
            String subject = "[Aptech] Nhac nho thuc hien task";
            String body = "Xin chao " + student.getFullName() + ",\n\n" +
                    staff.getFullName() + " vua gui nhac nho cho task: " + task.getTitle() + ".\n" +
                    "Noi dung: " + content.trim() + "\n\n" +
                    "Vui long vao he thong de kiem tra va xu ly task.";
            mailService.sendEmailQuietly(student.getEmail(), subject, body);
        }
    }

    public List<TaskStatusHistory> getStatusHistory(int taskId) {
        return taskRepository.getStatusHistory(taskId);
    }

    public List<TaskRevision> getRevisions(int taskId) {
        return taskRepository.getRevisions(taskId);
    }

    public List<TaskAbandonLog> getAbandonLogs(int taskId) {
        return taskRepository.getAbandonLogs(taskId);
    }

    private int getCurrentAccountId() {
        var account = SessionManager.getInstance().getCurrentAccount();
        if (account == null) {
            throw new RuntimeException("Khong xac dinh duoc tai khoan dang dang nhap");
        }
        return account.getAccountId();
    }

    private void requireActiveMember(int studentId, int groupId) {
        GroupMember member = groupRepository.findMemberByStudentAndGroup(studentId, groupId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new RuntimeException("Ban khong con tham gia nhom nay");
        }
    }

    private void requireActiveLeader(int studentId, int groupId) {
        GroupMember member = groupRepository.findMemberByStudentAndGroup(studentId, groupId);
        if (member == null || member.getStatus() != MemberStatus.ACTIVE) {
            throw new RuntimeException("Ban khong con tham gia nhom nay");
        }
        if (member.getRole() != MemberRole.LEADER) {
            throw new RuntimeException("Chi truong nhom moi duoc tao task");
        }
    }

    private void notifyRevisionRequested(Task task, int reviewerStudentId, String note) {
        if (task.getAssignedTo() == null) {
            return;
        }
        Student assignee = studentRepository.findById(task.getAssignedTo());
        Student reviewer = studentRepository.findById(reviewerStudentId);
        if (assignee == null || reviewer == null || assignee.getEmail() == null || assignee.getEmail().isBlank()) {
            return;
        }
        String subject = "[Aptech] Yeu cau chinh sua task";
        String body = "Xin chao " + assignee.getFullName() + ",\n\n" +
                reviewer.getFullName() + " vua gui yeu cau chinh sua cho task: " + task.getTitle() + ".\n" +
                "Noi dung yeu cau: " + note + "\n\n" +
                "Vui long vao he thong de xem chi tiet va gui lai task sau khi chinh sua.";
        mailService.sendEmailQuietly(assignee.getEmail(), subject, body);
    }
}
