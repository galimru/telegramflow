package org.telegram.telegramflow.api;

import org.telegram.telegramflow.common.TelegramRole;
import org.telegram.telegramflow.common.TelegramUser;

public interface UserManager {

    TelegramUser create();

    TelegramUser find(String userId);

    TelegramRole retrieveRole(TelegramUser user);

    void save(TelegramUser user);
}
