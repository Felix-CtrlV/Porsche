package Model;

import java.time.LocalDate;

public class user {
    private int id;
    private String username, email, password, address, phone, role, nrc, is_active;
    private LocalDate dob;
    private String start_date, end_date;
    public user(){}

    public user(int id, String username, String role, String start_date, String end_date, String is_active){
        this.id = id;
        this.username = username;
        this.role = role;
        this.start_date = start_date;
        this.end_date = end_date;
        this.is_active = is_active;
    }
    public user(int id, String username, String role){
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public user(String username, String role, String is_active){
        this.username = username;
        this.role = role;
        this.is_active = is_active;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getIs_active() {
        return is_active;
    }

    public void setIs_active(String is_active) {
        this.is_active = is_active;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getStart_date() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
        return start_date;
    }

    public String getEnd_date() {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d HH:mm:ss");
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getNrc() {
        return nrc;
    }

    public void setNrc(String nrc) {
        this.nrc = nrc;
    }
}
