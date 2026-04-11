package com.aptech.projectmgmt.service;

import com.aptech.projectmgmt.model.SchoolClass;
import com.aptech.projectmgmt.repository.ClassRepository;

import java.util.List;

public class ClassService {

	private final ClassRepository classRepository = new ClassRepository();

	public List<SchoolClass> getAllClasses() {
		return classRepository.findAll();
	}

	public List<SchoolClass> getClassesByAdvisor(int staffId) {
		return classRepository.findByAdvisorId(staffId);
	}

	public void createClass(String className, String academicYear) {
		String normalizedClassName = className != null ? className.trim() : "";
		if (normalizedClassName.isEmpty()) {
			throw new RuntimeException("Ten lop khong duoc de trong");
		}
		if (ClassRepository.UNASSIGNED_CLASS_NAME.equalsIgnoreCase(normalizedClassName)) {
			throw new RuntimeException("Ten lop nay duoc he thong du phong, vui long chon ten khac");
		}
		if (classRepository.findByName(normalizedClassName) != null) {
			throw new RuntimeException("Ten lop da ton tai");
		}
		classRepository.create(normalizedClassName, academicYear);
	}

	public SchoolClass getClassById(int classId) {
		return classRepository.findById(classId);
	}

	public int ensureUnassignedClass() {
		return classRepository.ensureUnassignedClass();
	}
}
