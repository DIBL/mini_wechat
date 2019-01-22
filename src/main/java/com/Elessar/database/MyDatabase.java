package com.Elessar.database;

import com.Elessar.app.server.User;
import java.util.List;

/**
 * Created by Hans on 1/20/19.
 */
public interface MyDatabase {
    void insert(User user);

    User update(User filter, User user);

    List<User> findUsers(User filter);

}
