package com.example.carl.mdhschemaapp;

class CourseInfo {
    String courseName;
    String courseCode;
    public CourseInfo() {}
    public CourseInfo(String code, String name) {
        this.courseCode = code;
        this.courseName = name;
    }

    @Override
    public String toString() {
        return this.courseCode;
    }
}
