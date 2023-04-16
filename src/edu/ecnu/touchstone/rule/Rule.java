package edu.ecnu.touchstone.rule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import edu.ecnu.touchstone.constraintchain.*;
import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.run.Touchstone;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.ComparisonOperator;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.ExistsExpression;
import net.sf.jsqlparser.expression.operators.relational.GreaterThan;
import net.sf.jsqlparser.expression.operators.relational.GreaterThanEquals;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.LikeExpression;
import net.sf.jsqlparser.expression.operators.relational.MinorThan;
import net.sf.jsqlparser.expression.operators.relational.MinorThanEquals;
import net.sf.jsqlparser.expression.operators.relational.NotEqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.ExpressionDeParser;

public class Rule {
    private Logger logger = null;

    public Rule() {
        logger = Logger.getLogger(Touchstone.class);
    }

    public static List<Rule> NQcase(SelectBody selectBody) {
        List<Rule> rules = new ArrayList<>();
        // set operations
        if (selectBody instanceof SetOperationList) {

        }
        else if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            // select
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            for (SelectItem selectItem: selectItems) {
                if (selectItem.toString().contains("select")) {

                }
            }
            // from
            FromItem from = plainSelect.getFromItem();
            if (from instanceof SubSelect) {
                rules.add(new FromRule());
            }
            // where
            Expression where = plainSelect.getWhere();
            if (where != null && where.toString().contains("select")) {
                where.accept(new ExpressionVisitorAdapter() {
                    @Override 
                    public void visit(InExpression inExpression) {
                        if (inExpression.toString().contains("select")) {
                            if (inExpression.isNot()) {
                                rules.add(new NotInRule());
                            } else {
                                rules.add(new InRule());
                            }
                        }
                    }
                    @Override
                    public void visit(ExistsExpression existExpression) {
                        if (existExpression.toString().contains("select")) {
                            if (existExpression.isNot()) {
                                rules.add(new NotExistRule());
                            } else {
                                rules.add(new ExistRule());
                            }

                        }
                    }
                });
            }
            // having
            Expression having = plainSelect.getHaving();
            if (having != null) {

            }
        } else if (selectBody instanceof WithItem) {

        } else {

        }
        return rules;
    }

    /* 
     * @Input: a flat Select query (e.g., no WITH ..., CREATE ...)
     * @Output: a list of ConstraintChain generated by this query 
     */
    public HashMap<String, List<CCNode>> parse(Query query) {
        Statement stmt = query.getStmt();
        Select select = (Select) stmt;
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect) selectBody;
        HashMap<String, List<CCNode>> ret = new HashMap<>();
        // where case
        Expression where = plainSelect.getWhere();
        if (where != null) {
            parseWhere(where);
        }

        return ret;
    }

    /*
     * @Input: where expression
     * @Description: parse all conditions in where
     * @Return: e.g., "a.c1 >= 3", "a.c2 like '%pattern'", "a.c3 in (1, 2, 3)"
     */
    public List<Expression> parseWhere(Expression where) {
        List<Expression> ret = new ArrayList<>();
        where.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(NotExpression notExpression) {
                ret.add(notExpression);
            }

            @Override 
            public void visit(InExpression inExpression) {
                ret.add(inExpression);
            }

            @Override 
            public void visit(LikeExpression likeExpression) {
                ret.add(likeExpression);
            }

            @Override 
            public void visit(NotEqualsTo notEqualsTo) {
                ret.add(notEqualsTo);
            }

            @Override
            public void visit(EqualsTo equalsTo) {
                ret.add(equalsTo);
            }

            @Override 
            public void visit(GreaterThan greaterThan) {
                ret.add(greaterThan);
            }

            @Override 
            public void visit(GreaterThanEquals greaterThanEquals) {
                ret.add(greaterThanEquals);
            }

            @Override
            public void visit(MinorThan minorThan) {
                ret.add(minorThan);
            }

            @Override 
            public void visit(MinorThanEquals minorThanEquals) {
                ret.add(minorThanEquals);
            }
        });
        return ret;
    }

    public static void main(String[] args) {
        String sql = "select * from t where (type <> 1) or (b = 2 and c <> 'hehe');";
        try {
            PlainSelect selectSql = ((PlainSelect) ((Select) CCJSqlParserUtil.parse(sql)).getSelectBody());
            Expression e = selectSql.getWhere();
            System.out.println("\n" + e);
            List<Expression> out = new ArrayList<>();
            e.accept(new ExpressionVisitorAdapter() {
                
                @Override 
                public void visit(NotEqualsTo expr) {
                    System.out.println("\n" + expr);
                    System.out.println("\n" + expr.getLeftExpression());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}