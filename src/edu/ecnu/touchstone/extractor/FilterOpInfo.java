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

    public FilterOpInfo(InExpression expr, String operato, Query query) {
        Expression left = expr.getLeftExpression();
        ItemsList right = expr.getRightItemsList();
        init(left, right, operator, query);
    }

    public FilterOpInfo(BinaryExpression expr, String operator, Query query) {
        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();
        init(left, right, operator, query);
    }

    private void init(Expression left, Object right, String operator, Query query) {
        Column leftColumn = getLeftColumn(left);
        String attr = leftColumn.getColumnName();
        if (leftColumn.getTable() != null) {
            this.table = leftColumn.getTable().getName();
        } else {
            this.table = query.attrToTable(attr);
        }
        if (!right.toString().contains("$")) {
            operator += right.toString();
        }
        this.operator = operator;
        this.expression = attr;
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