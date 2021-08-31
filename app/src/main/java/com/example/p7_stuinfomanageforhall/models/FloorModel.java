package com.example.p7_stuinfomanageforhall.models;

public class FloorModel {
    private String FloorNo;
    private Long TotalRoomInFloor;
    private Long TotalSeatInFloor;
    private Long TotalStuInFloor;

    public FloorModel(){

    }

    public FloorModel(String floorNo, Long totalRoomInFloor, Long totalSeatInFloor, Long totalStuInFloor){
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

    public Long getTotalRoomInFloor() {
        return TotalRoomInFloor;
    }

    public void setTotalStuInFloor(Long totalStuInFloor) {
        TotalStuInFloor = totalStuInFloor;
    }

    public Long getTotalSeatInFloor() {
        return TotalSeatInFloor;
    }

    public void setTotalSeatInFloor(Long totalSeatInFloor) {
        TotalSeatInFloor = totalSeatInFloor;
    }

    public Long getTotalStuInFloor() {
        return TotalStuInFloor;
    }

    public void setTotalRoomInFloor(Long totalRoomInFloor) {
        TotalRoomInFloor = totalRoomInFloor;
    }
}
