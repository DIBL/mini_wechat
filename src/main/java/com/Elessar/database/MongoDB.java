package com.Elessar.database;

import com.Elessar.app.server.Message;
import com.Elessar.app.server.User;
import com.Elessar.app.util.Metric;
import com.Elessar.app.util.MetricManager;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.BsonDocument;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.or;

/**
 * Created by Hans on 1/20/19.
 */
public class MongoDB implements MyDatabase{
    private static final Logger logger = LogManager.getLogger(MongoDB.class);
    private final MongoDatabase db;
    private final MetricManager metricManager;

    public MongoDB(MongoDatabase db, MetricManager metricManager) {
        this.db = db;
        this.metricManager = metricManager;
    }

    @Override
    public void insert(List<Message> messages) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.MESSAGES).append(".")
                                                                           .append(MyDatabase.INSERT).append("Many").toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during insert message list: {}", e.getMessage());
        }

        final List<Document> docs = new ArrayList<>();
        for (Message message : messages) {
            docs.add(msgToDoc(message));
        }
        db.getCollection(MyDatabase.MESSAGES).insertMany(docs);

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during insert message list: {}", e.getMessage());
        }
    }

    @Override
    public void insert(Message message) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.MESSAGES).append(".")
                                                                           .append(MyDatabase.INSERT).append("One").toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during insert single message: {}", e.getMessage());
        }

        final Document doc = msgToDoc(message);
        db.getCollection(MyDatabase.MESSAGES).insertOne(doc);

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during insert single message: {}", e.getMessage());
        }
    }

    @Override
    public void insert(User user) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.USERS).append(".")
                                                                           .append(MyDatabase.INSERT).append("One").toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during insert single user: {}", e.getMessage());
        }

        final Document doc = userToDoc(user);
        db.getCollection(MyDatabase.USERS).insertOne(doc);

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during insert single user: {}", e.getMessage());
        }
    }

    @Override
    public List<Message> find(Message filter) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.MESSAGES).append(".")
                                                                           .append(MyDatabase.FIND).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during find messages: {}", e.getMessage());
        }

        final List<Message> findResult = new ArrayList<>();
        try (MongoCursor<Document> cursor = db.getCollection(MyDatabase.MESSAGES).find(msgToBson(filter)).iterator()) {
            while (cursor.hasNext()) {
                findResult.add(docToMsg(cursor.next()));
            }
        }

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during find messages: {}", e.getMessage());
        }

        return findResult;
    }

    @Override
    public List<User> find(User filter) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.USERS).append(".")
                                                                           .append(MyDatabase.FIND).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during find users: {}", e.getMessage());
        }

        final List<User> findResult = new ArrayList<>();
        try (MongoCursor<Document> cursor = db.getCollection(MyDatabase.USERS).find(userToBson(filter)).iterator()) {
            while (cursor.hasNext()) {
                findResult.add(docToUser(cursor.next()));
            }
        }

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during find users: {}", e.getMessage());
        }

        return findResult;
    }

    @Deprecated
    @Override
    public List<Message> findAndUpdate(Message filter, Message update) {
        final List<Message> findResult = find(filter);
        db.getCollection(MyDatabase.MESSAGES).updateMany(msgToBson(filter), new Document("$set", msgToDoc(update)));
        return findResult;
    }

    @Override
    public void update(List<Message> filters, Message update) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.MESSAGES).append(".")
                                                                           .append(MyDatabase.UPDATE).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during update messages: {}", e.getMessage());
        }

        db.getCollection(MyDatabase.MESSAGES).updateMany(msgsToBson(filters), new Document("$set", msgToDoc(update)));

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during update messages: {}", e.getMessage());
        }
    }

    @Override
    public void update(Message filter, Message update) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.MESSAGES).append(".")
                                                                           .append(MyDatabase.UPDATE).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during update messages: {}", e.getMessage());
        }

        db.getCollection(MyDatabase.MESSAGES).updateMany(msgToBson(filter), new Document("$set", msgToDoc(update)));

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during update messages: {}", e.getMessage());
        }
    }

    @Override
    public User update(User user) {
        final Metric metric = new Metric(metricManager, new StringBuilder().append(MyDatabase.Database).append(".")
                                                                           .append(MyDatabase.USERS).append(".")
                                                                           .append(MyDatabase.UPDATE).toString());
        try {
            metric.timerStart();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to start timer during update users: {}", e.getMessage());
        }

        Bson filter = eq(User.NAME, user.getName());

        if (user.getPassword() != null) {
            filter = and(filter, eq(User.PASSWORD, user.getPassword()));
        }

        final User update = new User(null, null, user.getEmail(), user.getPhoneNumber(), user.getURL(), user.getOnline());
        final Document prevUserDoc = db.getCollection(MyDatabase.USERS).findOneAndUpdate(filter, new Document("$set", userToDoc(update)));

        try {
            metric.timerStop();
        } catch (Exception e) {
            logger.debug("Caught exception when trying to stop timer during update users: {}", e.getMessage());
        }

        return docToUser(prevUserDoc);
    }

    private Message docToMsg(Document doc) {
        if (doc == null) {
            return null;
        }
        return new Message(doc.getString(Message.FROM_USER),
                doc.getString(Message.TO_USER),
                doc.getString(Message.TEXT),
                doc.getLong(Message.TIMESTAMP),
                doc.getBoolean(Message.ISDELIVERED));
    }

    private User docToUser(Document doc) {
        if (doc == null) {
            return null;
        }
        return new User(doc.getString(User.NAME),
                 doc.getString(User.PASSWORD),
                 doc.getString(User.EMAIL),
                 doc.getString(User.PHONE),
                 doc.getString(User.URL),
                 doc.getBoolean(User.ONLINE));
    }

    private Bson msgToBson(Message filter) {
        Bson bson = new BsonDocument();
        if (filter.getFromUser() != null) {
            bson = and(bson, eq(Message.FROM_USER, filter.getFromUser()));
        }
        if (filter.getToUser() != null) {
            bson = and(bson, eq(Message.TO_USER, filter.getToUser()));
        }
        if (filter.getText() != null) {
            bson = and(bson, eq(Message.TEXT, filter.getText()));
        }
        if (filter.getTimestamp() != null) {
            bson = and(bson, eq(Message.TIMESTAMP, filter.getTimestamp()));
        }
        if (filter.getIsDelivered() != null) {
            bson = and(bson, eq(Message.ISDELIVERED, filter.getIsDelivered()));
        }

        return bson;
    }

    private Bson msgsToBson(List<Message> filters) {
        final List<Bson> filtersBson = new ArrayList<>();
        for (Message filter : filters) {
            filtersBson.add(msgToBson(filter));
        }
        return or(filtersBson);
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
        if (filter.getURL() != null) {
            bson = and(bson, eq(User.URL, filter.getURL()));
        }
        if (filter.getOnline() != null) {
            bson = and(bson, eq(User.ONLINE, filter.getOnline()));
        }

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
        if (user.getURL() != null) {
            doc.append(User.URL, user.getURL());
        }
        if (user.getOnline() != null) {
            doc.append(User.ONLINE, user.getOnline());
        }
        return doc;
    }

    private Document msgToDoc(Message msg) {
        final Document doc = new Document();
        if (msg.getFromUser() != null) {
            doc.append(Message.FROM_USER, msg.getFromUser());
        }
        if (msg.getToUser() != null) {
            doc.append(Message.TO_USER, msg.getToUser());
        }
        if (msg.getText() != null) {
            doc.append(Message.TEXT, msg.getText());
        }
        if (msg.getTimestamp() != null) {
            doc.append(Message.TIMESTAMP, msg.getTimestamp());
        }
        if (msg.getIsDelivered() != null) {
            doc.append(Message.ISDELIVERED, msg.getIsDelivered());
        }
        return doc;
    }

}

