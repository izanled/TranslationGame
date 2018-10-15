package com.izanled.translation.game.data;

public class UserData {
    private String _id;
    private String email;
    private int point;

    public UserData() {
    }

    public UserData(String email, int point) {
        this._id = _id;
        this.email = email;
        this.point = point;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
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
