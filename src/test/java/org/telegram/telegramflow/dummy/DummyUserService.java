package org.telegram.telegramflow.dummy;

import org.telegram.telegramflow.objects.DummyRole;
import org.telegram.telegramflow.objects.DummyUser;
import org.telegram.telegramflow.objects.TelegramRole;
import org.telegram.telegramflow.objects.TelegramUser;

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
