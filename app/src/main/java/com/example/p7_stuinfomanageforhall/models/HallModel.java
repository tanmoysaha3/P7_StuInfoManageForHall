package com.example.p7_stuinfomanageforhall.models;

public class HallModel {

    private String HallName;
    private String HallId;
    private String HallAdmin;
    private String HallAdminId;
    private Long TotalFloorInHall;
    private Long TotalRoomInHall;
    private Long TotalSeatInHall;
    private Long TotalStuInHall;
    private String HallType;

    public HallModel(){

    }

    public HallModel(String hallName, String hallId, String hallAdmin, String hallAdminId, Long totalFloorInHall,
                     Long totalRoomInHall, Long totalSeatInHall, Long totalStuInHall,
                     String hallType){
        this.HallName=hallName;
        this.HallId=hallId;
        this.HallAdmin=hallAdmin;
        this.TotalFloorInHall=totalFloorInHall;
        this.TotalRoomInHall=totalRoomInHall;
        this.TotalSeatInHall=totalSeatInHall;
        this.TotalStuInHall=totalStuInHall;
        this.HallType=hallType;
        this.HallAdminId=hallAdminId;
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

    public String getHallAdminId() {
        return HallAdminId;
    }

    public void setHallAdminId(String hallAdminId) {
        HallAdminId = hallAdminId;
    }

    public Long getTotalFloorInHall() {
        return TotalFloorInHall;
    }

    public void setTotalStuInHall(Long totalStuInHall) {
        TotalStuInHall = totalStuInHall;
    }

    public Long getTotalRoomInHall() {
        return TotalRoomInHall;
    }

    public void setTotalSeatInHall(Long totalSeatInHall) {
        TotalSeatInHall = totalSeatInHall;
    }

    public Long getTotalSeatInHall() {
        return TotalSeatInHall;
    }

    public void setTotalRoomInHall(Long totalRoomInHall) {
        TotalRoomInHall = totalRoomInHall;
    }

    public Long getTotalStuInHall() {
        return TotalStuInHall;
    }

    public void setTotalFloorInHall(Long totalFloorInHall) {
        TotalFloorInHall = totalFloorInHall;
    }

    public String getHallType() {
        return HallType;
    }

    public void setHallType(String hallType) {
        HallType = hallType;
    }
}
