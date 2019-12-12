package org.telegram.telegramflow.dummy;

import org.telegram.telegramflow.objects.AbstractUser;
import org.telegram.telegramflow.objects.TelegramRole;

public class DummyUser extends AbstractUser {

    private DummyRole role;

    public DummyUser() {
        setUserId("1");
    }

    @Override
    public TelegramRole getRole() {
        return role;
    }

    @Override
    public void setRole(TelegramRole role) {
        this.role = (DummyRole) role;
    }
}
