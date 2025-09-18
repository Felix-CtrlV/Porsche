package main.java.Utils;

import main.java.Database.Porsche_DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Session {

    private static Session instance;

    private int userid;
    private String username, role;

    private Session(int id, String name, String role){
        this.userid = id;
        this.username = name;
        this.role = role;
    }

    public static void startSession(int id, String name, String role){
        instance = new Session(id, name, role);
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

    public String getRole() {
        return role;
    }
}
