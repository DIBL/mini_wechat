package com.Elessar.database;

import org.bson.Document;

import java.util.Map;

/**
 * Created by Hans on 1/20/19.
 */
public interface MyDatabase {
    void insert(Map<String, String> document);

    Iterable<Document> find(Map<String, String> filters);

    void updateField(Map<String, String> filters, String fieldToUpdate, String value);

    boolean isFieldEqual(Document doc, String field, String valueToComp);

}
