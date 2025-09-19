package DAO;

import Model.user;

import java.sql.*;
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

        }
        return null;
    }

}
