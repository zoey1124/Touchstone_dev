package edu.ecnu.touchstone.extractor;
import java.util.List;
import java.util.stream.*;
import java.util.regex.Pattern;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import edu.ecnu.touchstone.schema.*;
import net.sf.jsqlparser.statement.*;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.*;
public class Query {

    String sql = null;
    Statement stmt = null;

    // tables used in the query
    List<Table> tables = null;

    public Query(String strQuery, List<Table> tables) {
        this.sql = strQuery;
        init(tables);
    }

    public void init(List<Table> tables) {
        try {
            stmt = CCJSqlParserUtil.parse(sql, 
                    parser -> parser.withSquareBracketQuotation(true));
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            List<String> tableList = tablesNamesFinder.getTableList(stmt);
            this.tables = tables.stream()
                                .filter(table -> tableList.contains(table.getTableName()))
                                .collect(Collectors.toList());
        } catch (JSQLParserException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    public boolean containNQ() {
        Matcher matcher = Pattern.compile("select").matcher(this.sql);
        int hit = 0;
        while (matcher.find()) {
            hit += 1;
        }
        return hit > 1;
    }

    /* 
     * @Description: Gievn an attribute name, return the table name it belongs to
     * @Input: attribute (column) name
     * @Return: table name
     */
    public Table attrToTable(String attr) {
        List<Table> table = this.tables.stream()
                                        .filter(t -> belongTable(attr, t))
                                        .collect(Collectors.toList());
        return table.get(0);
    }

    /* 
     * @Description: return true if attribute belongs to table
     */
    private boolean belongTable(String targetAttr, Table table) {
        List<String> attrList = table.getAttributes()
                                        .stream()
                                        .map(attr -> attr.getAttrName())
                                        .collect(Collectors.toList());
        attrList.addAll(table.getForeignKeys()
                             .stream()
                             .map(fk -> fk.getAttrName())
                             .collect(Collectors.toList()));
        attrList.addAll(table.getPrimaryKey()
                             .stream()
                             .map(pk -> pk.split("\\.")[1])
                             .collect(Collectors.toList()));
        return attrList.contains(targetAttr);
    }

    public String getSQL() {
        return this.sql;
    }

    public List<Table> getTables() {
        return this.tables;
    }

    public Statement getStmt() {
        return this.stmt;
    }

    @Override
    public String toString() {
        return "\n" + this.sql;
    }
}