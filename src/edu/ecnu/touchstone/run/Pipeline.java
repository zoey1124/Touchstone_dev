package edu.ecnu.touchstone.run;
import java.sql.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

/*
 * Given TPC-H files, 
 * 1. run Controller, 
 * 2. run Generator, 
 * 3. query parameters instantiation,
 * 4. load data into database 
 */

public class Pipeline{
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

    public static void main(String[] args) {
        // args[0]: the path of the configuration file 
        if (args.length != 1) {
			System.out.println("Please specify the configuration file for Touchstone!");
			System.exit(0);
		} 
        try {
            Configurations configurations = new Configurations(args[0]);
            
    
            // run controller
            String[] cmd = new String[]{"zsh", "-c", ""};
            Process process = Runtime.getRuntime().exec(cmd);

            // run data generator 
            /*
             * want to execute: /usr/bin/env /home/zoey/jdk-17.0.8.1+1/bin/java -agentlib:jdwp=transport=dt_socket,server=n,suspend=y,address=localhost:37293 @/tmp/cp_8gihk4qhd8gn88rpnjcyhffm5.argfile edu.ecnu.touchstone.run.RunController .//test//redmine.conf 
             */
    
            // run db connector
            Pipeline db = new Pipeline();
            Connection conn = db.connect();
            db.importData(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}