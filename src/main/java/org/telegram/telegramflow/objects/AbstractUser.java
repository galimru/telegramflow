package org.telegram.telegramflow.objects;

public abstract class AbstractUser implements TelegramUser {

    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private AuthState authState;
    private String phone;
    private String activeScreen;

    @Override
    public String getUserId() {
        return userId;
    }

    @Override
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public AuthState getAuthState() {
        return authState;
    }

    @Override
    public void setAuthState(AuthState authState) {
        this.authState = authState;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getActiveScreen() {
        return activeScreen;
    }

    @Override
    public void setActiveScreen(String activeScreen) {
        this.activeScreen = activeScreen;
    }
}
