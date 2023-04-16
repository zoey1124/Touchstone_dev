package edu.ecnu.touchstone.run;
import java.sql.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Put data in txt files into postgreSQL database 
public class PostgresqlConn {
    private final String url = "jdbc:postgresql://localhost:5432/tpch";
    private final String user = "tpch";
    private final String password = "123456";
    
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

    private String getOrder() throws IOException{
        String out = "";
        try {
        String[] cmd = {"/bin/sh", "-c", "sudo grep -A 1 'The partial order of tables:' /home/zoey/start_test/log/touchstone.log | tail -n 1"};
        Process process = Runtime.getRuntime().exec(cmd);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        out = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return out.trim();
    }

    // use this function on each data generator node, import data from txt file to postgresql 
    public void importData(Connection conn) throws Exception{
        String s = getOrder();
        s = s.substring(1, s.length() - 1);
        String[] partialOrder = s.split(", ");
        for (int i = 0; i < partialOrder.length; i++) {
            String table = partialOrder[i];
            Statement stmt = conn.createStatement();
            String sql = "COPY " + table + " FROM '/home/zoey/start_test//dg1//data//" + table + "_0.txt' delimiter ',' CSV;";
            stmt.executeUpdate(sql);
        }
    }

    // this file will also be uploaded to remote server under /start_test directory
    public static void main() throws Exception {
        PostgresqlConn db = new PostgresqlConn();
        Connection conn = db.connect();
        db.importData(conn);
    }
}