package edu.ecnu.touchstone.rule;

import java.util.HashMap;

import edu.ecnu.touchstone.extractor.Query;

public class NotExistRule extends Rule {
    String type = "NotExists";
    public NotExistRule(Query query, HashMap<String, Integer> joinTable) {
        super(query, joinTable);
        //TODO Auto-generated constructor stub
    }
}