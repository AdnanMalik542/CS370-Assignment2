package edu.cs;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBUtil {

    private static final String DB_URL =
        "jdbc:mysql://cs370-db.cjae2gayu61d.us-east-2.rds.amazonaws.com:3306/cs370_assignment2?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    private static final String DB_USER = "db_user";
    private static final String DB_PASSWORD = "Heatman123";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
}