package edu.ecnu.touchstone.extractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.touchstone.rule.Rule;
import edu.ecnu.touchstone.run.Touchstone;
import edu.ecnu.touchstone.schema.Table;
import edu.ecnu.touchstone.schema.SchemaReader;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

public class Loader {

    private Logger logger = null;
    String ccInputFile = null;

    public Loader(String ccInputFile) {
        logger = Logger.getLogger(Touchstone.class);
        this.ccInputFile = ccInputFile;
    }


    // Read in a .sql file line by line
    public List<Query> load(String sqlInputFile, List<Table> tables) {
        // initialize joinTable: tableName -> pk start join number 
        HashMap<String, Integer> joinTable = (HashMap<String, Integer>) 
                            tables.stream()
                            .collect(Collectors.toMap(t -> t.getTableName(), value -> 0));

        List<Query> queryOut = new ArrayList<Query>();
        List<String> CCList = new ArrayList<>();
        String inputLine = null;
        try (FileWriter ccWriter = new FileWriter(this.ccInputFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(new 
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
                        Query query = new Query(queryLine, tables);
                        // Call Rule on each Query
                        List<String> constraintChains = parseQuery(id, query, joinTable); 
                        // Write into cc input file 
                        for (String cc: constraintChains) {
                            ccWriter.write(cc + System.lineSeparator());
                        }
                        ccWriter.write("\n");
                        CCList.addAll(constraintChains);
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
            ccWriter.close();
        } catch (Exception e) {
            System.out.println("\n\tError input line: ");
            e.printStackTrace();
            System.exit(0);
        }
        logger.debug("cardinality input is: \n" + CCList);
        return queryOut;
    }
    
    /* 
     * @Description: decouple table from query to format constraint chain 
     */
    public List<String> parseQuery(int id, Query query, HashMap<String, Integer> joinTable) {
        List<String> ret = new ArrayList<>();
        ret.add("## query " + id);
        Rule rule = new Rule(query, joinTable);
        List<Info> infos = rule.parse(query);
        List<Table> tables = query.getTables();
        for (Table table: tables) {
            List<String> CCList = new ArrayList<>();
            CCList.add("[" + table.getTableName() + "]");
            // filter operation cardinality format 
            List<Info> filterInfos = infos.stream()                                    
                                    .filter(info -> info.getTable().equals(table.getTableName()))
                                    .filter(info -> info instanceof FilterOpInfo)
                                    .collect(Collectors.toList());
            if (filterInfos.size() == 1) {
                String filter = filterInfos.get(0).toString();
                String filterInfo = String.format("[0, %s, %f]", filter, 0.5);
                CCList.add(filterInfo);
            } 
            // add logical relation if multiple filters 
            else if (filterInfos.size() > 1) {
                String logicalRelation = rule.getLogicalRelation(query);
                String filter = filterInfos.stream()
                            .map(info -> info.toString())
                            .collect(Collectors.joining("#"));
                String filterInfo = String.format("[0, %s#%s, %f]", filter, logicalRelation, 0.5);
                CCList.add(filterInfo);
            }

            // public key cardinality constraint format
            String pkInfos = infos.stream()
                                .filter(info -> info.getTable().equals(table.getTableName()))
                                .filter(info -> info instanceof PkInfo)
                                .map(info -> info.toString())
                                .collect(Collectors.joining("; "));
            if (pkInfos.length() > 0)    CCList.add(pkInfos);

            // foreign key cardinality constraint format
            String fkInfos = infos.stream()
                                    .filter(info -> info.getTable().equals(table.getTableName()))
                                    .filter(info -> info instanceof FkInfo)
                                    .map(info -> info.toString())
                                    .collect(Collectors.joining("; "));
            if (fkInfos.length() > 0)    CCList.add(fkInfos);

            // the table should have some cardinality constraints 
            if (CCList.size() > 1) {
                String constraintChain = CCList.stream().collect(Collectors.joining("; "));
                ret.add(constraintChain);
            }
        }
        return ret;
    }

    // test 
    public static void main(String[] args) {
        PropertyConfigurator.configure(".//test//lib//log4j.properties");
        Loader loader = new Loader(".//test//input//redmine_cardinality_constraint.txt");
        SchemaReader schemaReader = new SchemaReader();
        List<Table> tables = schemaReader.read(".//test//input//redmine_schema.txt");
        loader.load(".//test//input//redmine_sql.sql", tables);
    }
}