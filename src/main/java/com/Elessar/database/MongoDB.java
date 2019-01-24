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
        doc.append(User.NAME, user.getName());
        doc.append(User.PASSWORD, user.getPassword());
        doc.append(User.EMAIL, user.getEmail());
        doc.append(User.ONLINE, user.getOnline());
        String phone = user.getPhoneNumber();
        if (phone != null && phone.length() > 0) {
            doc.append(User.PHONE, phone);
        }
        db.getCollection(MyDatabase.USERS).insertOne(doc);
    }

    @Override
    public List<User> findUsers(User filter) {
        final List<User> findResult = new ArrayList<>();
        try (MongoCursor<Document> cursor = db.getCollection(MyDatabase.USERS).find(userToBson(filter)).iterator()) {
            while (cursor.hasNext()) {
                findResult.add(docToUser(cursor.next()));
            }
        }
        return findResult;

    }

    @Override
    public User update(User user) {
        Bson filter = eq(User.NAME, user.getName());
        if (user.getPassword() != null) {
            filter = and(filter, eq(User.PASSWORD, user.getPassword()));
        }
        final Document prevUserDoc = db.getCollection(MyDatabase.USERS).findOneAndUpdate(filter, new Document("$set", userToDoc(user)));
        return docToUser(prevUserDoc);
    }

    private User docToUser(Document doc) {
        if (doc == null) {
            return null;
        }
        return new User(doc.getString(User.NAME),
                 doc.getString(User.PASSWORD),
                 doc.getString(User.EMAIL),
                 doc.getString(User.PHONE),
                 doc.getBoolean(User.ONLINE));
    }



    private Bson userToBson(User filter) {
        Bson bson = new BsonDocument();
        if (filter.getName() != null) {
            bson = and(bson, eq(User.NAME, filter.getName()));
        }
        if (filter.getPassword() != null) {
            bson = and(bson, eq(User.PASSWORD, filter.getPassword()));
        }
        if (filter.getEmail() != null) {
            bson = and(bson, eq(User.EMAIL, filter.getEmail()));
        }
        if (filter.getPhoneNumber() != null) {
            bson = and(bson, eq(User.PHONE, filter.getPhoneNumber()));
        }
        bson = and(bson, eq(User.ONLINE, filter.getOnline()));

        return bson;
    }

    private Document userToDoc(User user) {
        final Document doc = new Document();
        if (user.getName() != null) {
            doc.append(User.NAME, user.getName());
        }
        if (user.getPassword() != null) {
            doc.append(User.PASSWORD, user.getPassword());
        }
        if (user.getEmail() != null) {
            doc.append(User.EMAIL, user.getEmail());
        }
        if (user.getPhoneNumber() != null) {
            doc.append(User.PHONE, user.getPhoneNumber());
        }
        doc.append(User.ONLINE, user.getOnline());

        return doc;
    }

}
