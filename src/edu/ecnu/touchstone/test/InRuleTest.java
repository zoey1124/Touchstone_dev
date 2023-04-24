package edu.ecnu.touchstone.test;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class InRuleTest{
    String sql = "SELECT projects.* FROM projects WHERE (((projects.status = 1 AND EXISTS (SELECT 1 AS one FROM enabled_modules em WHERE em.project_id = projects.id AND em.name='time_tracking')) AND ((projects.is_public = TRUE AND projects.id NOT IN (SELECT project_id FROM members WHERE user_id IN (2,12))) OR projects.id IN (1,5) OR projects.id IN (2))))";
    @Test
    public void InRuleBasicTest() {

    }
}