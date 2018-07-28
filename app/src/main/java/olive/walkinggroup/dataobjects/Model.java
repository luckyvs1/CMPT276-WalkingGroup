package olive.walkinggroup.dataobjects;

import android.content.res.Resources;
import android.provider.Settings;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

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
    private static Group activeGroup;

    private static Group completedWalkGroup;
    private static List<Message> messageList;

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

    public static Group getActiveGroup() {
        return activeGroup;
    }

    public static void setActiveGroup(Group activeGroup) {
        Model.activeGroup = activeGroup;
    }

    public static boolean activeGroupSelected() {
        return activeGroup != null;
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

    public static void clearActiveGroup() {
        activeGroup = null;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public static void setMessageList(List<Message> messageList) {
        Model.messageList = messageList;
    }

    public static Group getCompletedWalkGroup() {
        return completedWalkGroup;
    }

    public static void setCompletedWalkGroup(Group completedWalkGroup) {
        Model.completedWalkGroup = completedWalkGroup;
    }
}
