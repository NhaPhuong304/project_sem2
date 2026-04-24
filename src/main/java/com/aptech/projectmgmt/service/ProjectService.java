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

	public List<Project> getProjectsByCreatedStaff(int staffId) {
		return projectRepository.findByCreatedByStaffId(staffId);
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
			throw new RuntimeException("Project name must not be empty");
		}
		if (project.getSemester() == null || project.getSemester().trim().isEmpty()) {
			throw new RuntimeException("Semester must not be empty");
		}
		if (project.getClassId() <= 0) {
			throw new RuntimeException("A project must be assigned to a valid class");
		}
		if (project.getSupervisorId() == null) {
			throw new RuntimeException("Please select a supervising teacher");
		}
		if (project.getStartDate() == null) {
			throw new RuntimeException("Please select a start date");
		}
		if (project.getEndDate() == null) {
			throw new RuntimeException("Please select an end date");
		}
		if (project.getReportDate() == null) {
			throw new RuntimeException("Please select a report date");
		}
		if (project.getEndDate() != null && project.getStartDate() != null
				&& project.getEndDate().isBefore(project.getStartDate())) {
			throw new RuntimeException("The end date must be after the start date");
		}
		if (project.getReportDate() != null && project.getEndDate() != null) {
			if (project.getReportDate().isAfter(project.getEndDate())) {
				throw new RuntimeException("The report date must not be later than the end date");
			}
			if (project.getReportDate().isBefore(project.getEndDate().minusDays(3))) {
				throw new RuntimeException("The report date must be within 3 days before the end date");
			}
		}
		if (project.getStatus() == null)
			project.setStatus(ProjectStatus.ACTIVE);
		if (project.getCreatedByStaffId() == null) {
			var currentStaff = SessionManager.getInstance().getCurrentStaff();
			if (currentStaff != null) {
				project.setCreatedByStaffId(currentStaff.getStaffId());
			}
		}

		String defaultGroupName = "Group - " + project.getProjectName().trim();
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
			throw new RuntimeException("Project name must not be empty");
		}
		if (project.getSemester() == null || project.getSemester().trim().isEmpty()) {
			throw new RuntimeException("Semester must not be empty");
		}
		if (project.getStartDate() == null || project.getEndDate() == null || project.getReportDate() == null) {
			throw new RuntimeException("Please enter the start, end, and report dates completely");
		}
		if (project.getEndDate() != null && project.getStartDate() != null
				&& project.getEndDate().isBefore(project.getStartDate())) {
			throw new RuntimeException("The end date must be after the start date");
		}
		if (project.getReportDate() != null && project.getEndDate() != null) {
			if (project.getReportDate().isAfter(project.getEndDate())) {
				throw new RuntimeException("The report date must not be later than the end date");
			}
			if (project.getReportDate().isBefore(project.getEndDate().minusDays(3))) {
				throw new RuntimeException("The report date must be within 3 days before the end date");
			}
		}
		projectRepository.update(project);
	}

	public boolean canMarkCompleted(Project project) {
		return project != null && project.getStatus() == ProjectStatus.ACTIVE && project.getReportDate() != null
				&& project.getReportDate().isBefore(LocalDate.now());
	}

	public void markProjectCompleted(int projectId) {
		Project project = projectRepository.findById(projectId);
		if (project == null) {
			throw new RuntimeException("Project could not be found");
		}
		if (!canMarkCompleted(project)) {
			throw new RuntimeException("Chi duoc danh dau hoan thanh khi project da qua han bao cao");
		}
		projectRepository.markCompleted(projectId);
	}
}
