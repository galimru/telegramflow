package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.DummyRole;
import org.telegram.telegramflow.common.DummyUser;
import org.telegram.telegramflow.common.TelegramRole;
import org.telegram.telegramflow.common.TelegramUser;

import javax.annotation.Nonnull;

public class DummyUserService implements UserService {
    @Nonnull
    @Override
    public TelegramUser create() {
        return new DummyUser();
    }

    @Override
    public TelegramUser find(@Nonnull String userId) {
        return new DummyUser();
    }

    @Override
    public TelegramRole retrieveRole(@Nonnull TelegramUser user) {
        return DummyRole.ADMIN;
    }

    @Override
    public void save(@Nonnull TelegramUser user) {
        // ignore
    }
}
