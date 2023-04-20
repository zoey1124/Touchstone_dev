package edu.ecnu.touchstone.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import edu.ecnu.touchstone.extractor.FilterOpInfo;
import edu.ecnu.touchstone.extractor.FkInfo;
import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.PkInfo;
import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.rule.Rule;
import edu.ecnu.touchstone.schema.SchemaReader;
import edu.ecnu.touchstone.schema.Table;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Join;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.expression.*;


public class RuleTest {

    private Rule rule;
    String sql1 = "select issues.* from issues "
                + "where issues.root_id = $1 and (issues.lft >= 4 and issues.rgt <= 5) "
                + "order by issues.lft ASC;";

    String sql2 = "select 1 as one from enumerations "
                + "where enumerations.type in ('IssuePriority') "
                + "or enumeration.type not in ('test') limit $1";
    
    String sql3 = "select attachments.* from attachments where (filename like '%');";
    
    String sql4 = "select distinct roles.* "
                + "from roles inner join member_roles "
                + "on member_roles.role_id = roles.id "
                + "inner join members on members.id = member_roles.member_id "
                + "inner join projects on projects.id = members.project_id "
                + "where (projects.status <> 9) and members.user_id = 2;";

    String tpch_sql5 = "select n_name, sum(l_extendedprice * (1 - l_discount)) as revenue "
                     + "from customer, orders, lineitem, supplier, nation, region "
                     + "where c_custkey = o_custkey and l_orderkey = o_orderkey and l_suppkey = s_suppkey "
                     + "and c_nationkey = s_nationkey and s_nationkey = n_nationkey "
                     + "and n_regionkey = r_regionkey and r_name = '[REGION]' "
                     + "and o_orderdate >= date '[DATE]' and o_orderdate < date '[DATE]' + interval '1' year "
                     + "group by n_name order by revenue desc;";

    String sql6 = "select roles.* from roles where roles.builtin = $1;";

    String sql7 = "SELECT count(users.* ) FROM users INNER JOIN watchers_copy "
                + "ON users.id = watchers_copy.user_id "
                + "WHERE watchers_copy.watchable_type = $1 "
                + "AND watchers_copy.watchable_id = $2 AND users.status = $3;";

    SchemaReader redmine_SchemaReader = new SchemaReader();
    List<Table> redmine_tables = redmine_SchemaReader.read(".//test//input//redmine_schema.txt");
    SchemaReader tpch_SchemaReader = new SchemaReader();
    List<Table> tpch_tables = tpch_SchemaReader.read(".//test//input//tpch_schema_sf_1.txt");
    Query q1 = new Query(sql1, redmine_tables);
    Query q2 = new Query(sql2, redmine_tables);
    Query q3 = new Query(sql3, redmine_tables);
    Query q4 = new Query(sql4, redmine_tables);
    Query q5 = new Query(tpch_sql5, tpch_tables);
    Query q6 = new Query(sql6, redmine_tables);
    Query q7 = new Query(sql7, redmine_tables);

    @Test
    public void testParseWhereBaseCase() throws Exception {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql1);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            List<Info> conditions = rule.parseExpression(where);
            assertEquals("Base case parse where should work", 3, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testParseWhereIn() throws Exception {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql2);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            List<Info> conditions = rule.parseExpression(where);
            assertEquals("IN case parse where should work", 2, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseWhereLike() throws Exception {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql3);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            List<Info> conditions = rule.parseExpression(where);
            assertEquals("LIKE case parse where should work", 1, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseWhereNotEqual() throws Exception {
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql4);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            List<Info> conditions = rule.parseExpression(where);
            assertEquals("<> case parse where should work", 2, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParseWhereTpch5() throws Exception {
        try {
            Statement stmt = CCJSqlParserUtil.parse(tpch_sql5);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            List<Info> conditions = rule.parseExpression(where);
            assertEquals("<> case parse where should work", 15, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParse() throws Exception {
        Rule rule = new Rule(q1);
        List<Info> infos = rule.parse();
        assertEquals("Base case parse should work", 3, infos.size());
    }

    @Test
    public void testParseFilterOpInfo() throws Exception {
        Rule rule = new Rule(q6);
        List<Info> infos = rule.parse();
        assertEquals("Should have 1 Info", 1, infos.size());
        assertTrue(infos.get(0) instanceof FilterOpInfo);
        FilterOpInfo filterOpInfo = (FilterOpInfo) infos.get(0);
        assertTrue(filterOpInfo.getTable().equals("roles"));
        assertTrue(filterOpInfo.toString().equals("builtin@="));
    }

    @Test
    public void testParseJoinInfo() throws Exception {
        Rule rule = new Rule(q7);
        List<Info> infos = rule.parse();
        assertEquals("Should have 5 Info", 5, infos.size());
        List<Info> filterOpInfos = infos.stream()
                                    .filter(i -> i instanceof FilterOpInfo)
                                    .collect(Collectors.toList());
        assertEquals("Should have 3 FilterOpInfo", 3, filterOpInfos.size());
        List<Info> pkInfos = infos.stream()
                                .filter(i -> i instanceof PkInfo)
                                .collect(Collectors.toList());
        assertEquals("Should have 1 PkInfo", 1, pkInfos.size());
        List<Info> fkInfos = infos.stream()
                                .filter(i -> i instanceof FkInfo)
                                .collect(Collectors.toList());
        assertEquals("Should have 1 FkInfo", 1, fkInfos.size());
        // check table names and key attributes on join information
        PkInfo pkInfo = (PkInfo) pkInfos.get(0);
        assertTrue(pkInfo.getTable().equals("users"));
        assertTrue(pkInfo.getFkTable().equals("watchers_copy"));
        assertTrue(pkInfo.getPk().equals("id"));
        assertTrue(pkInfo.getFk().equals("user_id"));

        FkInfo fkInfo = (FkInfo) fkInfos.get(0);
        assertTrue(fkInfo.getTable().equals("watchers_copy"));
    }
}