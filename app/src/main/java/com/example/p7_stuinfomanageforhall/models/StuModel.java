package com.example.p7_stuinfomanageforhall.models;

public class StuModel {
    private String StudentId;
    private String Name;

    public StuModel() {
    }

    public StuModel(String studentId, String name) {
        StudentId = studentId;
        Name = name;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStudentId() {
        return StudentId;
    }

    public void setStudentId(String studentId) {
        StudentId = studentId;
    }
}
