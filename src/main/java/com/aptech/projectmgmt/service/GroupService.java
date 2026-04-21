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
			throw new RuntimeException("Ten nhom khong duoc de trong");
		}
		Project project = projectRepository.findById(projectId);
		if (project == null) {
			throw new RuntimeException("Khong tim thay project");
		}
		if (project.getGroupId() > 0) {
			throw new RuntimeException("Moi project chi duoc gan 1 nhom");
		}
		if (groupRepository.existsGroupName(project.getClassId(), normalizedName)) {
			throw new RuntimeException("Ten nhom da ton tai trong lop nay");
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
			throw new RuntimeException("Khong tim thay nhom");
		}

		Student student = studentRepository.findById(studentId);
		if (student == null) {
			throw new RuntimeException("Khong tim thay sinh vien");
		}

		if (student.getClassId() != group.getClassId()) {
			throw new RuntimeException("Sinh vien khong thuoc lop cua nhom nay");
		}

		if (group.getProjectId() > 0 && groupRepository.existsStudentInProject(group.getProjectId(), studentId)) {
			throw new RuntimeException("Sinh vien nay da thuoc mot project dang hoat dong");
		}

		int activeMemberCount = groupRepository.countActiveMembers(groupId);
		boolean hasLeader = groupRepository.hasActiveLeader(groupId);

		if (activeMemberCount == 0 && role != MemberRole.LEADER) {
			throw new RuntimeException("Sinh vien dau tien cua nhom bat buoc phai la Truong nhom");
		}

		if (hasLeader && role == MemberRole.LEADER) {
			throw new RuntimeException("Moi nhom chi duoc co 1 Truong nhom");
		}

		groupRepository.addMember(groupId, studentId, role);
	}

	public List<String> getMemberNamesByGroupId(int groupId) {
		return groupRepository.getMemberNamesByGroupId(groupId);
	}

	public void reactivateMember(int memberId, int staffId) {
		GroupMember member = groupRepository.findMemberById(memberId);
		if (member == null) {
			throw new RuntimeException("Khong tim thay thanh vien");
		}

		groupRepository.reactivateMember(memberId);
		notifyMemberReactivated(member, staffId);
	}

	public void changeLeader(int groupId, int newLeaderMemberId, int staffId) {
		GroupMember newLeader = groupRepository.findMemberById(newLeaderMemberId);
		if (newLeader == null) {
			throw new RuntimeException("Khong tim thay thanh vien duoc chon");
		}
		if (newLeader.getGroupId() != groupId) {
			throw new RuntimeException("Thanh vien khong thuoc nhom nay");
		}
		if (newLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Chi duoc chon thanh vien dang hoat dong");
		}
		if (newLeader.getRole() == MemberRole.LEADER) {
			throw new RuntimeException("Thanh vien nay da la leader");
		}

		GroupMember oldLeader = groupRepository.findMembersByGroupId(groupId).stream()
				.filter(m -> m.getStatus() == MemberStatus.ACTIVE && m.getRole() == MemberRole.LEADER).findFirst()
				.orElseThrow(() -> new RuntimeException("Khong tim thay leader hien tai"));

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

		String subject = "[Aptech] Thong bao thay doi leader nhom";
		String body = "Xin chao " + oldLeaderStudent.getFullName() + ",\n\n"
				+ "Ban khong con giu vai tro truong nhom.\n" + "Nguoi thuc hien: " + staff.getFullName() + "\n"
				+ "Leader moi: " + (newLeaderStudent != null ? newLeaderStudent.getFullName() : "") + "\n\n"
				+ "Vai tro cua ban da duoc chuyen thanh thanh vien.";

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

		String subject = "[Aptech] Thong bao duoc phan quyen truong nhom";
		String body = "Xin chao " + newLeaderStudent.getFullName() + ",\n\n"
				+ "Ban da duoc phan quyen lam truong nhom.\n" + "Nguoi thuc hien: " + staff.getFullName() + "\n"
				+ "Leader cu: " + (oldLeaderStudent != null ? oldLeaderStudent.getFullName() : "") + "\n\n"
				+ "Vui long dang nhap he thong de theo doi va quan ly task cua nhom.";

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

		String subject = "[Aptech] Thong bao kich hoat lai tham gia nhom";
		String body = "Xin chao " + student.getFullName() + ",\n\n" + "Ban da duoc kich hoat lai tham gia nhom.\n"
				+ "Nguoi thuc hien: " + staff.getFullName() + "\n\n"
				+ "Vui long dang nhap he thong de tiep tuc theo doi cong viec va thong bao moi.";

		mailService.sendEmailQuietly(student.getEmail(), subject, body);
	}

	public void excludeLeaderAndTransfer(int oldLeaderMemberId, int newLeaderMemberId, int staffId, String reason) {
		GroupMember oldLeader = groupRepository.findMemberById(oldLeaderMemberId);
		if (oldLeader == null) {
			throw new RuntimeException("Khong tim thay leader can huy quyen");
		}
		if (oldLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Leader nay khong con hoat dong");
		}
		if (oldLeader.getRole() != MemberRole.LEADER) {
			throw new RuntimeException("Thanh vien nay khong phai leader");
		}

		GroupMember newLeader = groupRepository.findMemberById(newLeaderMemberId);
		if (newLeader == null) {
			throw new RuntimeException("Khong tim thay thanh vien thay the");
		}
		if (newLeader.getGroupId() != oldLeader.getGroupId()) {
			throw new RuntimeException("Leader moi khong thuoc cung nhom");
		}
		if (newLeader.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Chi duoc chon thanh vien dang hoat dong");
		}
		if (newLeader.getMemberId() == oldLeaderMemberId) {
			throw new RuntimeException("Khong the chon chinh leader hien tai");
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

		String subject = "[Aptech] Thong bao huy quyen tham gia nhom";
		String body = "Xin chao " + student.getFullName() + ",\n\n" + "Ban da bi huy quyen tham gia nhom.\n"
				+ "Nguoi thuc hien: " + staff.getFullName() + "\n" + "Ly do: " + reason + "\n\n"
				+ "Vui long lien he giao vu hoac giang vien huong dan neu can them thong tin.";

		mailService.sendEmailQuietly(student.getEmail(), subject, body);
	}

	public void renameGroup(int groupId, String newGroupName) {
		ProjectGroup group = groupRepository.findById(groupId);
		if (group == null) {
			throw new RuntimeException("Khong tim thay nhom");
		}
		String normalizedName = newGroupName != null ? newGroupName.trim() : "";
		if (normalizedName.isEmpty()) {
			throw new RuntimeException("Ten nhom khong duoc de trong");
		}
		if (groupRepository.existsGroupNameExcluding(group.getClassId(), groupId, normalizedName)) {
			throw new RuntimeException("Ten nhom da ton tai trong lop nay");
		}
		groupRepository.updateGroupName(groupId, normalizedName);
	}

	public void excludeMember(int memberId, int staffId, String reason) {
		GroupMember member = groupRepository.findMemberById(memberId);
		if (member == null) {
			throw new RuntimeException("Khong tim thay thanh vien");
		}
		if (member.getStatus() != MemberStatus.ACTIVE) {
			throw new RuntimeException("Thanh vien nay da bi loai");
		}
		if (groupRepository.countActiveMembers(member.getGroupId()) <= 2) {
			throw new RuntimeException("Moi nhom phai co toi thieu 2 sinh vien dang hoat dong");
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
			throw new RuntimeException("Khong tim thay thanh vien");
		}
		groupRepository.removeMember(memberId);
	}
}
