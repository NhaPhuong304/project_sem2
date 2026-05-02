package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.MemberStatus;
import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectGroup;
import com.aptech.projectmgmt.model.Staff;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.repository.GroupRepository;
import com.aptech.projectmgmt.repository.ProjectRepository;
import com.aptech.projectmgmt.repository.StaffRepository;
import com.aptech.projectmgmt.repository.StudentRepository;

import java.util.List;

public class GroupService {

	private final GroupRepository groupRepository = new GroupRepository();
	private final ProjectRepository projectRepository = new ProjectRepository();
	private final StudentRepository studentRepository = new StudentRepository();
	private final StaffRepository staffRepository = new StaffRepository();
	private final MailService mailService = new MailService();

	public List<ProjectGroup> getGroupsByProject(int projectId) {
		return groupRepository.findByProjectId(projectId);
	}
	public void deleteLeaderAndTransfer(int oldLeaderId, int newLeaderId){
		groupRepository.deleteLeaderAndTransfer(oldLeaderId, newLeaderId);
	}

	public List<GroupMember> getMembersByGroup(int groupId) {
		return groupRepository.findMembersByGroupId(groupId);
	}

	public List<GroupMember> getActiveMembersByGroup(int groupId) {
		return groupRepository.findMembersByGroupId(groupId).stream()
				.filter(member -> member.getStatus() == MemberStatus.ACTIVE).toList();
	}

	public int countActiveMembers(int groupId) {
		return groupRepository.countActiveMembers(groupId);
	}

	public GroupMember getMemberByStudentAndGroup(int studentId, int groupId) {
		return groupRepository.findMemberByStudentAndGroup(studentId, groupId);
	}

	public void createGroup(int projectId, String groupName) {
		String normalizedName = groupName != null ? groupName.trim() : "";
		if (normalizedName.isEmpty()) {
			throw new RuntimeException("Group name must not be empty");
		}
		Project project = projectRepository.findById(projectId);
		if (project == null) {
			throw new RuntimeException("Project could not be found");
		}
		if (project.getGroupId() > 0) {
			throw new RuntimeException("Each project can only be assigned to one group");
		}
		if (groupRepository.existsGroupName(project.getClassId(), normalizedName)) {
			throw new RuntimeException("This group name already exists in the class");
		}
		groupRepository.createStandaloneGroup(project.getClassId(), normalizedName);
	}

	public List<Student> getAvailableStudentsForClass(int classId) {
		return groupRepository.findAvailableStudentsForClass(classId);
	}

	public boolean hasActiveLeader(int groupId) {
		return groupRepository.hasActiveLeader(groupId);
	}

	public void addMemberToGroup(int groupId, int studentId, MemberRole role) {
		ProjectGroup group = groupRepository.findById(groupId);
		if (group == null) {
			throw new RuntimeException("Group could not be found");
		}

		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Student could not be found");
		}

		if (student.getClassId() != group.getClassId()) {
			throw new RuntimeException("The student does not belong to this group's class");
		}

		if (group.getProjectId() > 0 && groupRepository.existsStudentInProject(group.getProjectId(), studentId)) {
			throw new RuntimeException("This student already belongs to an active project");
		}

		int activeMemberCount = groupRepository.countActiveMembers(groupId);
		boolean hasLeader = groupRepository.hasActiveLeader(groupId);

		if (activeMemberCount == 0 && role != MemberRole.LEADER) {
			throw new RuntimeException("The first student in the group must be the group leader");
		}

		if (hasLeader && role == MemberRole.LEADER) {
			throw new RuntimeException("Each group can only have one leader");
		}

		groupRepository.addMember(groupId, studentId, role);
	}

	public List<String> getMemberNamesByGroupId(int groupId) {
		return groupRepository.getMemberNamesByGroupId(groupId);
	}

	public void reactivateMember(int memberId, int staffId) {
		GroupMember member = groupRepository.findMemberById(memberId);
		if (member == null) {
			throw new RuntimeException("Member could not be found");
		}

		groupRepository.reactivateMember(memberId);
		notifyMemberReactivated(member, staffId);
	}

	public void changeLeader(int groupId, int newLeaderMemberId, int staffId) {
		GroupMember newLeader = groupRepository.findMemberById(newLeaderMemberId);
		if (newLeader == null) {
			throw new RuntimeException("Member could not be found duoc chon");
		}
		if (newLeader.getGroupId() != groupId) {
			throw new RuntimeException("The member does not belong to this group");
		}
		if (newLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Only active members can be selected");
		}
		if (newLeader.getRole() == MemberRole.LEADER) {
			throw new RuntimeException("Member nay da la leader");
		}

		GroupMember oldLeader = groupRepository.findMembersByGroupId(groupId).stream()
				.filter(m -> m.getStatus() == MemberStatus.ACTIVE && m.getRole() == MemberRole.LEADER).findFirst()
				.orElseThrow(() -> new RuntimeException("The current leader could not be found"));

		groupRepository.transferLeader(oldLeader.getMemberId(), newLeaderMemberId);

		notifyLeaderChangedOldLeader(oldLeader, newLeader, staffId);
		notifyLeaderChangedNewLeader(oldLeader, newLeader, staffId);
	}

	private void notifyLeaderChangedOldLeader(GroupMember oldLeader, GroupMember newLeader, int staffId) {
		Student oldLeaderStudent = studentRepository.findById(oldLeader.getStudentId());
		Student newLeaderStudent = studentRepository.findById(newLeader.getStudentId());
		Staff staff = staffRepository.findById(staffId);

		if (oldLeaderStudent == null || staff == null || oldLeaderStudent.getEmail() == null
				|| oldLeaderStudent.getEmail().isBlank()) {
			return;
		}

		String subject = "[Aptech] Group leader change notification";
		String body = "Hello " + oldLeaderStudent.getFullName() + ",\n\n"
				+ "You no longer hold the group leader role.\n" + "Performed by: " + staff.getFullName() + "\n"
				+ "New leader: " + (newLeaderStudent != null ? newLeaderStudent.getFullName() : "") + "\n\n"
				+ "Your role has been changed to member.";

		mailService.sendEmailQuietly(oldLeaderStudent.getEmail(), subject, body);
	}

	private void notifyLeaderChangedNewLeader(GroupMember oldLeader, GroupMember newLeader, int staffId) {
		Student oldLeaderStudent = studentRepository.findById(oldLeader.getStudentId());
		Student newLeaderStudent = studentRepository.findById(newLeader.getStudentId());
		Staff staff = staffRepository.findById(staffId);

		if (newLeaderStudent == null || staff == null || newLeaderStudent.getEmail() == null
				|| newLeaderStudent.getEmail().isBlank()) {
			return;
		}

		String subject = "[Aptech] Group leader assignment notification";
		String body = "Hello " + newLeaderStudent.getFullName() + ",\n\n"
				+ "You have been assigned as the group leader.\n" + "Performed by: " + staff.getFullName() + "\n"
				+ "Previous leader: " + (oldLeaderStudent != null ? oldLeaderStudent.getFullName() : "") + "\n\n"
				+ "Please sign in to the system to track and manage the group's tasks.";

		mailService.sendEmailQuietly(newLeaderStudent.getEmail(), subject, body);
	}

	public List<GroupMember> getActiveMembersExcept(int groupId, int excludeMemberId) {
		return groupRepository.findActiveMembersExcept(groupId, excludeMemberId);
	}

	private void notifyMemberReactivated(GroupMember member, int staffId) {
		Student student = studentRepository.findById(member.getStudentId());
		Staff staff = staffRepository.findById(staffId);

		if (student == null || staff == null || student.getEmail() == null || student.getEmail().isBlank()) {
			return;
		}

		String subject = "[Aptech] Group participation reactivation notification";
		String body = "Hello " + student.getFullName() + ",\n\n" + "Your participation in the group has been reactivated.\n"
				+ "Performed by: " + staff.getFullName() + "\n\n"
				+ "Please sign in to continue tracking tasks and notifications.";

		mailService.sendEmailQuietly(student.getEmail(), subject, body);
	}

	public void excludeLeaderAndTransfer(int oldLeaderMemberId, int newLeaderMemberId, int staffId, String reason) {
		GroupMember oldLeader = groupRepository.findMemberById(oldLeaderMemberId);
		if (oldLeader == null) {
			throw new RuntimeException("The leader to be revoked could not be found");
		}
		if (oldLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("This leader is no longer active");
		}
		if (oldLeader.getRole() != MemberRole.LEADER) {
			throw new RuntimeException("This member is not a leader");
		}

		GroupMember newLeader = groupRepository.findMemberById(newLeaderMemberId);
		if (newLeader == null) {
			throw new RuntimeException("Member could not be found thay the");
		}
		if (newLeader.getGroupId() != oldLeader.getGroupId()) {
			throw new RuntimeException("The new leader does not belong to the same group");
		}
		if (newLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Only active members can be selected");
		}
		if (newLeader.getMemberId() == oldLeaderMemberId) {
			throw new RuntimeException("You cannot reselect the current leader");
		}

		String normalizedReason = reason != null ? reason.trim() : "";
		if (normalizedReason.isEmpty()) {
			throw new RuntimeException("Vui long nhap ly do huy quyen tham gia");
		}

		groupRepository.transferLeaderAndExclude(oldLeaderMemberId, newLeaderMemberId, staffId, normalizedReason);
		notifyMemberExcluded(oldLeader, staffId, normalizedReason);
	}

	private void notifyMemberExcluded(GroupMember member, int staffId, String reason) {
		Student student = studentRepository.findById(member.getStudentId());
		Staff staff = staffRepository.findById(staffId);

		if (student == null || staff == null || student.getEmail() == null || student.getEmail().isBlank()) {
			return;
		}

		String subject = "[Aptech] Group participation revocation notification";
		String body = "Hello " + student.getFullName() + ",\n\n" + "Your participation in the group has been revoked.\n"
				+ "Performed by: " + staff.getFullName() + "\n" + "Ly do: " + reason + "\n\n"
				+ "Please contact the staff office or supervising teacher if you need more information.";

		mailService.sendEmailQuietly(student.getEmail(), subject, body);
	}

	public void renameGroup(int groupId, String newGroupName) {
		ProjectGroup group = groupRepository.findById(groupId);
		if (group == null) {
			throw new RuntimeException("Group could not be found");
		}
		String normalizedName = newGroupName != null ? newGroupName.trim() : "";
		if (normalizedName.isEmpty()) {
			throw new RuntimeException("Group name must not be empty");
		}
		if (groupRepository.existsGroupNameExcluding(group.getClassId(), groupId, normalizedName)) {
			throw new RuntimeException("This group name already exists in the class");
		}
		groupRepository.updateGroupName(groupId, normalizedName);
	}

	public void excludeMember(int memberId, int staffId, String reason) {
		GroupMember member = groupRepository.findMemberById(memberId);
		if (member == null) {
			throw new RuntimeException("Member could not be found");
		}
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Member nay da bi loai");
		}
		if (groupRepository.countActiveMembers(member.getGroupId()) <= 2) {
			throw new RuntimeException("Each group must have at least 2 active students");
		}

		String normalizedReason = reason != null ? reason.trim() : "";
		if (normalizedReason.isEmpty()) {
			throw new RuntimeException("Vui long nhap ly do huy quyen tham gia");
		}

		groupRepository.excludeMember(memberId, staffId, normalizedReason);
		notifyMemberExcluded(member, staffId, normalizedReason);
	}

	public void removeMember(int memberId) {
		GroupMember member = groupRepository.findMemberById(memberId);
		if (member == null) {
			throw new RuntimeException("Member could not be found");
		}
		groupRepository.removeMember(memberId);
	}
}
