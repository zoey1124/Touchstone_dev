package edu.ecnu.touchstone.rule;

import java.util.List;

import net.sf.jsqlparser.statement.select.SelectBody;
import net.sf.jsqlparser.statement.select.SetOperation;


public class SetOperationRule extends Rule {
    String type = "SetOperationNQ";
    List<SetOperation> setOps = null;
    List<SelectBody> selects = null;

    public SetOperationRule(List<SetOperation> setOps, List<SelectBody> selects) {
        this.setOps = setOps;
        this.selects = selects;
    }

    public void apply() {
        SelectBody select = selects.remove(0);
        while (selects.size() > 0 && setOps.size() > 0) {
            SetOperation setOp = setOps.remove(0);
            if (setOp.toString().equals("union")
               || setOp.toString().equals("union all")) {

            } else if (setOp.toString().equals("intersect")) {

            } else if (setOp.toString().equals("except")) {

            }
            select = selects.remove(0);
        }
    }
}