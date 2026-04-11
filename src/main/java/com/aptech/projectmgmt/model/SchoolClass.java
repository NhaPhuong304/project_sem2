package com.aptech.projectmgmt.model;

import java.time.LocalDateTime;

public class SchoolClass {

	private int classId;
	private String className;
	private String academicYear;
	private LocalDateTime createdAt;
	private int studentCount;

	public SchoolClass() {
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getAcademicYear() {
		return academicYear;
	}

	public void setAcademicYear(String academicYear) {
		this.academicYear = academicYear;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public int getStudentCount() {
		return studentCount;
	}

	public void setStudentCount(int studentCount) {
		this.studentCount = studentCount;
	}

	@Override
	public String toString() {
		return className + " (" + " - " + academicYear + ")";
	}
}
