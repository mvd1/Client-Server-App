/**
* Class which represents the users can log in and out of the server. Each user has username, a password, and a boolean
* value which keeps track of their current login status. Getters for these three data members exist, as well as
* login and logout methods, which change the login status to true and false respectively. (Login status is set to false by
* default).
 */
public class User {

    private String username;
    private String password;
    private boolean loggedIn = false;

    public User() {
        this("Empty", "Empty");
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean getLoginStatus() { return loggedIn; }
    public void userLogin() { loggedIn = true; } // if logged out, then logs user in
    public void userLogout() { loggedIn = false; } // if logged in, then logs user out
}
