package edu.ecnu.touchstone.run;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import io.netty.handler.codec.http.HttpContentEncoder.Result;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;

/*
 * Given TPC-H files, 
 * 1. run Controller, 
 * 2. run Generator, 
 * 3. query parameters instantiation,
 * 4. load data into database 
 */

public class Pipeline{
    private static String getOrder() throws IOException{
        String out = "";
        try {
        ProcessBuilder processBuilder = new ProcessBuilder();
        String cmd = "sudo grep -A 1 'The partial order of tables:' /home/zoey/Touchstone_dev/test/log/touchstone.log | tail -n 1";
        processBuilder.command("bash", "-c", cmd);
        Process process = processBuilder.start();
        InputStream inputStream = process.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        out = reader.readLine().trim();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getClass().getName()+": "+e.getMessage());
            System.exit(0);
        }
        return out;
    }

    // use this function on each data generator node, import data from txt file to postgresql 
    public static void importData(Connection conn) throws Exception{
        String s = getOrder();
        s = s.substring(1, s.length() - 1);
        String[] partialOrder = s.split(", ");
        System.out.println("partial order is " + partialOrder);
        Statement stmt = conn.createStatement();
        for (int i = 1; i < partialOrder.length; i++) {
            String table = partialOrder[i];
            String sql0 = "COPY " + table + " FROM '/home/zoey/Touchstone_dev/data/" + table + "_0.txt' delimiter ',' CSV;";
            stmt.executeUpdate(sql0);
            String sql1 = "COPY " + table + " FROM '/home/zoey/Touchstone_dev/data/" + table + "_1.txt' delimiter ',' CSV;";
            stmt.executeUpdate(sql1);
        }
    }

    public static void main(String[] args) {
        // args[0]: the path of the configuration file 
        if (args.length != 1) {
			System.out.println("Please specify the configuration file for Touchstone!");
			System.exit(0);
		} 
        try {
            /*
             * 1. run controller 
             * want to execute: java -jar <test_dir>//RunController.jar <test_dir>//tpch.conf
             */
            String runTestPath = "/home/zoey/Touchstone_dev/test/";
            String runControllerPath = runTestPath + "//RunController.jar";
            String controllerCmd = "nohup java -jar " 
                                + runControllerPath 
                                + " " 
                                + runTestPath + "tpch.conf "
                                + "> /home/zoey/Touchstone_dev/test//log/controller.log";
            System.out.println(controllerCmd);

            ProcessBuilder controllerProcessBuilder = new ProcessBuilder();
            controllerProcessBuilder.command("bash", "-c", controllerCmd);
            controllerProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            controllerProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            // controllerProcessBuilder.start();

            /*
             * 2. run data generator
             * want to execute: java -jar <test_dir>//RunDataGenerator.jar <test_dir>//tpch.conf
             */
            String runDataGeneratorPath = runTestPath + "//RunDataGenerator.jar";
            String generatorCmd = "nohup java -jar " 
                                + runDataGeneratorPath 
                                + " " 
                                + runTestPath + "tpch.conf 0 " 
                                + "> /home/zoey/Touchstone_dev/test//log/generator.log";
            System.out.println(generatorCmd);

            ProcessBuilder generatorProcessBuilder = new ProcessBuilder();
            generatorProcessBuilder.command("bash", "-c", generatorCmd);

            controllerProcessBuilder.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            controllerProcessBuilder.redirectError(ProcessBuilder.Redirect.INHERIT);
            // controllerProcessBuilder.start();

            /*
             * 3. Extract total runtime from log
             * key word: Time of query instantiation
             */
            String controllerLog = runTestPath + "/log/controller.log";
            String controllerGrepTimeCommand = "sudo grep 'Time of query instantiation' " + controllerLog;
            String generatorGrepTimeCommand = "sudo grep -A 1 'Time of query instantiation' " + runTestPath + "/log/generator.log | tail -n 1";
            ProcessBuilder timerProcessBuilder = new ProcessBuilder();
            timerProcessBuilder.command("bash", "-c", controllerGrepTimeCommand);
            Process timerProcess = timerProcessBuilder.start();

            // Read the output of the command
            InputStream inputStream = timerProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String controllerTime = reader.readLine().trim();
            System.out.println(controllerTime);

            /* 
             * 4. Connect with database
             */
            String url = "jdbc:postgresql://localhost:5432/tpch_touchstone_sf_1";    
            String user = "postgres";
            String password = "123456";
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL database successfully!");   

            /* 
             * 5. Load data to database
             */
            importData(connection);
            
            /*
             * 6. Put values into queries
             */
            
            String queryFilePath = "/home/zoey/Touchstone_dev/experiment_prepare/app_query/tpch_queries.sql";
            BufferedReader queryFileReader = new BufferedReader(new FileReader(queryFilePath));
            String currentQuery = "";
            String line;
            ArrayList<String> queries = new ArrayList<String>();
            while ((line = queryFileReader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("--")) { // start a new query
                    queries.add(currentQuery);
                    currentQuery = "";
                } else { // append to current query
                    currentQuery += " " + line;
                }
            }
            queryFileReader.close();

            /* 
             * 7. Execute queries
             */

            Statement statement = connection.createStatement();
            for (String query: queries) {
                ResultSet resultSet = statement.executeQuery(query);
            }

            /*
             * 7. Count # of rows for each query 
             */


            connection.close();

            /* 8. Check data constraints on each column */


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}