package edu.ecnu.touchstone.run;

import java.sql.*;
import java.util.Map;

public class SchemaGenerator {
    private final String url = "jdbc:postgresql://localhost:5432/redmine";
    private final String user = "redmine";
    private final String password = "my_password";
    int NUM = 3000;

    Map<String, String> dataToType = Map.of(
        "integer", "integer",
        "character varying", "varchar",
        "timestamp without time zone'", "datetime",
        "text", "varchar",
        "boolean", "bool",
        "bytea", "varchar",
        "date", "date",
        "double precision", "decimal",
        "bigint", "integer"
    );

    public Connection connect() throws Exception {
        Connection c = null;
        try {
            c = DriverManager.getConnection(url, user, password);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        System.out.println("Open database successfully");
        return c;
    }
    
    public static void main(String[] args) {
        SchemaGenerator sg = new SchemaGenerator();
        try {
            Connection conn = sg.connect();
            // get Table
            String sql = "select table_name from information_schema.tables where table_type = 'BASE TABLE' and table_schema = 'public';";
            Statement stmt = conn.createStatement();
            
        } catch (Exception e) {

        }   
    }
}


