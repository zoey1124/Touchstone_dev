package edu.ecnu.touchstone.run;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedReader;
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
    static String runTestPath = "/home/zoey/Touchstone_dev/test/";

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
        }
    }

    // extract query parameter values from log and return a list of parameters
    // key word: Final instantiated parameters:
    // key word: Final global relative error:
    public static ArrayList<String[]> extractParams() {
        ArrayList<String[]> paramList = new ArrayList<>();
        try {
            String controllerLog = runTestPath + "/log/controller.log";
            String grepParamValueCommand = 
                "awk '/Final instantiated parameters:/,/Final global relative error:/ "
                + "{ if (!/Final instantiated parameters:/ && !/Final global relative error:/) print }' " 
                + controllerLog;
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("bash", "-c", grepParamValueCommand);
            Process process = processBuilder.start();

            // Read the output of the command
            InputStream inputStream = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String scientificNotationPattern = "[-+]?[0-9]*\\.[0-9]+E([-+]?[0-9]+)?";
            Pattern pattern = Pattern.compile(scientificNotationPattern);

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                line = line.trim();
                int startIndex = line.indexOf("values=[");
                int endIndex = line.indexOf("]", startIndex);
                String valueSubstring = line.substring(startIndex + "values=[".length(), endIndex);
                String[] valuesArray = valueSubstring.split(", ");
                // process date time format
                for (int i = 0; i < valuesArray.length; i++) {
                    String param = valuesArray[i];
                    Matcher dateTimeMatcher = pattern.matcher(param);
                    if (dateTimeMatcher.matches()) {
                        long milliseconds = (long) Double.parseDouble(param);
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date date = new Date(milliseconds);
                        String formattedDate = dateFormat.format(date);
                        valuesArray[i] = formattedDate;
                        System.out.println(formattedDate);
                    }
                }
                paramList.add(valuesArray);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return paramList;
    }

    public static ArrayList<String> loadQueries(ArrayList<String[]> paramList) {
        String queryDirectory = "/home/zoey/Touchstone_dev/experiment_prepare/app_query";

        return null;
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
            // Thread.sleep(20 * 1000); // wait for thread to finish
            String controllerLog = runTestPath + "/log/controller.log";
            String controllerGrepTimeCommand = "sudo grep 'Time of query instantiation' " + controllerLog;
            ProcessBuilder timerProcessBuilder = new ProcessBuilder();
            timerProcessBuilder.command("bash", "-c", controllerGrepTimeCommand);
            Process timerProcess = timerProcessBuilder.start();

            // Read the output of the command
            InputStream inputStream = timerProcess.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String controllerTime = reader.readLine().trim();
            System.out.println(controllerTime);

            /* 
             * 4. Load queries
             */
            ArrayList<String[]> paramList = extractParams();
            ArrayList<String> queries = loadQueries(paramList);

            /* 
             * 5. Connect with database
             */
            String url = "jdbc:postgresql://localhost:5432/tpch_touchstone_sf_1";    
            String user = "postgres";
            String password = "123456";
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to the PostgreSQL database successfully!");   
            System.exit(0);

            /* 
             * 6. Load data to database
             */
            importData(connection);

            /* 
             * 7. Execute queries
             */

            Statement statement = connection.createStatement();
            for (String query: queries) {
                ResultSet resultSet = statement.executeQuery(query);
            }

            /*
             * 8. Count # of rows for each query 
             */


            connection.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}