package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectStatus;
import com.aptech.projectmgmt.repository.GroupRepository;
import com.aptech.projectmgmt.repository.ProjectRepository;
import com.aptech.projectmgmt.util.SessionManager;

import java.time.LocalDate;
import java.util.List;

public class ProjectService {

    private final ProjectRepository projectRepository = new ProjectRepository();
    private final GroupRepository groupRepository = new GroupRepository();

    public List<Project> getProjectsByClass(int classId) {
        return projectRepository.findByClassId(classId);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> getProjectsByAdvisor(int staffId) {
        return projectRepository.findByAdvisorId(staffId);
    }

    public List<Project> getProjectsByStudent(int studentId) {
        return projectRepository.findByStudentId(studentId);
    }

    public Project getProjectById(int projectId) {
        return projectRepository.findById(projectId);
    }

    public Project getProjectByAdvisor(int projectId, int staffId) {
        return projectRepository.findByIdAndAdvisorId(projectId, staffId);
    }

    public void createProject(Project project) {
        if (project.getProjectName() == null || project.getProjectName().trim().isEmpty()) {
            throw new RuntimeException("Ten project khong duoc de trong");
        }
        if (project.getSemester() == null || project.getSemester().trim().isEmpty()) {
            throw new RuntimeException("Hoc ky khong duoc de trong");
        }
        if (project.getClassId() <= 0) {
            throw new RuntimeException("Project phai gan voi mot lop hop le");
        }
        if (project.getSupervisorId() == null) {
            throw new RuntimeException("Vui long chon giao vien huong dan");
        }
        if (project.getStartDate() == null) {
            throw new RuntimeException("Vui long chon ngay bat dau");
        }
        if (project.getEndDate() == null) {
            throw new RuntimeException("Vui long chon ngay ket thuc");
        }
        if (project.getReportDate() == null) {
            throw new RuntimeException("Vui long chon ngay bao cao");
        }
        if (project.getEndDate() != null && project.getStartDate() != null
                && project.getEndDate().isBefore(project.getStartDate())) {
            throw new RuntimeException("Ngay ket thuc phai sau ngay bat dau");
        }
        if (project.getReportDate() != null && project.getEndDate() != null
                && project.getReportDate().isBefore(project.getEndDate())) {
            throw new RuntimeException("Ngay bao cao phai sau hoac bang ngay ket thuc");
        }
        if (project.getStatus() == null) project.setStatus(ProjectStatus.ACTIVE);
        if (project.getCreatedByStaffId() == null) {
            var currentStaff = SessionManager.getInstance().getCurrentStaff();
            if (currentStaff != null) {
                project.setCreatedByStaffId(currentStaff.getStaffId());
            }
        }

        String defaultGroupName = "Nhom - " + project.getProjectName().trim();
        if (groupRepository.existsGroupName(project.getClassId(), defaultGroupName)) {
            defaultGroupName = defaultGroupName + " - " + System.currentTimeMillis();
        }
        int groupId = groupRepository.createStandaloneGroup(project.getClassId(), defaultGroupName);
        project.setGroupId(groupId);

        int projectId = projectRepository.create(project);
        project.setProjectId(projectId);
    }

    public void updateProject(Project project) {
        if (project.getProjectName() == null || project.getProjectName().trim().isEmpty()) {
            throw new RuntimeException("Ten project khong duoc de trong");
        }
        if (project.getSemester() == null || project.getSemester().trim().isEmpty()) {
            throw new RuntimeException("Hoc ky khong duoc de trong");
        }
        if (project.getStartDate() == null || project.getEndDate() == null || project.getReportDate() == null) {
            throw new RuntimeException("Vui long nhap day du ngay bat dau, ket thuc va bao cao");
        }
        if (project.getEndDate() != null && project.getStartDate() != null
                && project.getEndDate().isBefore(project.getStartDate())) {
            throw new RuntimeException("Ngay ket thuc phai sau ngay bat dau");
        }
        if (project.getReportDate() != null && project.getEndDate() != null
                && project.getReportDate().isBefore(project.getEndDate())) {
            throw new RuntimeException("Ngay bao cao phai sau hoac bang ngay ket thuc");
        }
        projectRepository.update(project);
    }

    public boolean canMarkCompleted(Project project) {
        return project != null
                && project.getStatus() == ProjectStatus.ACTIVE
                && project.getReportDate() != null
                && project.getReportDate().isBefore(LocalDate.now());
    }

    public void markProjectCompleted(int projectId) {
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new RuntimeException("Khong tim thay project");
        }
        if (!canMarkCompleted(project)) {
            throw new RuntimeException("Chi duoc danh dau hoan thanh khi project da qua han bao cao");
        }
        projectRepository.markCompleted(projectId);
    }
}
