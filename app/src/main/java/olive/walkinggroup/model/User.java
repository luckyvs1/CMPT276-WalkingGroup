package olive.walkinggroup.model;


// Singleton based user class to keep track of the logged in user
public class User {

    private String name;
    private String email;
    private String password;

    // Instantiating a singleton pattern for the user
    // Code from Dr. Brian Fraser
    // https://www.youtube.com/watch?v=evkPjPIV6cw&feature=youtu.be

    private static User instance;

    private User() {
        // Private to prevent anyone else from instantiating
    }

    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
