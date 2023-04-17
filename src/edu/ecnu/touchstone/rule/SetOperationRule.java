package edu.ecnu.touchstone.rule;

import java.util.List;

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


public class SetOperationRule extends Rule {
    String type = "SetOperationNQ";
    List<SetOperation> setOps = null;
    List<SelectBody> selects = null;
    List<Table> tables = null;

    public SetOperationRule(List<Table> tables, List<SetOperation> setOps, List<SelectBody> selects) {
        this.setOps = setOps;
        this.selects = selects;
        this.tables = tables;
    }

    
    public List<Info> apply() {
        SelectBody select1 = this.selects.get(0);
        SelectBody select2 = this.selects.get(1);
        SetOperation setOperation = this.setOps.get(0);
        if (setOperation.toString().equals("UNION")
            || setOperation.toString().equals("UNION ALL")) {
                Query query1 = new Query(0, select1.toString(), this.tables);
                Query query2 = new Query(0, select2.toString(), this.tables);
                parse(query1);
                parse(query2);
        }
        else if (setOperation.toString().equals("INTERSECT")) {

        }
        else if (setOperation.toString().equals("EXCEPT")) {
            Query query1 = new Query(0, select1.toString(), this.tables);
            parse(query1);
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