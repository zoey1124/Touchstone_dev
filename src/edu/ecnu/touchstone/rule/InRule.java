package edu.ecnu.touchstone.rule;

import java.util.List;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;

public class InRule extends Rule {
    String type = "In";
    Query subquery = null;


    public InRule(Query query, Query subquery) {
        super(query);
        this.subquery = subquery;
    }


    public List<Info> apply() {
        return parse(subquery);
    }
}
