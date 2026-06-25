package com.campuslink.models;

public class Student {
    private int studentId;
    private int userId;
    private String fullName;
    private String email;
    private String phone;
    private String course;
    private int yearOfStudy;
    private String skills;
    private String cvPath;

    public Student() {}

    public Student(int studentId, int userId, String fullName, String email,
                   String phone, String course, int yearOfStudy, String skills, String cvPath) {
        this.studentId = studentId; this.userId = userId; this.fullName = fullName;
        this.email = email; this.phone = phone; this.course = course;
        this.yearOfStudy = yearOfStudy; this.skills = skills; this.cvPath = cvPath;
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public int getYearOfStudy() { return yearOfStudy; }
    public void setYearOfStudy(int yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }
    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    @Override
    public String toString() { return "Student{id=" + studentId + ", name='" + fullName + "'}"; }
}
