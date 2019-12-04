package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.DummyRole;
import org.telegram.telegramflow.common.DummyUser;
import org.telegram.telegramflow.common.Role;
import org.telegram.telegramflow.common.User;

public class DummyUserService implements UserService {
    @Override
    public User create() {
        return new DummyUser();
    }

    @Override
    public User get(String userId) {
        return new DummyUser();
    }

    @Override
    public Role retrieveRole(User user) {
        return DummyRole.ADMIN;
    }

    @Override
    public void save(User user) {
        // ignore
    }
}
