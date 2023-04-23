package edu.ecnu.touchstone.extractor;

import java.util.List;

import edu.ecnu.touchstone.schema.Table;
import net.sf.jsqlparser.expression.BinaryExpression;
import net.sf.jsqlparser.schema.Column;


public class PkInfo implements Info {
    String pkTable = null;
    String fkTable = null;
    String pk = null;
    String fk = null;
    int joinIndex = 1;

    // expr left and right should both be column type, and op should be =
    public PkInfo(BinaryExpression expr, Query query) {
        Column left = (Column) expr.getLeftExpression();
        Column right = (Column) expr.getRightExpression();
        Table leftTable = query.attrToTable(left.getColumnName());
        // set keys
        if (leftTable.getPrimaryKey().contains(left.getColumnName())) {
            this.pk = left.getColumnName();
            this.fk = right.getColumnName();
        } else {
            this.pk = right.getColumnName();
            this.fk = left.getColumnName();
        }
        // set tables according to keys
        this.pkTable = query.attrToTable(this.pk).getTableName();
        this.fkTable = query.attrToTable(this.fk).getTableName();
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
        return String.format("[1, %s.%s, %d, %d]", 
        this.pkTable, this.pk, this.joinIndex, this.joinIndex + 1); 
    }

    @Override 
    public String getTable() {
        return this.pkTable;
    }
}