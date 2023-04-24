package edu.ecnu.touchstone.rule;

import java.util.HashMap;
import java.util.List;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;

public class NotInRule extends Rule {
    String type = "NotIn";
    public NotInRule(Query query, HashMap<String, Integer> joinTable) {
        super(query, joinTable);
    }

    @Override
    public List<Info> apply() {
        return null;
    }
}