package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.Role;
import org.telegram.telegramflow.common.User;

public interface UserService {

    User create();

    User get(String userId);

    Role retrieveRole(User user);

    void save(User user);
}
