package edu.ecnu.touchstone.test;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.rule.FromRule;
import edu.ecnu.touchstone.rule.Rule;
import edu.ecnu.touchstone.schema.SchemaReader;
import edu.ecnu.touchstone.schema.Table;
import edu.ecnu.touchstone.extractor.*;

public class FromRuleTest {
    String sql1 = "select supp_nation, cust_nation, l_year, sum(volume) as revenue "
                + "from (select n1.n_name as supp_nation, n2.n_name as cust_nation, "
                + "extract(year from l_shipdate) as l_year, l_extendedprice * (1 - l_discount) as volume "
                + "from supplier, lineitem, orders, customer, nation n1, nation n2 "
                + "where s_suppkey = l_suppkey and o_orderkey = l_orderkey "
                + "and c_custkey = o_custkey and s_nationkey = n1.n_nationkey "
                + "and c_nationkey = n2.n_nationkey and ( "
                + "(n1.n_name = '[NATION1]' and n2.n_name = '[NATION2]') "
                + "or (n1.n_name = '[NATION2]' and n2.n_name = '[NATION1]')) "
                + "and l_shipdate between date '1995-01-01' and date '1996-12-31') as shipping "
                + "group by supp_nation, cust_nation, l_year order by supp_nation, cust_nation, l_year;";
    SchemaReader schemaReader = new SchemaReader();
    List<Table> tpch_tables = schemaReader.read("/Users/mengzhusun/Desktop/Touchstone_dev" + "//test//input//tpch_schema_sf_1.txt");
    Query q1 = new Query(sql1, tpch_tables);

    @Test
    public void TestFromSubQueryCase() {
        Rule r = new Rule(q1);
        List<Rule> subqueryRules = r.subQueryCase(q1);
        assertEquals("Should contain 1 rule", 1, subqueryRules.size());
        FromRule fromRule = (FromRule) subqueryRules.get(0);
        assertEquals("Rule type should be From", "From", fromRule.getType());
        List<Info> infos = fromRule.apply();
        List<Info> filterOpInfos = infos.stream()
                                .filter(i -> i instanceof FilterOpInfo)
                                .collect(Collectors.toList());
        assertEquals("Should have filterOps", 5, filterOpInfos.size());
        List<Info> pkInfos = infos.stream()
                                .filter(i -> i instanceof PkInfo)
                                .collect(Collectors.toList());
        assertEquals("Should have PkInfo", 5, pkInfos.size());                     
        List<Info> fkInfos = infos.stream()
                                .filter(i -> i instanceof FkInfo)
                                .collect(Collectors.toList());
        assertEquals("Should have PkInfo", 5, fkInfos.size());

    }
}