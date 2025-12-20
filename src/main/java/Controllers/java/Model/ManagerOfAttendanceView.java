package Controllers.java.Model;

import java.sql.Time;

public class ManagerOfAttendanceView {
    private String workers ;
    private Time sign_in_time ;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Time getSign_in_time() {
        return sign_in_time;
    }

    public void setSign_in_time(Time sign_in_time) {
        this.sign_in_time = sign_in_time;
    }

    public String getWorkers() {
        return workers;
    }

    public void setWorkers(String workers) {
        this.workers = workers;
    }

    public ManagerOfAttendanceView(){}

    public ManagerOfAttendanceView(String name , Time sign_in_time,String status){
        this.workers = name;
        this.sign_in_time = sign_in_time;
        this.status = status;
    }
}
