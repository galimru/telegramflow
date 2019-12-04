package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.DummyRole;
import org.telegram.telegramflow.common.DummyUser;
import org.telegram.telegramflow.common.TelegramRole;
import org.telegram.telegramflow.common.TelegramUser;

public class DummyUserManager implements UserManager {
    @Override
    public TelegramUser create() {
        return new DummyUser();
    }

    @Override
    public TelegramUser find(String userId) {
        return new DummyUser();
    }

    @Override
    public TelegramRole retrieveRole(TelegramUser user) {
        return DummyRole.ADMIN;
    }

    @Override
    public void save(TelegramUser user) {
        // ignore
    }
}
