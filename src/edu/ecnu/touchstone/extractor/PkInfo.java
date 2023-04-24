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
        if (leftTable.getPrimaryKey().toString().contains(left.getColumnName())) {
            this.pk = left.getColumnName();
            this.pkTable = (left.getTable() != null) ? left.getTable().getName() : query.attrToTable(pk).getTableName();
            this.fk = right.getColumnName();
            this.fkTable = (right.getTable() != null) ? right.getTable().getName() : query.attrToTable(fk).getTableName();
        } else {
            this.pk = right.getColumnName();
            this.pkTable = (right.getTable() != null) ? right.getTable().getName() : query.attrToTable(pk).getTableName();
            this.fk = left.getColumnName();
            this.fkTable = (left.getTable() != null) ? left.getTable().getName() : query.attrToTable(fk).getTableName();
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