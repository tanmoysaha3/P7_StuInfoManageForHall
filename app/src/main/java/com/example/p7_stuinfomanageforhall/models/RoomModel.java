package com.example.p7_stuinfomanageforhall.models;

public class RoomModel {
    private String UpdatedRoomNo;
    private Long TotalSeatInRoom;
    private Long TotalStuInRoom;

    public RoomModel(){

    }

    public RoomModel(String updatedRoomNo, Long totalSeatInRoom, Long totalStuInRoom){
        this.UpdatedRoomNo=updatedRoomNo;
        this.TotalSeatInRoom=totalSeatInRoom;
        this.TotalStuInRoom=totalStuInRoom;
    }

    public String getUpdatedRoomNo() {
        return UpdatedRoomNo;
    }

    public void setUpdatedRoomNo(String updatedRoomNo) {
        UpdatedRoomNo = updatedRoomNo;
    }

    public Long getTotalSeatInRoom() {
        return TotalSeatInRoom;
    }

    public void setTotalStuInRoom(Long totalStuInRoom) {
        TotalStuInRoom = totalStuInRoom;
    }

    public Long getTotalStuInRoom() {
        return TotalStuInRoom;
    }

    public void setTotalSeatInRoom(Long totalSeatInRoom) {
        TotalSeatInRoom = totalSeatInRoom;
    }
}
