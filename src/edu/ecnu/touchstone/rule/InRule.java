package edu.ecnu.touchstone.rule;

import java.util.HashMap;
import java.util.List;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;

public class InRule extends Rule {
    String type = "In";
    Query subquery = null;


    public InRule(Query query, Query subquery, HashMap<String, Integer> joinTable) {
        super(query, joinTable);
        this.subquery = subquery;
    }


    @Override
    public List<Info> apply() {
        return parse(subquery);
    }
}
