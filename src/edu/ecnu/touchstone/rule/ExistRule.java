package edu.ecnu.touchstone.rule;

import java.util.HashMap;
import java.util.List;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;

public class ExistRule extends Rule {
    String type = "Exist";
    Query subquery = null;

    public ExistRule(Query query, Query subquery, HashMap<String, Integer> joinTable) {
        super(query, joinTable);
        this.subquery = subquery;
    }

    @Override
    public List<Info> apply() {
        return parse(subquery);
    }

}