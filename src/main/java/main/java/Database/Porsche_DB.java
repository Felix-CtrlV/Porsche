package main.java.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Porsche_DB {

    final String url = "jdbc:mysql://avnadmin:AVNS_tTQ3tp0kJN1xSXUbvN5@car-pg3-anthtooaung2792005-fda5.d.aivencloud.com:26737/car_show_room?ssl-mode=REQUIRED";
    final String username = "avnadmin";
    final String password = "AVNS_tTQ3tp0kJN1xSXUbvN5";
    Connection con;

    public Connection connect () throws ClassNotFoundException, SQLException {
        Class.forName("com.mysql.cj.jdbc.Driver");
        con = DriverManager.getConnection(url, username, password);
        System.out.println("Connected");
        return con;
    }

    public void disconnect() throws SQLException {
        if(con!=null && !con.isClosed()){
            con.close();
            System.out.println("Disconnected from the main.java.Database");
        }
    }
}
