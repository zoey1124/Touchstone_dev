package edu.ecnu.touchstone.extractor;

import java.util.List;

/* Represents a combination of filter logic. */
public class FilterInfo {
    List<FilterOpInfo> filterOpList = null;
    String logicalRelation = "and";
    String table = null;
    float selevtivity = 0f;

    public FilterInfo(List<FilterOpInfo> filterOpList, float selectivity, String table) {
        this.filterOpList = filterOpList;
        this.selevtivity = selectivity;
        this.table = table;
    }

    @Override 
    public String toString() {
        if (filterOpList.size() == 0) {
            return "";
        }
        else if (filterOpList.size() == 1) {
            return String.format("[0, %s, %s]", filterOpList.get(0).toString(), this.selevtivity);
        }
        String ret = "[0, ";
        for (FilterOpInfo filterOpInfo: filterOpList) {
            ret += filterOpInfo.toString() + "#";
        }
        ret += String.format("#%s, %f", this.logicalRelation, this.selevtivity);
        return ret;
    }
}