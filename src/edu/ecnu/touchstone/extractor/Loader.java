package edu.ecnu.touchstone.extractor;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import edu.ecnu.touchstone.run.Touchstone;
import edu.ecnu.touchstone.schema.Table;
import edu.ecnu.touchstone.schema.SchemaReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class Loader {

    private Logger logger = null;

    public Loader() {
        logger = Logger.getLogger(Touchstone.class);
    }


    // Read in a .sql file line by line
    public List<Query> load(String sqlInputFile, List<Table> tables) {
        List<Query> queryOut = new ArrayList<Query>();

        String inputLine = null;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new 
                FileInputStream(sqlInputFile)))) 
        {
            String queryLine = "";
            int id = 0;
            while ((inputLine = br.readLine()) != null) {
                inputLine = inputLine.toLowerCase().strip();
                // comments or blank
                if (inputLine.startsWith("--") 
                    || inputLine.isBlank()) { 
                    continue;
                } 
                // end of a query
                else if (inputLine.contains(";")) { 
                    queryLine += inputLine;
                    // don't consider non-queey sql (e.g. create, drop)
                    if (queryLine.startsWith("select")) { 
                        Query query = new Query(id, queryLine, tables);
                        queryOut.add(query);
                        id += 1;
                    }
                    queryLine = "";
                } 
                // middle of a query
                else {
                    queryLine += inputLine + " ";
                }
            }
        } catch (Exception e) {
            System.out.println("\n\tError input line: ");
            e.printStackTrace();
            System.exit(0);
        }
        logger.debug("Query Info is: \n" + queryOut);
        return queryOut;
    }
    
    

    // test 
    public static void main(String[] args) {
        PropertyConfigurator.configure(".//test//lib//log4j.properties");
        Loader loader = new Loader();
        SchemaReader schemaReader = new SchemaReader();
        List<Table> tables = schemaReader.read(".//test//input//redmine_schema_sf_1.txt");
        loader.load(".//test//input//redmine_test.sql", tables);
    }
}