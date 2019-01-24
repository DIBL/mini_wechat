package com.Elessar.database;

import com.Elessar.app.server.User;
import java.util.List;

/**
 * Created by Hans on 1/20/19.
 */
public interface MyDatabase {
    String USERS = "users";

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
     * Find users in database based on given filter
     * @param filter defines requirement filtering
     * @return a list of users which satisfies filter requirement
     */
    List<User> findUsers(User filter);

}
