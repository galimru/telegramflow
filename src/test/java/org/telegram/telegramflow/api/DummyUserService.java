package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.Role;
import org.telegram.telegramflow.common.User;
import org.telegram.telegramflow.defaults.DefaultRole;
import org.telegram.telegramflow.defaults.DefaultUser;

public class DummyUserService implements UserService {
    @Override
    public User create() {
        return new DefaultUser();
    }

    @Override
    public User get(String userId) {
        return new DefaultUser();
    }

    @Override
    public Role retrieveRole(User user) {
        return DefaultRole.ADMIN;
    }

    @Override
    public void save(User user) {
        // ignore
    }
}
