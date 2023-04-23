package edu.ecnu.touchstone.rule;

import java.util.List;

import edu.ecnu.touchstone.extractor.Info;
import edu.ecnu.touchstone.extractor.Query;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

public class FromRule extends Rule {

    String type = "From";
    Query subquery = null;

    public FromRule(Query query, Query subquery) {
        super(query);
        this.subquery = subquery;
    }

    public List<Info> apply() {
        if (isIndependent(query, subquery)){
            return parse(subquery); // TODO: change to parse later
        }
        return null;
    }

    /* 
     * @Description: return true if subquery is independent from outer query 
     *               i.e., there is no extra table in outer query 
     */
    public boolean isIndependent(Query query, Query subquery) {
        return query.getTables().size() == subquery.getTables().size();
    }

    public String getType() {
        return this.type;
    }
    
} 