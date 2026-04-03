package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.GroupMember;
import com.aptech.projectmgmt.model.MemberRole;
import com.aptech.projectmgmt.model.MemberStatus;
import com.aptech.projectmgmt.model.Project;
import com.aptech.projectmgmt.model.ProjectGroup;
import com.aptech.projectmgmt.model.Student;
import com.aptech.projectmgmt.repository.GroupRepository;
import com.aptech.projectmgmt.repository.ProjectRepository;
import com.aptech.projectmgmt.repository.StudentRepository;

import java.util.List;

public class GroupService {

    private final GroupRepository groupRepository = new GroupRepository();
    private final ProjectRepository projectRepository = new ProjectRepository();
    private final StudentRepository studentRepository = new StudentRepository();

    public List<ProjectGroup> getGroupsByProject(int projectId) {
        return groupRepository.findByProjectId(projectId);
    }

    public List<GroupMember> getMembersByGroup(int groupId) {
        return groupRepository.findMembersByGroupId(groupId);
    }

    public List<GroupMember> getActiveMembersByGroup(int groupId) {
        return groupRepository.findMembersByGroupId(groupId)
                .stream()
                .filter(member -> member.getStatus() == MemberStatus.ACTIVE)
                .toList();
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
        if (role == MemberRole.LEADER && groupRepository.hasActiveLeader(groupId)) {
            throw new RuntimeException("Nhom nay da co truong nhom");
        }
        groupRepository.addMember(groupId, studentId, role);
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
    }
}
