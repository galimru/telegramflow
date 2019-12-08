package org.telegram.telegramflow.objects;

public interface TelegramUser {

    String getUserId();

    void setUserId(String userId);

    String getUsername();

    void setUsername(String username);

    String getFirstName();

    void setFirstName(String firstName);

    String getLastName();

    void setLastName(String lastName);

    AuthState getAuthState();

    void setAuthState(AuthState authState);

    TelegramRole getRole();

    void setRole(TelegramRole role);

    String getPhone();

    void setPhone(String phone);

    String getActiveScreen();

    void setActiveScreen(String activeScreen);
}
