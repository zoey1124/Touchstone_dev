package edu.ecnu.touchstone.extractor;

public interface Info {
    /*
     * @Return: table name of pased info
     */
    String getTable();

    /*
     * @Return: cardinality input pattern
     */
    String toString();
}