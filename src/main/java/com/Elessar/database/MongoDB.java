package com.Elessar.database;

import com.Elessar.app.server.User;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Created by Hans on 1/20/19.
 */
public class MongoDB implements MyDatabase{
    private final MongoDatabase db;
    public MongoDB(MongoDatabase db) {
        this.db = db;
    }

    @Override
    public void insert(User user) {
        final Document doc = new Document();
        doc.append("name", user.getName());
        doc.append("password", user.getPassword());
        doc.append("email", user.getEmail());
        doc.append("phone", user.getPhoneNumber());
        doc.append("online", user.getOnline());
        db.getCollection("users").insertOne(doc);
    }

    @Override
    public List<User> findUsers(User filter) {
        List<User> findResult = new ArrayList<>();
        try (MongoCursor<Document> cursor = db.getCollection("users").find(userToBson(filter)).iterator()) {
            while (cursor.hasNext()) {
                findResult.add(docToUser(cursor.next()));
            }
        }
        return findResult;

    }

    @Override
    public User update(User filter, User user) {
        List<User> list = findUsers(filter);
        User prevUser = null;
        if (!list.isEmpty()) {
            prevUser = list.get(0);
        }
        db.getCollection("users").updateOne(userToBson(filter), new Document("$set", userToDoc(user)));
        return prevUser;
    }



    private User docToUser(Document doc) {
        return new User(doc.getString("name"),
                 doc.getString("password"),
                 doc.getString("email"),
                 doc.getString("phone"),
                 doc.getString("online"));
    }



    private Bson userToBson(User filter) {
        Bson bson = new BsonDocument();
        if (filter.getName() != null) {
            bson = and(bson, eq("name", filter.getName()));
        }
        if (filter.getPassword() != null) {
            bson = and(bson, eq("password", filter.getPassword()));
        }
        if (filter.getEmail() != null) {
            bson = and(bson, eq("email", filter.getEmail()));
        }
        if (filter.getPhoneNumber() != null) {
            bson = and(bson, eq("phone", filter.getPhoneNumber()));
        }
        if (filter.getOnline() != null ) {
            bson = and(bson, eq("online", filter.getOnline()));
        }
        return bson;
    }

    private Document userToDoc(User user) {
        Document doc = new Document();
        if (user.getName() != null) {
            doc.append("name", user.getName());
        }
        if (user.getPassword() != null) {
            doc.append("password", user.getPassword());
        }
        if (user.getEmail() != null) {
            doc.append("email", user.getEmail());
        }
        if (user.getPhoneNumber() != null) {
            doc.append("phone", user.getPhoneNumber());
        }
        if (user.getOnline() != null) {
            doc.append("online", user.getOnline());
        }
        return doc;
    }

}
