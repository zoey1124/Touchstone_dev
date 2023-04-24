package edu.ecnu.touchstone.rule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.ecnu.touchstone.constraintchain.Filter;
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
    HashMap<String, Integer> joinTable = null;
    String[] ruleOrder = {"SELECT", "From", "UNION", "In", "EXIST", "NOT IN", "NOT EXIST"};

    public Rule(Query query, HashMap<String, Integer> joinTable) {
        logger = Logger.getLogger(Touchstone.class);
        this.query = query;
        this.joinTable = joinTable;
    }

    public Query getQuery() {
        return this.query;
    }

    public List<Info> parse(Query query) {
        List<Info> CCList = new ArrayList<>();
        if (query.containNQ()) {
            List<Rule> rules = subQueryCase(query);
            // sort, apply
            for (Rule rule: rules) {
                CCList.addAll(rule.apply());
            }
        } 
        CCList.addAll(parseBasic(query));
        return CCList;
    }

    public List<Info> apply() {
        return null;
    }

    public List<Rule> subQueryCase(Query query) {
        Statement stmt = query.getStmt();
        SelectBody selectBody = ((Select) stmt).getSelectBody();
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
            // from
            FromItem from = plainSelect.getFromItem();
            if (from instanceof SubSelect) {
                Query subquery = getSubQuery(from.toString(), query);
                FromRule rule = new FromRule(query, subquery, joinTable);
                rules.add(rule);
            }
            // where
            Expression where = plainSelect.getWhere();
            if (where != null && where.toString().contains("SELECT")) {
                where.accept(new ExpressionVisitorAdapter() {
                    @Override 
                    public void visit(InExpression inExpression) {
                        if (inExpression.toString().contains("SELECT")) {
                            if (inExpression.isNot()) {
                                // NotInRule notInRule = new NotInRule();
                                // notInRule.apply();
                            } else {
                                Query subquery = getSubQuery(inExpression.toString(), query);
                                InRule rule = new InRule(query, subquery, joinTable);
                                rules.add(rule);
                            }
                        }
                    }
                    @Override
                    public void visit(ExistsExpression existExpression) {
                        if (existExpression.toString().contains("SELECT")) {
                            if (existExpression.isNot()) {
                                Rule rule = new NotExistRule(query, joinTable);
                            } else {
                                Query subquery = getSubQuery(existExpression.toString(), query);
                                Rule rule = new ExistRule(query, subquery, joinTable);
                                rules.add(rule);
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
     * @Input: e.g. String "EXISTS (SELECT ...)"
     *         e.g. String "FROM (SELECT ...)"
     */
    private Query getSubQuery(String expr, Query query) {
        int firstIndex = expr.indexOf("(");
        int lastIndex = expr.lastIndexOf(")");
        String subqueryStr = expr.toString().substring(firstIndex+1, lastIndex);
        return new Query(subqueryStr, query.getTables());
    }

    /* 
     * @Input: a flat Select query (e.g., no WITH ..., CREATE ...)
     * @Output: a list of ConstraintChain generated by this query 
     */
    public List<Info> parseBasic(Query query) {
        Statement stmt = query.getStmt();
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
            public void visit(Between between) {
                String operator = "bet";
                FilterOpInfo filterOp = new FilterOpInfo(between, operator, getQuery());
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
                    PkInfo pkInfo = new PkInfo(equalsTo, getQuery(), getJoinTable());
                    FkInfo fkInfo = new FkInfo(pkInfo);
                    infos.add(pkInfo);
                    infos.add(fkInfo);
                } else {
                    FilterOpInfo filterOp = new FilterOpInfo(equalsTo, "=", getQuery());
                    if (!filterOp.filterOnKey()) infos.add(filterOp);
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

    public HashMap<String, Integer> getJoinTable() {
        return this.joinTable;
    }

    /*
     * @Return: String and || or
     */
    public String getLogicalRelation(Query query) {
        List<String> logicalRelations = new ArrayList<>();
        Select select = (Select) query.getStmt();
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
        String sql = "select * from issues inner join projects on projects.id = issues.project_id where exists (select 1 as one from enabled_modules where enabled_modules.project_id = project.id);";
        PropertyConfigurator.configure(".//test//lib//log4j.properties");
        try {
            Statement stmt = CCJSqlParserUtil.parse(sql);
            Select select = (Select) stmt;
            PlainSelect plainSelect = (PlainSelect) select.getSelectBody();
            Expression where = plainSelect.getWhere();
            where.accept(new ExpressionVisitorAdapter() {
                @Override 
                public void visit(ExistsExpression expr) {
                    System.out.println(expr);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
