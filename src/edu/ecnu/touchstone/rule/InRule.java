package edu.ecnu.touchstone.rule;

import edu.ecnu.touchstone.extractor.Query;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.ExpressionVisitor;
import net.sf.jsqlparser.expression.ExpressionVisitorAdapter;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;

public class InRule extends Rule {
    String type = "InNQ";

    public InRule(Query query) {
        super(query);
    }

    public SubSelect getSubSelect() {
        Statement stmt = query.getStmt();
        Select select = (Select) stmt;
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        SubSelect subSelect = null;
        where.accept(new ExpressionVisitorAdapter() {
            // SubSelect subSelect = null;
            @Override 
            public void visit(SubSelect subSelect) {
                subSelect = subSelect;
            }
        });
        return null;
    }

    public void apply() {

    }
}
