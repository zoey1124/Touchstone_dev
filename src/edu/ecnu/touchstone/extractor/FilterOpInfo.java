package edu.ecnu.touchstone.extractor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        String attr = leftColumn.getColumnName();
        this.table = (leftColumn.getTable() != null) ? leftColumn.getTable().getName() : query.attrToTable(attr);
        this.expression = attr;
        if (!right.toString().contains("$")) {
            operator += getRightValue(right);
        }
        this.operator = operator;
    }

    public FilterOpInfo(BinaryExpression expr, String operator, Query query) {
        Expression left = expr.getLeftExpression();
        Expression right = expr.getRightExpression();
        Column leftColumn = getLeftColumn(left);
        String attr = leftColumn.getColumnName();
        this.table = (leftColumn.getTable() != null) ? this.table = leftColumn.getTable().getName()
                                                     : query.attrToTable(attr);
        this.expression = attr;
        if (!right.toString().contains("$")) {
            operator += getRightValue(right);
        } 
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

    private String getRightValue(ItemsList right) {
        List<String> rightValue = new ArrayList<>();
        right.accept((ItemsListVisitor) visitor(rightValue));
        if (rightValue.size() > 0) {
            return String.format("(%s)", rightValue.stream()
                                            .map(elm -> elm.replace("%", ""))
                                            .collect(Collectors.joining(", ")));
        }
        return "";
    }
    private String getRightValue(Expression right) {
        List<String> rightValue = new ArrayList<>();
        right.accept(visitor(rightValue));
        return (rightValue.size() > 0) ? rightValue.get(0).replace("%", "") : "";
    }
    private ExpressionVisitor visitor(List<String> rightValue) {
        return new ExpressionVisitorAdapter() {
            @Override 
            public void visit(StringValue stringValue) {
                rightValue.add(stringValue.getNotExcapedValue());
            }
            @Override
            public void visit(DateValue dateValue) {
                rightValue.add(dateValue.toString());
            }
            @Override
            public void visit(LongValue longValue) {
                rightValue.add(longValue.toString());
            }
            @Override 
            public void visit(DoubleValue doubleValue) {
                rightValue.add(doubleValue.toString());
            }
            @Override 
            public void visit(TimeValue timeValue) {
                rightValue.add(timeValue.toString());
            }
            @Override 
            public void visit(TimestampValue timestampValue) {
                rightValue.add(timestampValue.toString());
            }
        };
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