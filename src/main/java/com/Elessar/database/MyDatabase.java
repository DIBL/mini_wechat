package com.Elessar.database;

import com.Elessar.app.server.Message;
import com.Elessar.app.server.User;
import java.util.List;

/**
 * Created by Hans on 1/20/19.
 */
public interface MyDatabase {
    final static String Database = "db", USERS = "users", MESSAGES = "messages", INSERT = "insert", FIND = "find", UPDATE = "update";

    /**
     * Insert list of messages into database
     * @param messages list of data to be inserted into database
     */
    void insert(List<Message> messages);

    /**
     * Insert message data into database.
     * @param message data to be inserted into database
     *
     */
    void insert(Message message);

    /**
     * Insert user data into database. If the same user name already exists in database, an exception will be thrown
     * @param user data to be inserted into database
     *
     */
    void insert(User user);

    /**
     * Update user info in database
     * @param user defines filter and update fields
     *             user name and password if not null will be used for filter
     *             other fields will be used to update existing document in database
     * @return User info BEFORE update
     */
    User update(User user);

    /**
     * Update messages in database
     * @param filters defines group of filters as (message1 || message2 || message3 ...)
     * @param update defines update fields
     */
    void update(List<Message> filters, Message update);

    /**
     * Update messages in database
     * @param filter defines filter fields
     * @param update defines update fields
     */
    void update(Message filter, Message update);

    /**
     * Find and update messages in database
     * @param filter defines filter fields
     * @param update defines update fields
     * @return a list of messages before update
     */
    @Deprecated
    List<Message> findAndUpdate(Message filter, Message update);

    /**
     * Find users in database based on given filter
     * @param filter defines requirement filtering
     * @return a list of users which satisfies filter requirement
     */
    List<User> find(User filter);

    /**
     * Find messages in database based on given filter
     * @param filter defines requirement filtering
     * @return a list of messages which satisfies filter requirement
     */
    List<Message> find(Message filter);

}
