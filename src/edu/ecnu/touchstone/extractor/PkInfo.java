package edu.ecnu.touchstone.extractor;

import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.schema.Column;

public class PkInfo implements Info {
    String pkTable = null;
    String fkTable = null;
    String pk = null;
    String fk = null;
    int joinIndex = 0;

    // expr left and right should both be column type, and op should be =
    public PkInfo(BinaryExpression expr) {
        Column left = (Column) expr.getLeftExpression();
        Column right = (Column) expr.getRightExpression();
        pk = left.getColumnName();
        fk = right.getColumnName();
        if (left.getTable() != null) {
            this.pkTable = left.getTable().getName();
        } else {

        }
        if (right.getTable() != null) {
            this.fkTable = right.getTable().getName();
        } else {

        }
    }

    public String getPk() {
        return this.pk;
    }

    public String getFk() {
        return this.fk;
    }

    public String getPkTable() {
        return this.pkTable;
    }

    public String getFkTable() {
        return this.fkTable;
    }

    public void setJoinIndex(int index) {
        this.joinIndex = index;
    }

    public int getJoinIndex() {
        return this.joinIndex;
    }

    @Override
    public String toString() {
        return String.format("[1, %s, %d, %d]", 
        this.pk, this.joinIndex, this.joinIndex + 1); 
    }

    @Override 
    public String getTable() {
        return this.pkTable;
    }
}