package edu.ecnu.touchstone.extractor;

public class FkInfo implements Info {
    String pkTable = null;
    String fkTable = null;
    String pk = null;
    String fk = null;
    int joinIndex = 0;

    public FkInfo(PkInfo pkInfo) {
        this.pk = pkInfo.getPk();
        this.fk = pkInfo.getFk();
        this.pkTable = pkInfo.getPkTable();
        this.fkTable = pkInfo.getFkTable();
        this.joinIndex = pkInfo.getJoinIndex();
    }

    public void setJoinIndex(int i) {
        this.joinIndex = i;
    }

    @Override 
    public String getTable() {
        return this.fkTable;
    }

    @Override
    public String toString() {
        return String.format("[2, %s, %f, %s.%s, %d, %d]", 
        this.fk, 0.5, this.pkTable, this.pk, (int) Math.pow(2, this.joinIndex), (int) Math.pow(2, this.joinIndex + 1));
    }

}