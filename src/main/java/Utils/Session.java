package Utils;

import Database.Porsche_DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Session {

    private static Session instance;

    private int userid;
    private String username, role, password, email, phone, address;
    private LocalDate dob;

    private Session(int id, String name, String role, String password, String email, String phone, String address, LocalDate dob){
        this.userid = id;
        this.username = name;
        this.role = role;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.dob = dob;
    }

    public static void startSession(int id, String name, String role, String password, String email, String phone, String address, LocalDate dob){
        instance = new Session(id, name, role, password, email, phone, address, dob);
    }

    public static Session getInstance(){
        return instance;
    }

    public static void closePreviousSessionIfAny(int userId) {
        try {
            Porsche_DB connect = new Porsche_DB();
            Connection con = connect.connect();

            PreparedStatement sessionclose = con.prepareStatement("UPDATE user_attendance SET check_out = ? WHERE user_id = ? AND check_out IS NULL");
            sessionclose.setString(1, String.valueOf(LocalDateTime.now()));
            sessionclose.setInt(2, userId);
            sessionclose.executeUpdate();

            connect.disconnect();
        } catch (
                SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void handleLogout(int userId) {
        closePreviousSessionIfAny(userId);
    }

    public static void clearSession(){
        instance = null;
    }

    public int getUserid() {
        return userid;
    }

    public String getUsername() {
        return username;
    }

    public static void setInstance(Session instance) {
        Session.instance = instance;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getRole() {
        return role;
    }

    public String getPassword() {
        return password;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getDob() {
        return dob;
    }

    public String getPhone() {
        return phone;
    }
}
