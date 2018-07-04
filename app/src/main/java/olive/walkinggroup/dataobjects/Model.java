package olive.walkinggroup.dataobjects;

import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;

import olive.walkinggroup.R;
import olive.walkinggroup.proxy.ProxyBuilder;
import olive.walkinggroup.proxy.WGServerProxy;

/**
 * Model singleton class to store fields to be used across Activities.
 *
 * Information includes the current logged in user information, and a
 * proxy for accessing the server.
 *
 * Operations include updating proxy using a valid token.
 */

public class Model {
    // TODO: Have User object information save between application launches.
    private User currentUser;
    private static WGServerProxy proxy;

    private static Model instance;


    private Model(){
        // Prevent external instancing
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
            updateProxy(null);
        }
        return instance;
    }

    public static void updateProxy(String token) {
        String apiKey = "506224F3-9B49-46FA-BCD1-E6817704A6BF6";
        proxy = ProxyBuilder.getProxy(apiKey, token);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public WGServerProxy getProxy() {
        return proxy;
    }
}
