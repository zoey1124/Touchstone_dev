package edu.ecnu.touchstone.extractor;

import net.sf.jsqlparser.expression.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;

public class FilterOpInfo implements Info {
    String expression = null;
    String operator = null;
    String table = null;

    public FilterOpInfo(InExpression expr, String operator) {
        Expression left = expr.getLeftExpression();
        ItemsList right = expr.getRightItemsList();
        Column leftColumn = (Column) left;
        if (leftColumn.getTable() != null) {
            this.table = leftColumn.getTable().getName();
        }
        String columnName = leftColumn.getColumnName();
        if (!right.toString().contains("$")) {
            operator += right.toString();
        }
        this.operator = operator;
        this.expression = columnName;
    }

    public FilterOpInfo(BinaryExpression expr, String operator) {
        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();
        Column leftColumn = (Column) left;
        if (leftColumn.getTable() != null) {
            this.table = leftColumn.getTable().getName();
        }
        String columnName = leftColumn.getColumnName();
        if (!right.toString().contains("$")) {
            operator += right.toString();
        }
        this.operator = operator;
        this.expression = columnName;
    }

    public void setTable(String table) {
        this.table = table;
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