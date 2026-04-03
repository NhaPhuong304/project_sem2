package com.aptech.projectmgmt.model;

public class Student {

    private int studentId;
    private String studentCode;
    private String fullName;
    private String email;
    private int classId;
    private Integer accountId;
    private String photoUrl;
    private String className;

    public Student() {}

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public Integer getAccountId() { return accountId; }
    public void setAccountId(Integer accountId) { this.accountId = accountId; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}
