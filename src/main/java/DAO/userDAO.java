package DAO;

import Model.user;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class userDAO {

    private static Connection con;

    public userDAO(Connection con){
        this.con = con;
    }

    public static List<user> fetchCard() throws SQLException {
        List<user> users = new ArrayList<>();
        String sql = "select user_name, user_role, user_status from user_info";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
            user u = new user(rs.getString("user_name"), rs.getString("user_role"), rs.getString("user_status"));
            users.add(u);
        }
        return users;
    }

}
