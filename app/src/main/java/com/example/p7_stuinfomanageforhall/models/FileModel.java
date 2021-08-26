package com.example.p7_stuinfomanageforhall.models;

public class FileModel {

    private String Name;
    private String Status;
    private String URL;

    public FileModel() {
    }

    public FileModel(String name, String status, String url) {
        Name = name;
        Status = status;
        URL=url;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
