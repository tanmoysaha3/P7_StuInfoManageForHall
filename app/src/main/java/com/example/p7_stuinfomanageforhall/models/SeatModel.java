package com.example.p7_stuinfomanageforhall.models;

public class SeatModel {

    private String SeatNo;
    private String AssignedStuId;
    private String AssignedStuName;

    public SeatModel() {
    }

    public SeatModel(String seatNo, String assignedStuId, String assignedStuName) {
        SeatNo = seatNo;
        AssignedStuId = assignedStuId;
        AssignedStuName=assignedStuName;
    }

    public String getAssignedStuId() {
        return AssignedStuId;
    }

    public void setAssignedStuId(String assignedStuId) {
        AssignedStuId = assignedStuId;
    }

    public String getSeatNo() {
        return SeatNo;
    }

    public void setSeatNo(String seatNo) {
        SeatNo = seatNo;
    }

    public String getAssignedStuName() {
        return AssignedStuName;
    }

    public void setAssignedStuName(String assignedStuName) {
        AssignedStuName = assignedStuName;
    }
}
