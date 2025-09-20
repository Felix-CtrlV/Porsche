package DAO;

import Model.user;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class userDAO {

    private static Connection con;

    public userDAO(Connection con){
        this.con = con;
    }

    public static List<user> accountData() throws SQLException{
        List<user> users = new ArrayList<>();
        String sql = "select user_id, user_name, user_phone, user_email, user_address, dob, user_status from user_info order by user_status desc";
        PreparedStatement get = con.prepareStatement(sql);
        ResultSet rs = get.executeQuery();
        while(rs.next()){
            int id = rs.getInt(1);
            String name = rs.getString(2);
            String phone = rs.getString(3);
            String email = rs.getString(4);
            String address = rs.getString(5);
            String dob = rs.getString(6);
            String status = rs.getInt(7) == 1 ? "active" : "inactive";
            user u = new user(id, name, phone, email, address, LocalDate.parse(dob), status);
            users.add(u);
        }
        return users;
    }

}
