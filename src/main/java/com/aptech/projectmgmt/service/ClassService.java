package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.SchoolClass;
import com.aptech.projectmgmt.repository.ClassRepository;

import java.util.List;

public class ClassService {

	private final ClassRepository classRepository = new ClassRepository();

	public List<SchoolClass> getAllClasses() {
		return classRepository.findAll();
	}

	public List<SchoolClass> getClassesByManager(int managerId) {
		return classRepository.findByManagerId(managerId);
	}
	public List<SchoolClass> getClassesByStaff(int staffId) {
	    return classRepository.findByStaffId(staffId);
	}

	public List<SchoolClass> getClassesByAdvisor(int staffId) {
		return classRepository.findByAdvisorId(staffId);
	}

    public void createClass(String className, String academicYear, Integer managerId) {
        String normalizedClassName = className != null ? className.trim() : "";
        if (normalizedClassName.isEmpty()) {
            throw new RuntimeException("Class name must not be empty");
        }
        if (ClassRepository.UNASSIGNED_CLASS_NAME.equalsIgnoreCase(normalizedClassName)) {
            throw new RuntimeException("This class name is reserved by the system. Please choose another name");
        }
        if (classRepository.findByName(normalizedClassName) != null) {
            throw new RuntimeException("Class name already exists");
        }
        classRepository.create(normalizedClassName, academicYear, managerId);
    }

	public SchoolClass getClassById(int classId) {
		return classRepository.findById(classId);
	}

	public int ensureUnassignedClass() {
		return classRepository.ensureUnassignedClass();
	}
}
