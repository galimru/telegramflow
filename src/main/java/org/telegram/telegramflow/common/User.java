package org.telegram.telegramflow.common;

public interface User {

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

    Role getRole();

    void setRole(Role role);

    String getPhone();

    void setPhone(String phone);

    String getActiveScreen();

    void setActiveScreen(String activeScreen);
}
