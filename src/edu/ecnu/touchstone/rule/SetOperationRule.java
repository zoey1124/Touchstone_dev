package edu.ecnu.touchstone.rule;

import java.util.List;

import net.sf.jsqlparser.statement.select.SetOperation;


public class SetOperationRule extends Rule {
    String type = "SetOperationNQ";
    List<SetOperation> setOps = null;
    List<String> NQList = null;

    public SetOperationRule(List<SetOperation> setOps, List<String> NQList) {
        this.setOps = setOps;
        this.NQList = NQList;
    }

    public void apply() {
    }
}