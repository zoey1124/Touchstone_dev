package edu.ecnu.touchstone.extractor;

import java.util.ArrayList;
import java.util.List;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

public class FilterOpInfo implements Info {
    String expression = null;
    String operator = null;
    String table = null;

    public FilterOpInfo(InExpression expr, String operator, Query query) {
        Expression left = expr.getLeftExpression();
        ItemsList right = expr.getRightItemsList();
        Column leftColumn = getLeftColumn(left);
        this.expression = leftColumn.getColumnName();
        this.table = query.attrToTable(expression).getTableName();
        this.operator = operator + String.format("(%d)", right.toString().split(",").length);
    }

    public FilterOpInfo(Between between, String operator, Query query) {
        Expression left = between.getLeftExpression();
        Column leftColumn = getLeftColumn(left);
        this.expression = leftColumn.getColumnName();
        this.table = query.attrToTable(expression).getTableName();
        this.operator = operator;
    }

    public FilterOpInfo(BinaryExpression expr, String operator, Query query) {
        Expression left = expr.getLeftExpression();
        Column leftColumn = getLeftColumn(left);
        this.expression = leftColumn.getColumnName();
        this.table = (leftColumn.getTable() != null) ? this.table = leftColumn.getTable().getName()
                                                     : query.attrToTable(expression).getTableName();
        this.operator = operator;
    }

    private Column getLeftColumn(Expression left) {
        List<Column> leftColumns = new ArrayList<>();
        left.accept(new ExpressionVisitorAdapter() {
            @Override
            public void visit(Column column) {
                leftColumns.add(column);
            }
        });
        Column leftColumn = leftColumns.get(0);
        return leftColumn;
    }

    @Override
    public String toString() {
        return this.expression + "@" + this.operator;
    }
    @Override 
    public String getTable() {
        return this.table;
    }
}