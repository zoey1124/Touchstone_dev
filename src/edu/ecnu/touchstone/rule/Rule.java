package edu.ecnu.touchstone.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.touchstone.extractor.FilterOpInfo;
import edu.ecnu.touchstone.extractor.FkInfo;
import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.PkInfo;
import edu.ecnu.touchstone.extractor.Query;
import edu.ecnu.touchstone.run.Touchstone;
import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class Rule {
    private Logger logger = null;
    Query query = null;
    String[] ruleOrder = {"SELECT", "UNION", "In", "EXIST", "NOT IN", "NOT EXIST"};

    public Rule(Query query) {
        logger = Logger.getLogger(Touchstone.class);
        this.query = query;
    }

    public Query getQuery() {
        return this.query;
    }

    public List<Rule> NQcase(SelectBody selectBody) {
        List<Rule> rules = new ArrayList<>();
        // set operations
        if (selectBody instanceof SetOperationList) {
            SetOperationList setOperationList = (SetOperationList) selectBody;
            // SetOperationRule setOperationRule = 
            //         new SetOperationRule(this.query.getTables(), 
            //                              setOperationList.getOperations(), 
            //                              setOperationList.getSelects());
            // setOperationRule.apply();
        }
        else if (selectBody instanceof PlainSelect) {
            PlainSelect plainSelect = (PlainSelect) selectBody;
            // select
            List<SelectItem> selectItems = plainSelect.getSelectItems();
            for (SelectItem selectItem: selectItems) {
                if (selectItem.toString().contains("select")) {
                    // SelectRule selectRule = new SelectRule(this);
                    // selectRule.apply();
                }
            }
            // from
            FromItem from = plainSelect.getFromItem();
            if (from instanceof SubSelect) {
                // FromRule fromRule = new FromRule();
                // fromRule.apply();
            }
            // where
            Expression where = plainSelect.getWhere();
            if (where != null && where.toString().contains("select")) {
                where.accept(new ExpressionVisitorAdapter() {
                    @Override 
                    public void visit(InExpression inExpression) {
                        if (inExpression.toString().contains("select")) {
                            if (inExpression.isNot()) {
                                // NotInRule notInRule = new NotInRule();
                                // notInRule.apply();
                            } else {
                                // InRule inRule = new InRule();
                                // inRule.apply();
                            }
                        }
                    }
                    @Override
                    public void visit(ExistsExpression existExpression) {
                        if (existExpression.toString().contains("select")) {
                            if (existExpression.isNot()) {
                                // rules.add(new NotExistRule());
                            } else {
                                // rules.add(new ExistRule());
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
    public List<Info> parse() {
        Statement stmt = this.query.getStmt();
        Select select = (Select) stmt;
        SelectBody selectBody = select.getSelectBody();
        PlainSelect plainSelect = (PlainSelect) selectBody;
        List<Info> ret = new ArrayList<>();
        // where 
        Expression where = plainSelect.getWhere();
        if (where != null) {
            ret.addAll(parseExpression(where));
        }
        // join
        List<Join> joinList = plainSelect.getJoins();
        if (joinList != null) {
            for (Join join: joinList) {
                List<Expression> onExprs = (List<Expression>) join.getOnExpressions();  
                for (Expression onExpr: onExprs) {
                    List<Info> conditions = parseExpression(onExpr);
                    ret.addAll(conditions);
                }
            }
        }
        //having
        Expression having = plainSelect.getHaving();
        if (having != null) {
            List<Info> conditions = parseExpression(having);
            ret.addAll(conditions);
        }
        return ret;
    }

    /*
     * @Input: Expression
     * @Description: parse all conditions in where
     * @Return: FilterOp and JoinInformations
     */
    public List<Info> parseExpression(Expression expr) {
        List<Info> infos = new ArrayList<>();
        expr.accept(new ExpressionVisitorAdapter() {

            @Override 
            public void visit(InExpression inExpression) {
                String operator = (inExpression.isNot()) ? "not in" : "in";
                FilterOpInfo filterOp = new FilterOpInfo(inExpression, operator, getQuery());
                infos.add(filterOp);
            }

            @Override 
            public void visit(LikeExpression likeExpression) {
                String operator = (likeExpression.isNot()) ? "not like" : "like";
                FilterOpInfo filterOp = new FilterOpInfo(likeExpression, operator, getQuery());
                infos.add(filterOp);
            }

            @Override 
            public void visit(NotEqualsTo notEqualsTo) {
                FilterOpInfo filterOp = new FilterOpInfo(notEqualsTo, "<>", getQuery());
                infos.add(filterOp);
            }

            @Override
            public void visit(EqualsTo equalsTo) {
                if (isJoinCondition(equalsTo)) {
                    PkInfo pkInfo = new PkInfo(equalsTo, getQuery());
                    FkInfo fkInfo = new FkInfo(pkInfo);
                    infos.add(pkInfo);
                    infos.add(fkInfo);
                } else {
                    FilterOpInfo filterOp = new FilterOpInfo(equalsTo, "=", getQuery());
                    infos.add(filterOp);
                }
            }

            @Override 
            public void visit(GreaterThan greaterThan) {
                FilterOpInfo filterOp = new FilterOpInfo(greaterThan, ">", getQuery());
                infos.add(filterOp);
            }

            @Override 
            public void visit(GreaterThanEquals greaterThanEquals) {
                FilterOpInfo filterOp = new FilterOpInfo(greaterThanEquals, ">=", getQuery());
                infos.add(filterOp);
            }

            @Override
            public void visit(MinorThan minorThan) {
                FilterOpInfo filterOp = new FilterOpInfo(minorThan, "<", getQuery());
                infos.add(filterOp);
            }

            @Override 
            public void visit(MinorThanEquals minorThanEquals) {
                FilterOpInfo filterOp = new FilterOpInfo(minorThanEquals, "<=", getQuery());
                infos.add(filterOp);
            }
        });
        return infos;
    }

    /*
     * @Input: BinaryExpression like a = b, a >= b
     */
    public boolean isJoinCondition(BinaryExpression expr) {
        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();
        return (left instanceof Column) && (right instanceof Column) 
                && (!right.toString().contains("$"));
    }

    /*
     * @Return: String and || or
     */
    public String getLogicalRelation() {
        List<String> logicalRelations = new ArrayList<>();
        Select select = (Select) this.query.getStmt();
        PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
        Expression where = plainSelect.getWhere();
        where.accept(new ExpressionVisitorAdapter() {
            @Override 
            public void visit(OrExpression orExpression) {
                logicalRelations.add("or");
            }

            @Override 
            public void visit(AndExpression andExpression) {
                logicalRelations.add("and");
            }
        });
        return (logicalRelations.contains("or")) ? "or" : "and";
    }

    public static void main(String[] args) {
        String sql = "SELECT count(users.* ) FROM users INNER JOIN email_addresses ON email_addresses.user_id = users.id WHERE users.type IN ($1, $2) AND email_addresses.address = $3;";
        PropertyConfigurator.configure(".//test//lib//log4j.properties");
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
            System.out.println(tablesNamesFinder.getTableList(stmt));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
