package com.izanled.translation.game.data;

public class UserData extends DocId {
    private String email;
    private int point;

    public UserData() {
    }

    public UserData(String email, int point) {
        this.email = email;
        this.point = point;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
