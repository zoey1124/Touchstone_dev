package edu.ecnu.touchstone.rule;

import java.util.ArrayList;
import java.util.HashMap;
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
    String type = "SetOperation";
    SetOperation setOp = null;
    List<SelectBody> selects = null;
    HashMap<String, Integer> joinTable = null;

    public SetOperationRule(Query query, HashMap<String, Integer> joinTable) {
        super(query, joinTable);
        Select select = (Select) query.getStmt();
        SetOperationList setOperationList = (SetOperationList) select.getSelectBody();
        this.setOp = setOperationList.getOperations().get(0);
        this.selects = setOperationList.getSelects();
        this.joinTable = joinTable;
    }

    
    @Override 
    public List<Info> apply() {
        List<Info> CCList =  new ArrayList<>();
        String sql1 = this.selects.get(0).toString();
        String sql2 = this.selects.get(1).toString();
        Query q1 = new Query(sql1, this.query.getTables());
        Query q2 = new Query(sql2, this.query.getTables());
        if (setOp.toString().equals("UNION")
            || setOp.toString().equals("UNION ALL")) {
                Rule rule1 = new Rule(q1, joinTable);
                CCList.addAll(rule1.parse(q1));
                Rule rule2 = new Rule(q2, joinTable);
                CCList.addAll(rule2.parse(q2));
        }
        else if (setOp.toString().equals("INTERSECT")) {

        }
        else if (setOp.toString().equals("EXCEPT")) {
            Rule rule1 = new Rule(q1, joinTable);
        }
        return CCList;
    }
}