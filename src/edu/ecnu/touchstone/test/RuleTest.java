package edu.ecnu.touchstone.test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.ecnu.touchstone.extractor.FilterOpInfo;
import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.rule.Rule;
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

    String sql5 = "select * from r join s on r.k1 = s.k2 join t on s.k3 = t.k4;";

    String sql6 = "select roles.* from roles where roles.builtin = $1;";

    @Before
    public void setUp() {
        rule = new Rule();
    }

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
    public void testParseJoinBaseCase() throws Exception {
        List<Info> conditions = new ArrayList<>();
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql5);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            List<Join> joinList = plainSelect.getJoins();
            for (Join join: joinList) {
                List<Expression> onExprs = (List<Expression>) join.getOnExpressions();  
                for (Expression onExpr: onExprs) {
                    conditions.addAll(rule.parseExpression(onExpr));
                }
            }
            assertEquals("Base case parse join ON should work", 4, conditions.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testParser() throws Exception {
        List<Table> tables = new ArrayList<>();
        Query query = new Query(0, sql1, tables);
        List<Info> infos = rule.parse(query);
        assertEquals("Base case parse should work", 3, infos.size());
    }

    @Test
    public void testFilterOpInfo() throws Exception {
        List<Table> tables = new ArrayList<>();
        Query query = new Query(0, sql6, tables);
        List<Info> infos = rule.parse(query);
        assertEquals("Should have 1 Info", 1, infos.size());
        assertTrue(infos.get(0) instanceof FilterOpInfo);
        FilterOpInfo filterOpInfo = (FilterOpInfo) infos.get(0);
        assertTrue(filterOpInfo.getTable().equals("roles"));
        assertTrue(filterOpInfo.toString().equals("builtin@="));
    }
}