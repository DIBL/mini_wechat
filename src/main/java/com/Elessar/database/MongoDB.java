package com.Elessar.database;

import com.mongodb.client.MongoCollection;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.Map;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

/**
 * Created by Hans on 1/20/19.
 */
public class MongoDB implements MyDatabase{
    private final MongoCollection<Document> users;
    public MongoDB(MongoCollection<Document> users) {
        this.users = users;
    }

    @Override
    public void insert(Map<String, String> document) {
        final Document doc = new Document();
        for (Map.Entry<String, String> field : document.entrySet()) {
            doc.append(field.getKey(), field.getValue());
        }
        users.insertOne(doc);
    }

    @Override
    public Iterable<Document> find(Map<String, String> filters) {
        Bson query = new BsonDocument();
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            query = and(query, eq(filter.getKey(), filter.getValue()));
        }
        return users.find(query);
    }

    @Override
    public void updateField(Map<String, String> filters, String fieldToUpdate, String value) {
        Bson query = new BsonDocument();
        for (Map.Entry<String, String> filter : filters.entrySet()) {
            query = and(query, eq(filter.getKey(), filter.getValue()));
        }
        users.updateOne(query, set(fieldToUpdate, value));
    }

    @Override
    public boolean isFieldEqual(Document doc, String field, String valueToComp) {
        return doc.getString(field).equals(valueToComp);
    }

}
