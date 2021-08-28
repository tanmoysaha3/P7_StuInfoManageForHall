package com.example.p7_stuinfomanageforhall.models;

public class HallModel {

    private String HallName;
    private String HallId;
    private String HallAdmin;
    private String TotalFloorInHall;
    private String TotalRoomInHall;
    private String TotalSeatInHall;
    private String TotalStuInHall;
    private String HallType;

    public HallModel(){

    }

    public HallModel(String hallName, String hallId, String hallAdmin, String totalFloorInHall,
                     String totalRoomInHall, String totalSeatInHall, String totalStuInHall,
                     String hallType){
        this.HallName=hallName;
        this.HallId=hallId;
        this.HallAdmin=hallAdmin;
        this.TotalFloorInHall=totalFloorInHall;
        this.TotalRoomInHall=totalRoomInHall;
        this.TotalSeatInHall=totalSeatInHall;
        this.TotalStuInHall=totalStuInHall;
        this.HallType=hallType;
    }

    public String getHallName() {
        return HallName;
    }

    public void setHallName(String hallName) {
        HallName = hallName;
    }

    public String getHallId() {
        return HallId;
    }

    public void setHallId(String hallId) {
        HallId = hallId;
    }

    public String getHallAdmin() {
        return HallAdmin;
    }

    public void setHallAdmin(String hallAdmin) {
        HallAdmin = hallAdmin;
    }

    public String getTotalFloorInHall() {
        return TotalFloorInHall;
    }

    public void setTotalFloorInHall(String totalFloorInHall) {
        TotalFloorInHall = totalFloorInHall;
    }

    public String getTotalRoomInHall() {
        return TotalRoomInHall;
    }

    public void setTotalRoomInHall(String totalRoomInHall) {
        TotalRoomInHall = totalRoomInHall;
    }

    public String getTotalSeatInHall() {
        return TotalSeatInHall;
    }

    public void setTotalSeatInHall(String totalSeatInHall) {
        TotalSeatInHall = totalSeatInHall;
    }

    public String getTotalStuInHall() {
        return TotalStuInHall;
    }

    public void setTotalStuInHall(String totalStuInHall) {
        TotalStuInHall = totalStuInHall;
    }

    public String getHallType() {
        return HallType;
    }

    public void setHallType(String hallType) {
        HallType = hallType;
    }
}
