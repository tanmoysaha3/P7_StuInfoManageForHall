package com.example.p7_stuinfomanageforhall.models;

public class FloorModel {
    private String FloorNo;
    private String TotalRoomInFloor;
    private String TotalSeatInFloor;
    private String TotalStuInFloor;

    public FloorModel(){

    }

    public FloorModel(String floorNo, String totalRoomInFloor, String totalSeatInFloor, String totalStuInFloor){
        this.FloorNo=floorNo;
        this.TotalRoomInFloor=totalRoomInFloor;
        this.TotalSeatInFloor=totalSeatInFloor;
        this.TotalStuInFloor=totalStuInFloor;
    }

    public String getFloorNo() {
        return FloorNo;
    }

    public void setFloorNo(String floorNo) {
        FloorNo = floorNo;
    }

    public String getTotalRoomInFloor() {
        return TotalRoomInFloor;
    }

    public void setTotalRoomInFloor(String totalRoomInFloor) {
        TotalRoomInFloor = totalRoomInFloor;
    }

    public String getTotalSeatInFloor() {
        return TotalSeatInFloor;
    }

    public void setTotalSeatInFloor(String totalSeatInFloor) {
        TotalSeatInFloor = totalSeatInFloor;
    }

    public String getTotalStuInFloor() {
        return TotalStuInFloor;
    }

    public void setTotalStuInFloor(String totalStuInFloor) {
        TotalStuInFloor = totalStuInFloor;
    }
}
