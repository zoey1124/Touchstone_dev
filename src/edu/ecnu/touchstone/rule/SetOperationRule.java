package edu.ecnu.touchstone.rule;

import java.util.List;
import java.util.Set;

import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.schema.Table;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperation;
import net.sf.jsqlparser.statement.select.SetOperationList;

/*
 * Set Operation nested query. e.g., UNION, INTERSECT, EXCEPT
 */
public class SetOperationRule extends Rule {
    String type = "SetOperationNQ";
    SetOperation setOp = null;
    List<SelectBody> selects = null;

    public SetOperationRule(Query query) {
        super(query);
        Select select = (Select) query.getStmt();
        SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
        this.setOp = setOperationList.getOperations().get(0);
        this.selects = setOperationList.getSelects();
    }

    
    public List<Info> apply() {
        String sql1 = this.selects.get(0).toString();
        String sql2 = this.selects.get(1).toString();
        Query q1 = new Query(sql1, this.query.getTables());
        Query q2 = new Query(sql2, this.query.getTables());
        if (setOp.toString().equals("UNION")
            || setOp.toString().equals("UNION ALL")) {
                Rule rule1 = new Rule(q1);
                Rule rule2 = new Rule(q2);
        }
        else if (setOp.toString().equals("INTERSECT")) {

        }
        else if (setOp.toString().equals("EXCEPT")) {
            Rule rule1 = new Rule(q1);
        }
        return null;
    }

    public static void main(String[] args) {
        PropertyConfigurator.configure(".//test//lib//log4j.properties");
        String sql = "(select a.c1 from product) except (select b.c1 from sells);";
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
            System.out.println(setOperationList.getSelects());
            System.out.println(setOperationList.getOperations().get(0).toString());
        } catch (Exception e) {
            e.getStackTrace();
        }
        
    }
}